package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Usuario;
import com.reservasvuelos.reservas_backend.servicios.UsuarioServicio;

@RestController
@RequestMapping("/usuarios")
public class UsuarioControlador extends CrudControlador<Usuario, Long> {

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        super(usuarioServicio);
    }
}
