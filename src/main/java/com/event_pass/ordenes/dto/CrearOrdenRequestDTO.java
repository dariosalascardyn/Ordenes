package com.event_pass.ordenes.dto;

import lombok.Data;

@Data 
public class CrearOrdenRequestDTO {
    private  Long usuarioId;
    private  Long eventoId;
    private  Integer cantidad;
    
}
