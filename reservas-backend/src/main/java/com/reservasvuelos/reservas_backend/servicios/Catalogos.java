package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Component;

import com.reservasvuelos.reservas_backend.Repositorio.EstadoNotificacionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EstadoPagoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EstadoReservaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EstadoTiqueteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoNotificacionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoTrayectoRepositorio;
import com.reservasvuelos.reservas_backend.model.EstadoNotificacion;
import com.reservasvuelos.reservas_backend.model.EstadoPago;
import com.reservasvuelos.reservas_backend.model.EstadoReserva;
import com.reservasvuelos.reservas_backend.model.EstadoTiquete;
import com.reservasvuelos.reservas_backend.model.TipoNotificacion;
import com.reservasvuelos.reservas_backend.model.TipoTrayecto;

@Component
public class Catalogos {

    private final EstadoReservaRepositorio estadoReservaRepositorio;
    private final EstadoPagoRepositorio estadoPagoRepositorio;
    private final EstadoTiqueteRepositorio estadoTiqueteRepositorio;
    private final TipoTrayectoRepositorio tipoTrayectoRepositorio;
    private final TipoNotificacionRepositorio tipoNotificacionRepositorio;
    private final EstadoNotificacionRepositorio estadoNotificacionRepositorio;

    public Catalogos(EstadoReservaRepositorio estadoReservaRepositorio, EstadoPagoRepositorio estadoPagoRepositorio,
            EstadoTiqueteRepositorio estadoTiqueteRepositorio, TipoTrayectoRepositorio tipoTrayectoRepositorio,
            TipoNotificacionRepositorio tipoNotificacionRepositorio,
            EstadoNotificacionRepositorio estadoNotificacionRepositorio) {
        this.estadoReservaRepositorio = estadoReservaRepositorio;
        this.estadoPagoRepositorio = estadoPagoRepositorio;
        this.estadoTiqueteRepositorio = estadoTiqueteRepositorio;
        this.tipoTrayectoRepositorio = tipoTrayectoRepositorio;
        this.tipoNotificacionRepositorio = tipoNotificacionRepositorio;
        this.estadoNotificacionRepositorio = estadoNotificacionRepositorio;
    }

    public EstadoReserva estadoReserva(String nombre) {
        return estadoReservaRepositorio.buscarPorNombre(nombre).orElseThrow(() -> faltante("estado_reserva", nombre));
    }

    public EstadoPago estadoPago(String nombre) {
        return estadoPagoRepositorio.buscarPorNombre(nombre).orElseThrow(() -> faltante("estado_pago", nombre));
    }

    public EstadoTiquete estadoTiquete(String nombre) {
        return estadoTiqueteRepositorio.buscarPorNombre(nombre).orElseThrow(() -> faltante("estado_tiquete", nombre));
    }

    public TipoTrayecto tipoTrayecto(String nombre) {
        return tipoTrayectoRepositorio.buscarPorNombre(nombre).orElseThrow(() -> faltante("tipo_trayecto", nombre));
    }

    public TipoNotificacion tipoNotificacion(String nombre) {
        return tipoNotificacionRepositorio.buscarPorNombre(nombre)
                .orElseThrow(() -> faltante("tipo_notificacion", nombre));
    }

    public EstadoNotificacion estadoNotificacion(String nombre) {
        return estadoNotificacionRepositorio.buscarPorNombre(nombre)
                .orElseThrow(() -> faltante("estado_notificacion", nombre));
    }

    private IllegalStateException faltante(String tabla, String nombre) {
        return new IllegalStateException("Falta " + nombre + " en la tabla " + tabla + ", corre sql/catalogos.sql");
    }
}
