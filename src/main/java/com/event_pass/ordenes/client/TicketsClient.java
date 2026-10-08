package com.event_pass.ordenes.client;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.event_pass.ordenes.dto.EmisionTicketsResponseDTO;
import com.event_pass.ordenes.dto.TicketDTO;
import com.event_pass.ordenes.dto.TicketEmisionRequestDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TicketsClient {

    private final RestTemplate restTemplate;

    @Value("${servicios.tickets.url:http://localhost:8083}")
    private String ticketsUrl;

    public List<TicketDTO> emitirTickets(Long ordenId, Long usuarioId, Long eventoId, Integer cantidad) {
        String url = ticketsUrl + "/interno/tickets";
        TicketEmisionRequestDTO request = new TicketEmisionRequestDTO(ordenId, usuarioId, eventoId, cantidad);

        try {
            ResponseEntity<EmisionTicketsResponseDTO> response = restTemplate.postForEntity(
                    url, request, EmisionTicketsResponseDTO.class
            );

            if (response.getBody() != null && response.getBody().getTickets() != null) {
                return response.getBody().getTickets();
            }
        } catch (Exception e) {
            System.out.println("Error de conexion con microservicio Tickets: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}