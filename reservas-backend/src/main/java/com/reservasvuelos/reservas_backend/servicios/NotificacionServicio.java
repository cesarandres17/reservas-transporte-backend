package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.NotificacionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PromocionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoNotificacionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ViajeRepositorio;
import com.reservasvuelos.reservas_backend.model.Notificacion;
import com.reservasvuelos.reservas_backend.model.Promocion;
import com.reservasvuelos.reservas_backend.model.TipoNotificacion;
import com.reservasvuelos.reservas_backend.model.Viaje;

@Service
public class NotificacionServicio extends CrudServicio<Notificacion, Long> {

    private final TipoNotificacionRepositorio tipoNotificacionRepositorio;
    private final PromocionRepositorio promocionRepositorio;
    private final ViajeRepositorio viajeRepositorio;

    public NotificacionServicio(NotificacionRepositorio repositorio,
            TipoNotificacionRepositorio tipoNotificacionRepositorio,
            PromocionRepositorio promocionRepositorio,
            ViajeRepositorio viajeRepositorio) {
        super(repositorio, "la notificación");
        this.tipoNotificacionRepositorio = tipoNotificacionRepositorio;
        this.promocionRepositorio = promocionRepositorio;
        this.viajeRepositorio = viajeRepositorio;
    }

    @Override
    protected void asignarId(Notificacion notificacion, Long id) {
        notificacion.setCodNotificacion(id);
    }

    @Override
    protected void completar(Notificacion notificacion, Notificacion actual) {
        notificacion.setTipoNotificacion(referencia(notificacion.getTipoNotificacion(), TipoNotificacion::getCodTipo,
                tipoNotificacionRepositorio, "el tipo de notificación"));
        notificacion.setPromocion(referencia(notificacion.getPromocion(), Promocion::getCodPromocion,
                promocionRepositorio, "la promoción"));
        notificacion.setViaje(referencia(notificacion.getViaje(), Viaje::getCodViaje, viajeRepositorio, "el viaje"));
        if (notificacion.getFechaCreacion() == null) {
            notificacion.setFechaCreacion(actual == null ? LocalDateTime.now() : actual.getFechaCreacion());
        }
    }
}
