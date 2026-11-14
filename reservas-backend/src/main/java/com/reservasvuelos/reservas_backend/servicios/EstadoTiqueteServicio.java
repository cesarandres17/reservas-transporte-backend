package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.EstadoTiqueteRepositorio;
import com.reservasvuelos.reservas_backend.model.EstadoTiquete;

@Service
public class EstadoTiqueteServicio extends CrudServicio<EstadoTiquete, Long> {

    public EstadoTiqueteServicio(EstadoTiqueteRepositorio repositorio) {
        super(repositorio, "el estado de tiquete");
    }

    @Override
    protected void asignarId(EstadoTiquete estadoTiquete, Long id) {
        estadoTiquete.setCodEstado(id);
    }
}
