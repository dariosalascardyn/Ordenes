package com.event_pass.ordenes.client;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.event_pass.ordenes.dto.EmisionTicketsResponseDTO;
import com.event_pass.ordenes.dto.TicketDTO;
import com.event_pass.ordenes.dto.TicketEmisionRequestDTO;
import com.event_pass.ordenes.exception.ServicioExternoException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TicketsClient {

    private final RestTemplate restTemplate;

    @Value("${servicios.tickets.url:http://localhost:8081}")
    private String ticketsUrl;

    public List<TicketDTO> emitirTickets(
        Long ordenId,
        Long usuarioId,
        Long eventoId,
        Integer cantidad,
        String authorization
    ) {
        String url = ticketsUrl + "/interno/tickets";
        TicketEmisionRequestDTO request = new TicketEmisionRequestDTO(ordenId, usuarioId, eventoId, cantidad);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);

        try {
            ResponseEntity<EmisionTicketsResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                EmisionTicketsResponseDTO.class
            );
            EmisionTicketsResponseDTO body = response.getBody();
            if (body == null || body.getTickets() == null || !Objects.equals(body.getOrdenId(), ordenId)) {
                throw new ServicioExternoException("RESPUESTA_INVALIDA_TICKETS");
            }
            return body.getTickets();
        } catch (RestClientException exception) {
            throw new ServicioExternoException("ERROR_EMISION_TICKETS", exception);
        }
    }
}
