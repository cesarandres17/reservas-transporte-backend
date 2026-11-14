package com.reservasvuelos.reservas_backend.servicios;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.UsuarioRepositorio;
import com.reservasvuelos.reservas_backend.model.Usuario;

@Component
public class UsuarioActual {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioActual(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Optional<Usuario> buscar(Authentication autenticacion) {
        if (autenticacion == null || autenticacion instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return usuarioRepositorio.findByEmailIgnoreCase(autenticacion.getName());
    }

    public Usuario obtener(Authentication autenticacion) {
        return buscar(autenticacion).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión para continuar"));
    }

    public boolean esPersonal(Authentication autenticacion) {
        return autenticacion != null && autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> rol.equals("ROLE_" + AuthServicio.ROL_ADMINISTRADOR)
                        || rol.equals("ROLE_" + AuthServicio.ROL_EMPLEADO));
    }
}
