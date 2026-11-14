package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.ClienteNotificacionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.dto.NotificacionClienteDTO;
import com.reservasvuelos.reservas_backend.model.ClienteNotificacion;
import com.reservasvuelos.reservas_backend.model.EstadoNotificacion;

@Service
public class BandejaServicio {

    private final ClienteNotificacionRepositorio clienteNotificacionRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final UsuarioActual usuarioActual;
    private final Catalogos catalogos;

    public BandejaServicio(ClienteNotificacionRepositorio clienteNotificacionRepositorio,
            ClienteRepositorio clienteRepositorio, UsuarioActual usuarioActual, Catalogos catalogos) {
        this.clienteNotificacionRepositorio = clienteNotificacionRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.usuarioActual = usuarioActual;
        this.catalogos = catalogos;
    }

    @Transactional(readOnly = true)
    public List<NotificacionClienteDTO> mias(Authentication autenticacion) {
        Long codCliente = codCliente(autenticacion);
        if (codCliente == null) {
            return List.of();
        }
        return clienteNotificacionRepositorio.deCliente(codCliente, LocalDateTime.now()).stream()
                .map(NotificacionClienteDTO::desde)
                .toList();
    }

    @Transactional
    public NotificacionClienteDTO marcarLeida(Long codNotificacion, Authentication autenticacion) {
        Long codCliente = codCliente(autenticacion);
        ClienteNotificacion fila = codCliente == null ? null : clienteNotificacionRepositorio
                .findById(ClienteNotificacionServicio.id(codCliente, codNotificacion)).orElse(null);
        if (fila == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No tienes la notificación " + codNotificacion);
        }
        leer(fila, catalogos.estadoNotificacion("LEIDA"), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return NotificacionClienteDTO.desde(fila);
    }

    @Transactional
    public int marcarTodas(Authentication autenticacion) {
        Long codCliente = codCliente(autenticacion);
        if (codCliente == null) {
            return 0;
        }
        EstadoNotificacion leida = catalogos.estadoNotificacion("LEIDA");
        LocalDateTime ahora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        int marcadas = 0;
        for (ClienteNotificacion fila : clienteNotificacionRepositorio.deCliente(codCliente, ahora)) {
            if (fila.getFechaLectura() == null) {
                leer(fila, leida, ahora);
                marcadas++;
            }
        }
        return marcadas;
    }

    private void leer(ClienteNotificacion fila, EstadoNotificacion leida, LocalDateTime ahora) {
        if (fila.getFechaLectura() == null) {
            fila.setEstadoNotificacion(leida);
            fila.setFechaLectura(ahora);
        }
    }

    private Long codCliente(Authentication autenticacion) {
        Long codUsuario = usuarioActual.obtener(autenticacion).getCodUsuario();
        return clienteRepositorio.existsById(codUsuario) ? codUsuario : null;
    }
}
