package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.servicios.RolServicio;

@RestController 
@RequestMapping ("/api/roles")
public class RolControlador  {
    private final RolServicio rolServicio;
    
    public RolControlador(RolServicio rolServicio) {
        this.rolServicio = rolServicio;
    }
    
}
