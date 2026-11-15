package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.dto.RegistroClienteDTO;
import com.reservasvuelos.reservas_backend.dto.UsuarioDTO;
import com.reservasvuelos.reservas_backend.servicios.AuthServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthControlador {

    private final AuthServicio authServicio;

    public AuthControlador(AuthServicio authServicio) {
        this.authServicio = authServicio;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registrar(@Valid @RequestBody RegistroClienteDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authServicio.registrarCliente(datos));
    }

    @GetMapping("/perfil")
    public UsuarioDTO perfil(Authentication autenticacion) {
        return authServicio.perfil(autenticacion.getName());
    }
}
