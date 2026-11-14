package com.reservasvuelos.reservas_backend.Controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.servicios.CiudadServicio;

@RestController
@RequestMapping("/ciudades")
public class CiudadControlador extends CrudControlador<Ciudad, Long> {

    private final CiudadServicio ciudadServicio;

    public CiudadControlador(CiudadServicio ciudadServicio) {
        super(ciudadServicio);
        this.ciudadServicio = ciudadServicio;
    }

    @GetMapping("/departamento/{codDepartamento}")
    public List<Ciudad> buscarPorDepartamento(@PathVariable("codDepartamento") Long codDepartamento) {
        return ciudadServicio.buscarPorDepartamento(codDepartamento);
    }
}
