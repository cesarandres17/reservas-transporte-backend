package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.ClienteNotificacionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EstadoNotificacionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.NotificacionRepositorio;
import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.model.ClienteNotificacion;
import com.reservasvuelos.reservas_backend.model.ClienteNotificacionId;
import com.reservasvuelos.reservas_backend.model.EstadoNotificacion;
import com.reservasvuelos.reservas_backend.model.Notificacion;

@Service
public class ClienteNotificacionServicio extends CrudServicio<ClienteNotificacion, ClienteNotificacionId> {

    private final ClienteRepositorio clienteRepositorio;
    private final NotificacionRepositorio notificacionRepositorio;
    private final EstadoNotificacionRepositorio estadoNotificacionRepositorio;

    public ClienteNotificacionServicio(ClienteNotificacionRepositorio repositorio,
            ClienteRepositorio clienteRepositorio,
            NotificacionRepositorio notificacionRepositorio,
            EstadoNotificacionRepositorio estadoNotificacionRepositorio) {
        super(repositorio, "la notificación del cliente");
        this.clienteRepositorio = clienteRepositorio;
        this.notificacionRepositorio = notificacionRepositorio;
        this.estadoNotificacionRepositorio = estadoNotificacionRepositorio;
    }

    public static ClienteNotificacionId id(Long codCliente, Long codNotificacion) {
        ClienteNotificacionId id = new ClienteNotificacionId();
        id.setCliente(codCliente);
        id.setNotificacion(codNotificacion);
        return id;
    }

    @Override
    protected void asignarId(ClienteNotificacion clienteNotificacion, ClienteNotificacionId id) {
    }

    @Override
    protected void completar(ClienteNotificacion clienteNotificacion, ClienteNotificacion actual) {
        if (actual != null) {
            clienteNotificacion.setCliente(actual.getCliente());
            clienteNotificacion.setNotificacion(actual.getNotificacion());
        } else {
            clienteNotificacion.setCliente(obligatorio(referencia(clienteNotificacion.getCliente(),
                    Cliente::getCodCliente, clienteRepositorio, "el cliente"), "el cliente"));
            clienteNotificacion.setNotificacion(obligatorio(referencia(clienteNotificacion.getNotificacion(),
                    Notificacion::getCodNotificacion, notificacionRepositorio, "la notificación"), "la notificación"));
        }
        clienteNotificacion.setEstadoNotificacion(referencia(clienteNotificacion.getEstadoNotificacion(),
                EstadoNotificacion::getCodEstado, estadoNotificacionRepositorio, "el estado de notificación"));
    }

    @Override
    protected void validarNuevo(ClienteNotificacion clienteNotificacion) {
        rechazarSiExiste(id(clienteNotificacion.getCliente().getCodCliente(),
                clienteNotificacion.getNotificacion().getCodNotificacion()));
    }
}
