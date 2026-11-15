package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.EstadoNotificacionRepositorio;
import com.reservasvuelos.reservas_backend.model.EstadoNotificacion;

@Service
public class EstadoNotificacionServicio extends CrudServicio<EstadoNotificacion, Long> {

    public EstadoNotificacionServicio(EstadoNotificacionRepositorio repositorio) {
        super(repositorio, "el estado de notificación");
    }

    @Override
    protected void asignarId(EstadoNotificacion estadoNotificacion, Long id) {
        estadoNotificacion.setCodEstado(id);
    }
}
