package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.MetodoPagoRepositorio;
import com.reservasvuelos.reservas_backend.model.MetodoPago;

@Service
public class MetodoPagoServicio extends CrudServicio<MetodoPago, Long> {

    public MetodoPagoServicio(MetodoPagoRepositorio repositorio) {
        super(repositorio, "el método de pago");
    }

    @Override
    protected void asignarId(MetodoPago metodoPago, Long id) {
        metodoPago.setCodMetodo(id);
    }
}
