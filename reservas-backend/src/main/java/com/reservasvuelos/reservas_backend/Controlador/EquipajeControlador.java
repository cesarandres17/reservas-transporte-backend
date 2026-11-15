package com.reservasvuelos.reservas_backend.Controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Equipaje;
import com.reservasvuelos.reservas_backend.servicios.EquipajeServicio;

@RestController
@RequestMapping("/equipajes")
public class EquipajeControlador {

    private final EquipajeServicio servicio;

    public EquipajeControlador(EquipajeServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Equipaje> listar() {
        return servicio.listar();
    }

    @GetMapping("/{codTiquete}/{numeroPieza}")
    public Equipaje buscar(@PathVariable("codTiquete") Long codTiquete,
            @PathVariable("numeroPieza") Integer numeroPieza) {
        return servicio.buscar(EquipajeServicio.id(codTiquete, numeroPieza));
    }

    @PostMapping
    public ResponseEntity<Equipaje> crear(@RequestBody Equipaje equipaje) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(equipaje));
    }

    @PutMapping("/{codTiquete}/{numeroPieza}")
    public Equipaje actualizar(@PathVariable("codTiquete") Long codTiquete,
            @PathVariable("numeroPieza") Integer numeroPieza,
            @RequestBody Equipaje equipaje) {
        return servicio.actualizar(EquipajeServicio.id(codTiquete, numeroPieza), equipaje);
    }

    @DeleteMapping("/{codTiquete}/{numeroPieza}")
    public ResponseEntity<Void> eliminar(@PathVariable("codTiquete") Long codTiquete,
            @PathVariable("numeroPieza") Integer numeroPieza) {
        servicio.eliminar(EquipajeServicio.id(codTiquete, numeroPieza));
        return ResponseEntity.noContent().build();
    }
}
