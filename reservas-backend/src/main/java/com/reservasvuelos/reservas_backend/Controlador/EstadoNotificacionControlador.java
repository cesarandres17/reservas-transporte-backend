package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.EstadoNotificacion;
import com.reservasvuelos.reservas_backend.servicios.EstadoNotificacionServicio;

@RestController
@RequestMapping("/estados-notificacion")
public class EstadoNotificacionControlador extends CrudControlador<EstadoNotificacion, Long> {

    public EstadoNotificacionControlador(EstadoNotificacionServicio estadoNotificacionServicio) {
        super(estadoNotificacionServicio);
    }
}
