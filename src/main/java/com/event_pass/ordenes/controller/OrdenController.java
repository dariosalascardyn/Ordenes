package com.event_pass.ordenes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_pass.ordenes.dto.CrearOrdenRequestDTO;
import com.event_pass.ordenes.dto.OrdenResponseDTO;
import com.event_pass.ordenes.model.EstadoOrden;
import com.event_pass.ordenes.service.OrdenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;

    @PostMapping
    public ResponseEntity<OrdenResponseDTO> crearOrden(@RequestBody CrearOrdenRequestDTO request) {
        OrdenResponseDTO response = ordenService.crearOrden(request);
        if (response.getEstado() == EstadoOrden.RECHAZADA) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ordenId}")
    public ResponseEntity<OrdenResponseDTO> obtenerOrden(@PathVariable Long ordenId) {
        return ResponseEntity.ok(ordenService.obtenerOrden(ordenId));
    }
}
