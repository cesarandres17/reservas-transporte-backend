package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.servicios.ClaseServicioServicio;

@RestController
@RequestMapping("/clase-servicio")
public class ClaseServicioControlador {

    private final ClaseServicioServicio claseServicioServicio;

    public ClaseServicioControlador(ClaseServicioServicio claseServicioServicio) {
        this.claseServicioServicio = claseServicioServicio;
    }


}
