package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.EstadoViaje;
import com.reservasvuelos.reservas_backend.servicios.EstadoViajeServicio;

@RestController
@RequestMapping("/estados-viaje")
public class EstadoViajeControlador extends CrudControlador<EstadoViaje, Long> {

    public EstadoViajeControlador(EstadoViajeServicio estadoViajeServicio) {
        super(estadoViajeServicio);
    }
}
