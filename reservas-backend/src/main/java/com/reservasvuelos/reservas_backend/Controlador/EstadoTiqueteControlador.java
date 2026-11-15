package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.EstadoTiquete;
import com.reservasvuelos.reservas_backend.servicios.EstadoTiqueteServicio;

@RestController
@RequestMapping("/estados-tiquete")
public class EstadoTiqueteControlador extends CrudControlador<EstadoTiquete, Long> {

    public EstadoTiqueteControlador(EstadoTiqueteServicio estadoTiqueteServicio) {
        super(estadoTiqueteServicio);
    }
}
