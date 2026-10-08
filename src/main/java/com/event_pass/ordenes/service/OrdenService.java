package com.event_pass.ordenes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_pass.ordenes.client.EventosClient;
import com.event_pass.ordenes.client.TicketsClient;
import com.event_pass.ordenes.dto.CrearOrdenRequestDTO;
import com.event_pass.ordenes.dto.OrdenResponseDTO;
import com.event_pass.ordenes.dto.TicketDTO;
import com.event_pass.ordenes.model.EstadoOrden;
import com.event_pass.ordenes.model.Orden;
import com.event_pass.ordenes.repository.OrdenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final EventosClient eventosClient;
    private final TicketsClient ticketsClient;

    @Transactional
    public OrdenResponseDTO crearOrden(CrearOrdenRequestDTO request) {
        // guarda la orden inicial con estado PENDIENTE
        Orden orden = new Orden();
        orden.setUsuarioId(request.getUsuarioId());
        orden.setEventoId(request.getEventoId());
        orden.setCantidad(request.getCantidad());
        orden.setEstado(EstadoOrden.PENDIENTE);

        orden = ordenRepository.save(orden);

        // reservar aforo en el microservicio Eventos
        boolean reservaExitosa = eventosClient.reservarAforo(
                orden.getEventoId(), orden.getId(), orden.getCantidad()
        );

        if (!reservaExitosa) {
            orden.setEstado(EstadoOrden.RECHAZADA);
            ordenRepository.save(orden);
            return OrdenResponseDTO.builder()
                    .ordenId(orden.getId())
                    .usuarioId(orden.getUsuarioId())
                    .eventoId(orden.getEventoId())
                    .cantidad(orden.getCantidad())
                    .estado(EstadoOrden.RECHAZADA)
                    .motivo("AFORO_INSUFICIENTE")
                    .fecha(orden.getFecha())
                    .build();
        }

        // emitir los tickets en el microservicio Tickets
        List<TicketDTO> tickets = ticketsClient.emitirTickets(
                orden.getId(), orden.getUsuarioId(), orden.getEventoId(), orden.getCantidad()
        );

        if (tickets == null || tickets.isEmpty()) {
            orden.setEstado(EstadoOrden.ERROR);
            ordenRepository.save(orden);
            return OrdenResponseDTO.builder()
                    .ordenId(orden.getId())
                    .estado(EstadoOrden.ERROR)
                    .motivo("ERROR_EMISION_TICKETS")
                    .build();
        }

        // confirmar la orden como EMITIDA
        orden.setEstado(EstadoOrden.EMITIDA);
        ordenRepository.save(orden);

        return OrdenResponseDTO.builder()
                .ordenId(orden.getId())
                .usuarioId(orden.getUsuarioId())
                .eventoId(orden.getEventoId())
                .cantidad(orden.getCantidad())
                .estado(EstadoOrden.EMITIDA)
                .fecha(orden.getFecha())
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