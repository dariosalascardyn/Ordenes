package com.event_pass.ordenes.exception;

public class ServicioExternoException extends RuntimeException {

    private final String codigo;

    public ServicioExternoException(String codigo, Throwable causa) {
        super(codigo, causa);
        this.codigo = codigo;
    }

    public ServicioExternoException(String codigo) {
        super(codigo);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
