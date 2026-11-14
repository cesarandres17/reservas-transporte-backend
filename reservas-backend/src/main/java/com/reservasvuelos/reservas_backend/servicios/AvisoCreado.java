package com.reservasvuelos.reservas_backend.servicios;

public record AvisoCreado(Long codNotificacion, Long codReserva, boolean adjuntarFactura) {
}
