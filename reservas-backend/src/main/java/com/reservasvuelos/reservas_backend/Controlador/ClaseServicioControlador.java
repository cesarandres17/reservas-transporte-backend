package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.reservasvuelos.reservas_backend.servicios.ClaseServicioServicio;

@Controller 
@RequestMapping("/clase-servicio")
public class ClaseServicioControlador {

    private final ClaseServicioServicio claseServicioServicio;
    
    public ClaseServicioControlador(ClaseServicioServicio claseServicioServicio) {
        this.claseServicioServicio = claseServicioServicio;
    }
    
    
}
