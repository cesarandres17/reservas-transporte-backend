package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.EstadoReserva;
import com.reservasvuelos.reservas_backend.servicios.EstadoReservaServicio;

@RestController
@RequestMapping("/estados-reserva")
public class EstadoReservaControlador extends CrudControlador<EstadoReserva, Long> {

    public EstadoReservaControlador(EstadoReservaServicio estadoReservaServicio) {
        super(estadoReservaServicio);
    }
}
