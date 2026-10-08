package com.event_pass.ordenes.dto;

import java.util.List;

import lombok.Data;

@Data
public class EmisionTicketsResponseDTO {
    private Long ordenId;
    private List<TicketDTO> tickets;
}