package com.event_pass.ordenes.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.event_pass.ordenes.client.EventosClient;
import com.event_pass.ordenes.client.TicketsClient;
import com.event_pass.ordenes.dto.CrearOrdenRequestDTO;
import com.event_pass.ordenes.dto.OrdenResponseDTO;
import com.event_pass.ordenes.dto.TicketDTO;
import com.event_pass.ordenes.exception.ServicioExternoException;
import com.event_pass.ordenes.model.EstadoOrden;
import com.event_pass.ordenes.model.Orden;
import com.event_pass.ordenes.repository.OrdenRepository;
import com.event_pass.ordenes.security.JwtTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final EventosClient eventosClient;
    private final TicketsClient ticketsClient;
    private final JwtTokenService jwtTokenService;

    @Transactional
    public OrdenResponseDTO crearOrden(CrearOrdenRequestDTO request, String authorization) {
        validarSolicitud(request);
        String tokenComprador = jwtTokenService.validarComprador(authorization, request.getUsuarioId());

        Orden orden = new Orden();
        orden.setUsuarioId(request.getUsuarioId());
        orden.setEventoId(request.getEventoId());
        orden.setCantidad(request.getCantidad());
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden = ordenRepository.save(orden);

        boolean reservaExitosa;
        try {
            reservaExitosa = eventosClient.reservarAforo(
                orden.getEventoId(), orden.getId(), orden.getCantidad()
            );
        } catch (ServicioExternoException exception) {
            return finalizarConError(orden, exception.getCodigo());
        }

        if (!reservaExitosa) {
            orden.setEstado(EstadoOrden.RECHAZADA);
            ordenRepository.save(orden);
            return respuesta(orden, "AFORO_INSUFICIENTE", null);
        }

        List<TicketDTO> tickets;
        try {
            tickets = ticketsClient.emitirTickets(
                orden.getId(),
                orden.getUsuarioId(),
                orden.getEventoId(),
                orden.getCantidad(),
                tokenComprador
            );
        } catch (ServicioExternoException exception) {
            return finalizarConError(orden, exception.getCodigo());
        }

        if (tickets == null || tickets.size() != orden.getCantidad()) {
            return finalizarConError(orden, "CANTIDAD_TICKETS_INCORRECTA");
        }

        orden.setEstado(EstadoOrden.EMITIDA);
        ordenRepository.save(orden);
        return respuesta(orden, null, tickets);
    }

    private void validarSolicitud(CrearOrdenRequestDTO request) {
        if (request == null || request.getUsuarioId() == null || request.getUsuarioId() <= 0
            || request.getEventoId() == null || request.getEventoId() <= 0
            || request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "usuarioId, eventoId y cantidad deben ser valores positivos"
            );
        }
    }

    private OrdenResponseDTO finalizarConError(Orden orden, String motivo) {
        orden.setEstado(EstadoOrden.ERROR);
        ordenRepository.save(orden);
        return respuesta(orden, motivo, null);
    }

    private OrdenResponseDTO respuesta(Orden orden, String motivo, List<TicketDTO> tickets) {
        return OrdenResponseDTO.builder()
            .ordenId(orden.getId())
            .usuarioId(orden.getUsuarioId())
            .eventoId(orden.getEventoId())
            .cantidad(orden.getCantidad())
            .estado(orden.getEstado())
            .fecha(orden.getFecha())
            .motivo(motivo)
            .tickets(tickets)
            .build();
    }

    public OrdenResponseDTO obtenerOrden(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + ordenId));

        return OrdenResponseDTO.builder()
                .ordenId(orden.getId())
                .usuarioId(orden.getUsuarioId())
                .eventoId(orden.getEventoId())
                .cantidad(orden.getCantidad())
                .estado(orden.getEstado())
                .fecha(orden.getFecha())
                .build();
    }
}
