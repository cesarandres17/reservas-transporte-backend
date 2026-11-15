package com.reservasvuelos.reservas_backend.dto;

import java.time.LocalDateTime;

import com.reservasvuelos.reservas_backend.model.ClienteNotificacion;
import com.reservasvuelos.reservas_backend.model.Notificacion;

public record NotificacionClienteDTO(
        Long codNotificacion,
        String tipo,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        String estado,
        boolean leida,
        LocalDateTime fechaEnvio,
        LocalDateTime fechaLectura,
        Long codViaje,
        Long codPromocion) {

    public static NotificacionClienteDTO desde(ClienteNotificacion clienteNotificacion) {
        Notificacion notificacion = clienteNotificacion.getNotificacion();
        String estado = clienteNotificacion.getEstadoNotificacion().getNombre();
        return new NotificacionClienteDTO(
                notificacion.getCodNotificacion(),
                notificacion.getTipoNotificacion().getNombre(),
                notificacion.getTitulo(),
                notificacion.getMensaje(),
                notificacion.getFechaCreacion(),
                estado,
                clienteNotificacion.getFechaLectura() != null,
                clienteNotificacion.getFechaEnvio(),
                clienteNotificacion.getFechaLectura(),
                notificacion.getViaje() == null ? null : notificacion.getViaje().getCodViaje(),
                notificacion.getPromocion() == null ? null : notificacion.getPromocion().getCodPromocion());
    }
}
