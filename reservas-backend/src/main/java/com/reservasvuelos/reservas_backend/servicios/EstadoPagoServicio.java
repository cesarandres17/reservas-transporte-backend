package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.EstadoPagoRepositorio;
import com.reservasvuelos.reservas_backend.model.EstadoPago;

@Service
public class EstadoPagoServicio extends CrudServicio<EstadoPago, Long> {

    public EstadoPagoServicio(EstadoPagoRepositorio repositorio) {
        super(repositorio, "el estado de pago");
    }

    @Override
    protected void asignarId(EstadoPago estadoPago, Long id) {
        estadoPago.setCodEstado(id);
    }
}
