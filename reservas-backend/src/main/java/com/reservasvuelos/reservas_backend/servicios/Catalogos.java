package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Component;

import com.reservasvuelos.reservas_backend.Repositorio.EstadoPagoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EstadoReservaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EstadoTiqueteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoTrayectoRepositorio;
import com.reservasvuelos.reservas_backend.model.EstadoPago;
import com.reservasvuelos.reservas_backend.model.EstadoReserva;
import com.reservasvuelos.reservas_backend.model.EstadoTiquete;
import com.reservasvuelos.reservas_backend.model.TipoTrayecto;

@Component
public class Catalogos {

    private final EstadoReservaRepositorio estadoReservaRepositorio;
    private final EstadoPagoRepositorio estadoPagoRepositorio;
    private final EstadoTiqueteRepositorio estadoTiqueteRepositorio;
    private final TipoTrayectoRepositorio tipoTrayectoRepositorio;

    public Catalogos(EstadoReservaRepositorio estadoReservaRepositorio, EstadoPagoRepositorio estadoPagoRepositorio,
            EstadoTiqueteRepositorio estadoTiqueteRepositorio, TipoTrayectoRepositorio tipoTrayectoRepositorio) {
        this.estadoReservaRepositorio = estadoReservaRepositorio;
        this.estadoPagoRepositorio = estadoPagoRepositorio;
        this.estadoTiqueteRepositorio = estadoTiqueteRepositorio;
        this.tipoTrayectoRepositorio = tipoTrayectoRepositorio;
    }

    public EstadoReserva estadoReserva(String nombre) {
        return estadoReservaRepositorio.findByNombre(nombre).orElseThrow(() -> faltante("estado_reserva", nombre));
    }

    public EstadoPago estadoPago(String nombre) {
        return estadoPagoRepositorio.findByNombre(nombre).orElseThrow(() -> faltante("estado_pago", nombre));
    }

    public EstadoTiquete estadoTiquete(String nombre) {
        return estadoTiqueteRepositorio.findByNombre(nombre).orElseThrow(() -> faltante("estado_tiquete", nombre));
    }

    public TipoTrayecto tipoTrayecto(String nombre) {
        return tipoTrayectoRepositorio.findByNombre(nombre).orElseThrow(() -> faltante("tipo_trayecto", nombre));
    }

    private IllegalStateException faltante(String tabla, String nombre) {
        return new IllegalStateException("Falta " + nombre + " en la tabla " + tabla + ", corre sql/catalogos.sql");
    }
}
