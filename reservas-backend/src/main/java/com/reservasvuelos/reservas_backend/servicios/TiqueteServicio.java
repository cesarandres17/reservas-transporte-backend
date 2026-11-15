package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.AsientoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EstadoTiqueteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PasajeroRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ReservaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TarifaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoTrayectoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TiqueteRepositorio;
import com.reservasvuelos.reservas_backend.model.EstadoTiquete;
import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.TipoTrayecto;
import com.reservasvuelos.reservas_backend.model.Tiquete;

@Service
public class TiqueteServicio extends CrudServicio<Tiquete, Long> {

    private final EstadoTiqueteRepositorio estadoTiqueteRepositorio;
    private final TipoTrayectoRepositorio tipoTrayectoRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final PasajeroRepositorio pasajeroRepositorio;
    private final TarifaRepositorio tarifaRepositorio;
    private final AsientoRepositorio asientoRepositorio;

    public TiqueteServicio(TiqueteRepositorio repositorio,
            EstadoTiqueteRepositorio estadoTiqueteRepositorio,
            TipoTrayectoRepositorio tipoTrayectoRepositorio,
            ReservaRepositorio reservaRepositorio,
            PasajeroRepositorio pasajeroRepositorio,
            TarifaRepositorio tarifaRepositorio,
            AsientoRepositorio asientoRepositorio) {
        super(repositorio, "el tiquete");
        this.estadoTiqueteRepositorio = estadoTiqueteRepositorio;
        this.tipoTrayectoRepositorio = tipoTrayectoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.pasajeroRepositorio = pasajeroRepositorio;
        this.tarifaRepositorio = tarifaRepositorio;
        this.asientoRepositorio = asientoRepositorio;
    }

    @Override
    protected void asignarId(Tiquete tiquete, Long id) {
        tiquete.setCodTiquete(id);
    }

    @Override
    protected void completar(Tiquete tiquete, Tiquete actual) {
        tiquete.setEstado(referencia(tiquete.getEstado(), EstadoTiquete::getCodEstado,
                estadoTiqueteRepositorio, "el estado de tiquete"));
        tiquete.setTipoTrayecto(referencia(tiquete.getTipoTrayecto(), TipoTrayecto::getCodTipo,
                tipoTrayectoRepositorio, "el tipo de trayecto"));
        tiquete.setReserva(referencia(tiquete.getReserva(), Reserva::getCodReserva, reservaRepositorio, "la reserva"));
        tiquete.setPasajero(referencia(tiquete.getPasajero(), Pasajero::getCodPasajero,
                pasajeroRepositorio, "el pasajero"));
        tiquete.setTarifa(referencia(tiquete.getTarifa(), TarifaServicio::id, tarifaRepositorio, "la tarifa"));
        tiquete.setAsiento(referencia(tiquete.getAsiento(), AsientoServicio::id, asientoRepositorio, "el asiento"));
        if (tiquete.getFechaEmision() == null) {
            tiquete.setFechaEmision(actual == null ? LocalDateTime.now() : actual.getFechaEmision());
        }
    }
}
