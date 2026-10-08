package com.event_pass.ordenes.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.event_pass.ordenes.dto.ReservaRequestDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
public class EventosClient {

    private final RestTemplate restTemplate;

    // Asumimos que Eventos = puerto 8081 localmente
    @Value("${servicios.eventos.url:http://localhost:8081}")
    private String eventosUrl;

    public boolean reservarAforo(Long eventoId, Long ordenId, Integer cantidad) {
        String url = eventosUrl + "/interno/eventos/" + eventoId + "/reservas";
        ReservaRequestDTO request = new ReservaRequestDTO(ordenId, cantidad);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            // Si responde 409 Conflict, no hay aforo suficiente segun el contrato
            return false;
        } catch (Exception e) { // Otros errores
            return false; 
        }
    }
}