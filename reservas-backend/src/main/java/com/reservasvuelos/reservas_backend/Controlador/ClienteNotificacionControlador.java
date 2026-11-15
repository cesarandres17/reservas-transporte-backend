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

import com.reservasvuelos.reservas_backend.model.ClienteNotificacion;
import com.reservasvuelos.reservas_backend.servicios.ClienteNotificacionServicio;

@RestController
@RequestMapping("/clientes-notificaciones")
public class ClienteNotificacionControlador {

    private final ClienteNotificacionServicio servicio;

    public ClienteNotificacionControlador(ClienteNotificacionServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ClienteNotificacion> listar() {
        return servicio.listar();
    }

    @GetMapping("/{codCliente}/{codNotificacion}")
    public ClienteNotificacion buscar(@PathVariable("codCliente") Long codCliente,
            @PathVariable("codNotificacion") Long codNotificacion) {
        return servicio.buscar(ClienteNotificacionServicio.id(codCliente, codNotificacion));
    }

    @PostMapping
    public ResponseEntity<ClienteNotificacion> crear(@RequestBody ClienteNotificacion clienteNotificacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(clienteNotificacion));
    }

    @PutMapping("/{codCliente}/{codNotificacion}")
    public ClienteNotificacion actualizar(@PathVariable("codCliente") Long codCliente,
            @PathVariable("codNotificacion") Long codNotificacion,
            @RequestBody ClienteNotificacion clienteNotificacion) {
        return servicio.actualizar(ClienteNotificacionServicio.id(codCliente, codNotificacion), clienteNotificacion);
    }

    @DeleteMapping("/{codCliente}/{codNotificacion}")
    public ResponseEntity<Void> eliminar(@PathVariable("codCliente") Long codCliente,
            @PathVariable("codNotificacion") Long codNotificacion) {
        servicio.eliminar(ClienteNotificacionServicio.id(codCliente, codNotificacion));
        return ResponseEntity.noContent().build();
    }
}
