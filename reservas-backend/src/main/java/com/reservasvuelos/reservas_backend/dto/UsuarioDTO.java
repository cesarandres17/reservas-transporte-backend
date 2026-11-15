package com.reservasvuelos.reservas_backend.dto;

import com.reservasvuelos.reservas_backend.model.Usuario;

public record UsuarioDTO(
        Long codUsuario,
        String primerNombre,
        String primerApellido,
        String email,
        String rol) {

    public static UsuarioDTO desde(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getCodUsuario(),
                usuario.getPrimerNombre(),
                usuario.getPrimerApellido(),
                usuario.getEmail(),
                usuario.getRol().getNombre());
    }
}
