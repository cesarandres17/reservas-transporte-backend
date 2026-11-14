package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.dto.EnvioPromocionDTO;
import com.reservasvuelos.reservas_backend.model.Notificacion;
import com.reservasvuelos.reservas_backend.servicios.NotificacionServicio;
import com.reservasvuelos.reservas_backend.servicios.NotificadorServicio;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionControlador extends CrudControlador<Notificacion, Long> {

    private final NotificadorServicio notificadorServicio;

    public NotificacionControlador(NotificacionServicio notificacionServicio,
            NotificadorServicio notificadorServicio) {
        super(notificacionServicio);
        this.notificadorServicio = notificadorServicio;
    }

    @PostMapping("/promociones/{codPromocion}")
    @ResponseStatus(HttpStatus.CREATED)
    public EnvioPromocionDTO enviarPromocion(@PathVariable Long codPromocion) {
        return notificadorServicio.promocion(codPromocion);
    }
}
