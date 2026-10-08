package com.event_pass.ordenes.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.event_pass.ordenes.model.EstadoOrden;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class OrdenResponseDTO {
    private Long ordenId;
    private Long usuarioId;
    private Long eventoId;
    private Integer cantidad;
    private EstadoOrden estado;
    private LocalDateTime fecha;
    private String motivo;
    private List<TicketDTO> tickets;
}
