package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.CiudadRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.RolRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoDocumentoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.UsuarioRepositorio;
import com.reservasvuelos.reservas_backend.dto.RegistroClienteDTO;
import com.reservasvuelos.reservas_backend.dto.UsuarioDTO;
import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.model.Rol;
import com.reservasvuelos.reservas_backend.model.TipoDocumento;
import com.reservasvuelos.reservas_backend.model.Usuario;
import com.reservasvuelos.reservas_backend.model.enums.EstadoUsuario;

@Service
public class AuthServicio {

    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ROL_EMPLEADO = "EMPLEADO";
    public static final String ROL_CLIENTE = "CLIENTE";

    private final UsuarioRepositorio usuarioRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final RolRepositorio rolRepositorio;
    private final TipoDocumentoRepositorio tipoDocumentoRepositorio;
    private final CiudadRepositorio ciudadRepositorio;
    private final PasswordEncoder passwordEncoder;

    public AuthServicio(UsuarioRepositorio usuarioRepositorio, ClienteRepositorio clienteRepositorio,
            RolRepositorio rolRepositorio, TipoDocumentoRepositorio tipoDocumentoRepositorio,
            CiudadRepositorio ciudadRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.tipoDocumentoRepositorio = tipoDocumentoRepositorio;
        this.ciudadRepositorio = ciudadRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioDTO registrarCliente(RegistroClienteDTO datos) {
        String email = datos.email().trim().toLowerCase();
        if (usuarioRepositorio.existeEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con el email " + email);
        }

        TipoDocumento tipoDocumento = tipoDocumentoRepositorio.buscarPorSigla(datos.siglaTipoDocumento())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe el tipo de documento " + datos.siglaTipoDocumento()));
        if (usuarioRepositorio.existeDocumento(tipoDocumento.getCodTipo(), datos.numeroDocumento())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese documento");
        }

        Ciudad ciudad = null;
        if (datos.codCiudad() != null) {
            ciudad = ciudadRepositorio.buscarPorId(datos.codCiudad())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "No existe la ciudad " + datos.codCiudad()));
        }

        Rol rolCliente = rolRepositorio.buscarPorNombre(ROL_CLIENTE)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + ROL_CLIENTE + " en la tabla rol"));

        Usuario usuario = new Usuario();
        usuario.setTipoDocumento(tipoDocumento);
        usuario.setNumeroDocumento(datos.numeroDocumento());
        usuario.setPrimerNombre(datos.primerNombre());
        usuario.setSegundoNombre(datos.segundoNombre());
        usuario.setPrimerApellido(datos.primerApellido());
        usuario.setSegundoApellido(datos.segundoApellido());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(datos.password()));
        usuario.setTelefono(datos.telefono());
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setDireccion(datos.direccion());
        usuario.setCiudad(ciudad);
        usuario.setFechaRegistro(LocalDate.now());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRol(rolCliente);
        usuarioRepositorio.insertar(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setRecibeNotificaciones(datos.recibeNotificaciones() == null || datos.recibeNotificaciones());
        clienteRepositorio.insertar(cliente);

        return UsuarioDTO.desde(usuario);
    }

    public UsuarioDTO perfil(String email) {
        return usuarioRepositorio.buscarPorEmail(email)
                .map(UsuarioDTO::desde)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el usuario " + email));
    }
}
