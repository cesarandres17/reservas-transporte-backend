package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Notificacion;
import com.reservasvuelos.reservas_backend.servicios.NotificacionServicio;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionControlador extends CrudControlador<Notificacion, Long> {

    public NotificacionControlador(NotificacionServicio notificacionServicio) {
        super(notificacionServicio);
    }
}
