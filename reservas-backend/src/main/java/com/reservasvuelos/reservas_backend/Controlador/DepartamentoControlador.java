package com.reservasvuelos.reservas_backend.Controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Departamento;
import com.reservasvuelos.reservas_backend.servicios.DepartamentoServicio;


@RestController
@RequestMapping ("/departamentos")
public class DepartamentoControlador {

    @Autowired
    private DepartamentoServicio departamentoServicio;

    @GetMapping
    public ResponseEntity<List<Departamento>> getAllDepartamentos() {
        return ResponseEntity.ok(departamentoServicio.buscarTodos());
    }

    @GetMapping("/pais/{codPais}")
    public ResponseEntity<List<Departamento>> obtenerDepartamentosPorPais(@PathVariable Long codPais) {
      return ResponseEntity.ok(departamentoServicio.buscarPorPais(codPais));
    }




}
