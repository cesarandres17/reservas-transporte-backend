package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Ruta;
import com.reservasvuelos.reservas_backend.servicios.RutaServicio;

@RestController
@RequestMapping("/rutas")
public class RutaControlador extends CrudControlador<Ruta, Long> {

    public RutaControlador(RutaServicio rutaServicio) {
        super(rutaServicio);
    }
}
