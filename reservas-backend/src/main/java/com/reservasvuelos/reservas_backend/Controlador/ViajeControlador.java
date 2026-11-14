package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Viaje;
import com.reservasvuelos.reservas_backend.servicios.ViajeServicio;

@RestController
@RequestMapping("/viajes")
public class ViajeControlador extends CrudControlador<Viaje, Long> {

    public ViajeControlador(ViajeServicio viajeServicio) {
        super(viajeServicio);
    }
}
