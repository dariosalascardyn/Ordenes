package com.event_pass.ordenes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class ReservaRequestDTO {
    private Long ordenId;
    private Integer cantidad;
    
}
