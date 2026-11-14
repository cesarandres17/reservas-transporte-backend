package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EmpleadoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EstadoReservaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PromocionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ReservaRepositorio;
import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.model.Empleado;
import com.reservasvuelos.reservas_backend.model.EstadoReserva;
import com.reservasvuelos.reservas_backend.model.Promocion;
import com.reservasvuelos.reservas_backend.model.Reserva;

@Service
public class ReservaServicio extends CrudServicio<Reserva, Long> {

    private final EstadoReservaRepositorio estadoReservaRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;
    private final PromocionRepositorio promocionRepositorio;

    public ReservaServicio(ReservaRepositorio repositorio,
            EstadoReservaRepositorio estadoReservaRepositorio,
            ClienteRepositorio clienteRepositorio,
            EmpleadoRepositorio empleadoRepositorio,
            PromocionRepositorio promocionRepositorio) {
        super(repositorio, "la reserva");
        this.estadoReservaRepositorio = estadoReservaRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
        this.promocionRepositorio = promocionRepositorio;
    }

    @Override
    protected void asignarId(Reserva reserva, Long id) {
        reserva.setCodReserva(id);
    }

    @Override
    protected void completar(Reserva reserva, Reserva actual) {
        reserva.setEstado(referencia(reserva.getEstado(), EstadoReserva::getCodEstado,
                estadoReservaRepositorio, "el estado de reserva"));
        reserva.setCliente(referencia(reserva.getCliente(), Cliente::getCodCliente,
                clienteRepositorio, "el cliente"));
        reserva.setEmpleado(referencia(reserva.getEmpleado(), Empleado::getCodEmpleado,
                empleadoRepositorio, "el empleado"));
        reserva.setPromocion(referencia(reserva.getPromocion(), Promocion::getCodPromocion,
                promocionRepositorio, "la promoción"));
        if (reserva.getFechaReserva() == null) {
            reserva.setFechaReserva(actual == null ? LocalDateTime.now() : actual.getFechaReserva());
        }
    }
}
