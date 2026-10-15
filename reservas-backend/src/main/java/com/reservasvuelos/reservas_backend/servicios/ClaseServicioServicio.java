package com.reservasvuelos.reservas_backend.servicios;
import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.ClaseServicioRepositorio;

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
