package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.EstadoPagoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.MetodoPagoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PagoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ReservaRepositorio;
import com.reservasvuelos.reservas_backend.model.EstadoPago;
import com.reservasvuelos.reservas_backend.model.MetodoPago;
import com.reservasvuelos.reservas_backend.model.Pago;
import com.reservasvuelos.reservas_backend.model.Reserva;

@Service
public class PagoServicio extends CrudServicio<Pago, Long> {

    private final EstadoPagoRepositorio estadoPagoRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final MetodoPagoRepositorio metodoPagoRepositorio;

    public PagoServicio(PagoRepositorio repositorio,
            EstadoPagoRepositorio estadoPagoRepositorio,
            ReservaRepositorio reservaRepositorio,
            MetodoPagoRepositorio metodoPagoRepositorio) {
        super(repositorio, "el pago");
        this.estadoPagoRepositorio = estadoPagoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.metodoPagoRepositorio = metodoPagoRepositorio;
    }

    @Override
    protected void asignarId(Pago pago, Long id) {
        pago.setCodPago(id);
    }

    @Override
    protected void completar(Pago pago, Pago actual) {
        pago.setEstado(referencia(pago.getEstado(), EstadoPago::getCodEstado,
                estadoPagoRepositorio, "el estado de pago"));
        pago.setReserva(referencia(pago.getReserva(), Reserva::getCodReserva, reservaRepositorio, "la reserva"));
        pago.setMetodoPago(referencia(pago.getMetodoPago(), MetodoPago::getCodMetodo,
                metodoPagoRepositorio, "el método de pago"));
        if (pago.getFechaPago() == null) {
            pago.setFechaPago(actual == null ? LocalDateTime.now() : actual.getFechaPago());
        }
    }
}
