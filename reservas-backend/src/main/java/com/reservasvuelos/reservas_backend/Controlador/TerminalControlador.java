package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Terminal;
import com.reservasvuelos.reservas_backend.servicios.TerminalServicio;

@RestController
@RequestMapping("/terminales")
public class TerminalControlador extends CrudControlador<Terminal, Long> {

    public TerminalControlador(TerminalServicio terminalServicio) {
        super(terminalServicio);
    }
}
