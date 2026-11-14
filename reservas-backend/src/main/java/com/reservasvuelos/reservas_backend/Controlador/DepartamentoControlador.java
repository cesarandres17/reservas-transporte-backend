package com.reservasvuelos.reservas_backend.Controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Departamento;
import com.reservasvuelos.reservas_backend.servicios.DepartamentoServicio;

@RestController
@RequestMapping("/departamentos")
public class DepartamentoControlador extends CrudControlador<Departamento, Long> {

    private final DepartamentoServicio departamentoServicio;

    public DepartamentoControlador(DepartamentoServicio departamentoServicio) {
        super(departamentoServicio);
        this.departamentoServicio = departamentoServicio;
    }

    @GetMapping("/pais/{codPais}")
    public List<Departamento> buscarPorPais(@PathVariable("codPais") Long codPais) {
        return departamentoServicio.buscarPorPais(codPais);
    }
}
