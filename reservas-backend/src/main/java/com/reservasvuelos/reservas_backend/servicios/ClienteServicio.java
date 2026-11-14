package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.UsuarioRepositorio;
import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.model.Usuario;

@Service
public class ClienteServicio extends CrudServicio<Cliente, Long> {

    private final UsuarioRepositorio usuarioRepositorio;

    public ClienteServicio(ClienteRepositorio repositorio, UsuarioRepositorio usuarioRepositorio) {
        super(repositorio, "el cliente");
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    protected void asignarId(Cliente cliente, Long id) {
        cliente.setCodCliente(id);
    }

    @Override
    protected void completar(Cliente cliente, Cliente actual) {
        if (actual != null) {
            cliente.setUsuario(actual.getUsuario());
        } else {
            cliente.setUsuario(obligatorio(referencia(cliente.getUsuario(), Usuario::getCodUsuario,
                    usuarioRepositorio, "el usuario"), "el usuario"));
        }
    }

    @Override
    protected void validarNuevo(Cliente cliente) {
        rechazarSiExiste(cliente.getUsuario().getCodUsuario());
    }
}
