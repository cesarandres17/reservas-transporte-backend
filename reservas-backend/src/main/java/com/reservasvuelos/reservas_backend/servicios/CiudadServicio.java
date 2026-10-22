package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.CiudadRepositorio;
import com.reservasvuelos.reservas_backend.model.Ciudad;
@Service
public class CiudadServicio {
    private final CiudadRepositorio ciudadRepositorio;

    public CiudadServicio(CiudadRepositorio ciudadRepositorio) {
        this.ciudadRepositorio = ciudadRepositorio;
    }

    public List<Ciudad> findAll() {
        return ciudadRepositorio.findAll();
    }

}
