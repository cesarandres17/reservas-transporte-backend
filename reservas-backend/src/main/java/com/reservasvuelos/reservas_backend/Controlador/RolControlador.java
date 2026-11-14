package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Rol;
import com.reservasvuelos.reservas_backend.servicios.RolServicio;

@RestController
@RequestMapping("/roles")
public class RolControlador extends CrudControlador<Rol, Long> {

    public RolControlador(RolServicio rolServicio) {
        super(rolServicio);
    }
}
