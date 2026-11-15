package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.EstadoReservaRepositorio;
import com.reservasvuelos.reservas_backend.model.EstadoReserva;

@Service
public class EstadoReservaServicio extends CrudServicio<EstadoReserva, Long> {

    public EstadoReservaServicio(EstadoReservaRepositorio repositorio) {
        super(repositorio, "el estado de reserva");
    }

    @Override
    protected void asignarId(EstadoReserva estadoReserva, Long id) {
        estadoReserva.setCodEstado(id);
    }
}
