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

import com.reservasvuelos.reservas_backend.servicios.CrudServicio;

public abstract class CrudControlador<T, ID> {

    protected final CrudServicio<T, ID> servicio;

    protected CrudControlador(CrudServicio<T, ID> servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<T> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public T buscar(@PathVariable("id") ID id) {
        return servicio.buscar(id);
    }

    @PostMapping
    public ResponseEntity<T> crear(@RequestBody T entidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(entidad));
    }

    @PutMapping("/{id}")
    public T actualizar(@PathVariable("id") ID id, @RequestBody T entidad) {
        return servicio.actualizar(id, entidad);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") ID id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
