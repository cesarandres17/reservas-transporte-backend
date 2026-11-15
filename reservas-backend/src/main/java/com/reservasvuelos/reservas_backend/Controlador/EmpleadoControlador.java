package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Empleado;
import com.reservasvuelos.reservas_backend.servicios.EmpleadoServicio;

@RestController
@RequestMapping("/empleados")
public class EmpleadoControlador extends CrudControlador<Empleado, Long> {

    public EmpleadoControlador(EmpleadoServicio empleadoServicio) {
        super(empleadoServicio);
    }
}
