package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.CiudadRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.RolRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoDocumentoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.UsuarioRepositorio;
import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.model.Rol;
import com.reservasvuelos.reservas_backend.model.TipoDocumento;
import com.reservasvuelos.reservas_backend.model.Usuario;
import com.reservasvuelos.reservas_backend.model.enums.EstadoUsuario;

@Service
public class UsuarioServicio extends CrudServicio<Usuario, Long> {

    private final TipoDocumentoRepositorio tipoDocumentoRepositorio;
    private final CiudadRepositorio ciudadRepositorio;
    private final RolRepositorio rolRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicio(UsuarioRepositorio repositorio,
            TipoDocumentoRepositorio tipoDocumentoRepositorio,
            CiudadRepositorio ciudadRepositorio,
            RolRepositorio rolRepositorio,
            PasswordEncoder passwordEncoder) {
        super(repositorio, "el usuario");
        this.tipoDocumentoRepositorio = tipoDocumentoRepositorio;
        this.ciudadRepositorio = ciudadRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void asignarId(Usuario usuario, Long id) {
        usuario.setCodUsuario(id);
    }

    @Override
    protected void completar(Usuario usuario, Usuario actual) {
        usuario.setTipoDocumento(referencia(usuario.getTipoDocumento(), TipoDocumento::getCodTipo,
                tipoDocumentoRepositorio, "el tipo de documento"));
        usuario.setCiudad(referencia(usuario.getCiudad(), Ciudad::getCodCiudad, ciudadRepositorio, "la ciudad"));
        usuario.setRol(referencia(usuario.getRol(), Rol::getCodRol, rolRepositorio, "el rol"));
        if (usuario.getEmail() != null) {
            usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        }
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        } else if (actual != null) {
            usuario.setPassword(actual.getPassword());
        }
        if (usuario.getFechaRegistro() == null) {
            usuario.setFechaRegistro(actual == null ? LocalDate.now() : actual.getFechaRegistro());
        }
        if (usuario.getEstado() == null) {
            usuario.setEstado(actual == null ? EstadoUsuario.ACTIVO : actual.getEstado());
        }
    }
}
