package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.TipoNotificacion;
import com.reservasvuelos.reservas_backend.servicios.TipoNotificacionServicio;

@RestController
@RequestMapping("/tipos-notificacion")
public class TipoNotificacionControlador extends CrudControlador<TipoNotificacion, Long> {

    public TipoNotificacionControlador(TipoNotificacionServicio tipoNotificacionServicio) {
        super(tipoNotificacionServicio);
    }
}
