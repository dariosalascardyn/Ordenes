package com.event_pass.ordenes.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.event_pass.ordenes.dto.ReservaRequestDTO;
import com.event_pass.ordenes.exception.ServicioExternoException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventosClient {

    private final RestTemplate restTemplate;

    @Value("${servicios.eventos.url:http://localhost:8084}")
    private String eventosUrl;

    public boolean reservarAforo(Long eventoId, Long ordenId, Integer cantidad) {
        String url = eventosUrl + "/interno/eventos/" + eventoId + "/reservas";
        ReservaRequestDTO request = new ReservaRequestDTO(ordenId, cantidad);

        try {
            return restTemplate.postForEntity(url, request, String.class)
                .getStatusCode()
                .is2xxSuccessful();
        } catch (HttpStatusCodeException exception) {
            if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                return false;
            }
            throw new ServicioExternoException("ERROR_RESERVA_EVENTOS", exception);
        } catch (RestClientException exception) {
            throw new ServicioExternoException("ERROR_RESERVA_EVENTOS", exception);
        }
    }
}
