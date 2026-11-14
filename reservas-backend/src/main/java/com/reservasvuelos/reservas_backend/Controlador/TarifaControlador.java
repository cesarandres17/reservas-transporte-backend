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

import com.reservasvuelos.reservas_backend.model.Tarifa;
import com.reservasvuelos.reservas_backend.servicios.TarifaServicio;

@RestController
@RequestMapping("/tarifas")
public class TarifaControlador {

    private final TarifaServicio servicio;

    public TarifaControlador(TarifaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Tarifa> listar() {
        return servicio.listar();
    }

    @GetMapping("/{codViaje}/{codClase}")
    public Tarifa buscar(@PathVariable("codViaje") Long codViaje, @PathVariable("codClase") Long codClase) {
        return servicio.buscar(TarifaServicio.id(codViaje, codClase));
    }

    @PostMapping
    public ResponseEntity<Tarifa> crear(@RequestBody Tarifa tarifa) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(tarifa));
    }

    @PutMapping("/{codViaje}/{codClase}")
    public Tarifa actualizar(@PathVariable("codViaje") Long codViaje,
            @PathVariable("codClase") Long codClase,
            @RequestBody Tarifa tarifa) {
        return servicio.actualizar(TarifaServicio.id(codViaje, codClase), tarifa);
    }

    @DeleteMapping("/{codViaje}/{codClase}")
    public ResponseEntity<Void> eliminar(@PathVariable("codViaje") Long codViaje,
            @PathVariable("codClase") Long codClase) {
        servicio.eliminar(TarifaServicio.id(codViaje, codClase));
        return ResponseEntity.noContent().build();
    }
}
