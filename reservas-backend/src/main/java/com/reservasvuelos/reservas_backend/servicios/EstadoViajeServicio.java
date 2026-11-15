package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.EstadoViajeRepositorio;
import com.reservasvuelos.reservas_backend.model.EstadoViaje;

@Service
public class EstadoViajeServicio extends CrudServicio<EstadoViaje, Long> {

    public EstadoViajeServicio(EstadoViajeRepositorio repositorio) {
        super(repositorio, "el estado de viaje");
    }

    @Override
    protected void asignarId(EstadoViaje estadoViaje, Long id) {
        estadoViaje.setCodEstado(id);
    }
}
