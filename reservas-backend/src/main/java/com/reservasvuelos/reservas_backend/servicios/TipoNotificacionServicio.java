package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.TipoNotificacionRepositorio;
import com.reservasvuelos.reservas_backend.model.TipoNotificacion;

@Service
public class TipoNotificacionServicio extends CrudServicio<TipoNotificacion, Long> {

    public TipoNotificacionServicio(TipoNotificacionRepositorio repositorio) {
        super(repositorio, "el tipo de notificación");
    }

    @Override
    protected void asignarId(TipoNotificacion tipoNotificacion, Long id) {
        tipoNotificacion.setCodTipo(id);
    }
}
