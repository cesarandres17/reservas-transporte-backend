package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.servicios.ClienteServicio;

@RestController
@RequestMapping("/clientes")
public class ClienteControlador extends CrudControlador<Cliente, Long> {

    public ClienteControlador(ClienteServicio clienteServicio) {
        super(clienteServicio);
    }
}
