package com.event_pass.ordenes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class TicketEmisionRequestDTO {
    private Long ordenId;
    private Long usuarioId;
    private Long eventoId;
    private Integer cantidad;
    
}
