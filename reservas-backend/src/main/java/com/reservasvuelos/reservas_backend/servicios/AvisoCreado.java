package com.reservasvuelos.reservas_backend.servicios;

public record AvisoCreado(Long codNotificacion, Long codReserva, Adjunto adjunto) {

    public enum Adjunto {
        NINGUNO,
        FACTURA,
        PASES_ABORDAR
    }
}
