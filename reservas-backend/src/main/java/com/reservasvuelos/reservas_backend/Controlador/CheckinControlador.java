package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Checkin;
import com.reservasvuelos.reservas_backend.servicios.CheckinServicio;

@RestController
@RequestMapping("/checkins")
public class CheckinControlador extends CrudControlador<Checkin, Long> {

    public CheckinControlador(CheckinServicio checkinServicio) {
        super(checkinServicio);
    }
}
