package com.reservasvuelos.reservas_backend.servicios;
import java.util.List;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.ClaseServicioRepositorio;
import com.reservasvuelos.reservas_backend.model.ClaseServicio;

@Service
public class ClaseServicioServicio {

    private final ClaseServicioRepositorio claseServicioRepositorio;

    public ClaseServicioServicio(ClaseServicioRepositorio claseServicioRepositorio) {
        this.claseServicioRepositorio = claseServicioRepositorio;
    }

    public List<ClaseServicio> findAll() {
        return claseServicioRepositorio.findAll();
    }

}
