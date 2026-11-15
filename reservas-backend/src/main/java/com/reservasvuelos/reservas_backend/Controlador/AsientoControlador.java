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

import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.servicios.AsientoServicio;

@RestController
@RequestMapping("/asientos")
public class AsientoControlador {

    private final AsientoServicio servicio;

    public AsientoControlador(AsientoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Asiento> listar() {
        return servicio.listar();
    }

    @GetMapping("/{codVehiculo}/{numeroAsiento}")
    public Asiento buscar(@PathVariable("codVehiculo") Long codVehiculo,
            @PathVariable("numeroAsiento") String numeroAsiento) {
        return servicio.buscar(AsientoServicio.id(codVehiculo, numeroAsiento));
    }

    @PostMapping
    public ResponseEntity<Asiento> crear(@RequestBody Asiento asiento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(asiento));
    }

    @PutMapping("/{codVehiculo}/{numeroAsiento}")
    public Asiento actualizar(@PathVariable("codVehiculo") Long codVehiculo,
            @PathVariable("numeroAsiento") String numeroAsiento,
            @RequestBody Asiento asiento) {
        return servicio.actualizar(AsientoServicio.id(codVehiculo, numeroAsiento), asiento);
    }

    @DeleteMapping("/{codVehiculo}/{numeroAsiento}")
    public ResponseEntity<Void> eliminar(@PathVariable("codVehiculo") Long codVehiculo,
            @PathVariable("numeroAsiento") String numeroAsiento) {
        servicio.eliminar(AsientoServicio.id(codVehiculo, numeroAsiento));
        return ResponseEntity.noContent().build();
    }
}
