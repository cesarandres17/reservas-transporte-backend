package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.servicios.ReservaServicio;

@RestController
@RequestMapping("/reservas")
public class ReservaControlador extends CrudControlador<Reserva, Long> {

    public ReservaControlador(ReservaServicio reservaServicio) {
        super(reservaServicio);
    }
}
