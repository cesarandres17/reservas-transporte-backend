package com.reservasvuelos.reservas_backend.config;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.reservasvuelos.reservas_backend.Repositorio.RolRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoDocumentoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.UsuarioRepositorio;
import com.reservasvuelos.reservas_backend.model.Rol;
import com.reservasvuelos.reservas_backend.model.TipoDocumento;
import com.reservasvuelos.reservas_backend.model.Usuario;
import com.reservasvuelos.reservas_backend.model.enums.EstadoUsuario;
import com.reservasvuelos.reservas_backend.servicios.AuthServicio;

@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final RolRepositorio rolRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final TipoDocumentoRepositorio tipoDocumentoRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String emailAdmin;

    @Value("${app.admin.password}")
    private String passwordAdmin;

    public DatosIniciales(RolRepositorio rolRepositorio, UsuarioRepositorio usuarioRepositorio,
            TipoDocumentoRepositorio tipoDocumentoRepositorio, PasswordEncoder passwordEncoder) {
        this.rolRepositorio = rolRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.tipoDocumentoRepositorio = tipoDocumentoRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Rol administrador = crearRolSiNoExiste(AuthServicio.ROL_ADMINISTRADOR, "Administra todo el sistema");
        crearRolSiNoExiste(AuthServicio.ROL_EMPLEADO, "Atiende reservas, pagos y check-in");
        crearRolSiNoExiste(AuthServicio.ROL_CLIENTE, "Busca viajes, reserva y paga");

        if (!usuarioRepositorio.existsByRol_Nombre(AuthServicio.ROL_ADMINISTRADOR)) {
            crearAdministrador(administrador);
        }
    }

    private Rol crearRolSiNoExiste(String nombre, String descripcion) {
        return rolRepositorio.findByNombre(nombre).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rol.setDescripcion(descripcion);
            log.info("Se creo el rol {}", nombre);
            return rolRepositorio.save(rol);
        });
    }

    private void crearAdministrador(Rol administrador) {
        TipoDocumento cedula = tipoDocumentoRepositorio.findBySiglaIgnoreCase("CC").orElseGet(() -> {
            TipoDocumento tipo = new TipoDocumento();
            tipo.setSigla("CC");
            tipo.setNombre("Cédula de ciudadanía");
            return tipoDocumentoRepositorio.save(tipo);
        });

        Usuario admin = new Usuario();
        admin.setTipoDocumento(cedula);
        admin.setNumeroDocumento("1000000000");
        admin.setPrimerNombre("Administrador");
        admin.setPrimerApellido("Sistema");
        admin.setEmail(emailAdmin);
        admin.setPassword(passwordEncoder.encode(passwordAdmin));
        admin.setFechaRegistro(LocalDate.now());
        admin.setEstado(EstadoUsuario.ACTIVO);
        admin.setRol(administrador);
        usuarioRepositorio.save(admin);
        log.info("Se creo el usuario administrador {}", emailAdmin);
    }
}
