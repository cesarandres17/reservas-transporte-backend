package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.ClaseServicioRepositorio;
import com.reservasvuelos.reservas_backend.model.ClaseServicio;

@Service
public class ClaseServicioServicio extends CrudServicio<ClaseServicio, Long> {

    public ClaseServicioServicio(ClaseServicioRepositorio repositorio) {
        super(repositorio, "la clase de servicio");
    }

    @Override
    protected void asignarId(ClaseServicio claseServicio, Long id) {
        claseServicio.setCodClase(id);
    }
}
