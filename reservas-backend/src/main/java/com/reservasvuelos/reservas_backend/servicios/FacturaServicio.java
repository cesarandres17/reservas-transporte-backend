package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.FacturaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ReservaRepositorio;
import com.reservasvuelos.reservas_backend.model.Factura;
import com.reservasvuelos.reservas_backend.model.Reserva;

@Service
public class FacturaServicio extends CrudServicio<Factura, Long> {

    private final ReservaRepositorio reservaRepositorio;

    public FacturaServicio(FacturaRepositorio repositorio, ReservaRepositorio reservaRepositorio) {
        super(repositorio, "la factura");
        this.reservaRepositorio = reservaRepositorio;
    }

    @Override
    protected void asignarId(Factura factura, Long id) {
        factura.setCodReserva(id);
    }

    @Override
    protected void completar(Factura factura, Factura actual) {
        if (actual != null) {
            factura.setReserva(actual.getReserva());
        } else {
            factura.setReserva(obligatorio(referencia(factura.getReserva(), Reserva::getCodReserva,
                    reservaRepositorio, "la reserva"), "la reserva"));
        }
        if (factura.getFechaEmision() == null) {
            factura.setFechaEmision(actual == null ? LocalDateTime.now() : actual.getFechaEmision());
        }
    }

    @Override
    protected void validarNuevo(Factura factura) {
        rechazarSiExiste(factura.getReserva().getCodReserva());
    }
}
