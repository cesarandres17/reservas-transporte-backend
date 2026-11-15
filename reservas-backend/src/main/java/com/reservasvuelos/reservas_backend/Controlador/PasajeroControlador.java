package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.servicios.PasajeroServicio;

@RestController
@RequestMapping("/pasajeros")
public class PasajeroControlador extends CrudControlador<Pasajero, Long> {

    public PasajeroControlador(PasajeroServicio pasajeroServicio) {
        super(pasajeroServicio);
    }
}
