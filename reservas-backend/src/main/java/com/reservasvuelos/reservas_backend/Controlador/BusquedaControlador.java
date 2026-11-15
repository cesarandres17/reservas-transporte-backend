package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Busqueda;
import com.reservasvuelos.reservas_backend.servicios.BusquedaServicio;

@RestController
@RequestMapping("/busquedas")
public class BusquedaControlador extends CrudControlador<Busqueda, Long> {

    public BusquedaControlador(BusquedaServicio busquedaServicio) {
        super(busquedaServicio);
    }
}
