package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.ClaseServicio;
import com.reservasvuelos.reservas_backend.servicios.ClaseServicioServicio;

@RestController
@RequestMapping("/clases-servicio")
public class ClaseServicioControlador extends CrudControlador<ClaseServicio, Long> {

    public ClaseServicioControlador(ClaseServicioServicio claseServicioServicio) {
        super(claseServicioServicio);
    }
}
