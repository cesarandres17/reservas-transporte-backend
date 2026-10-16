package com.reservasvuelos.reservas_backend.servicios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.RolRepositorio;

@Service
public class RolServicio {
    @Autowired 
    private final RolRepositorio rolRepositorio;

    public RolServicio(RolRepositorio rolRepositorio) {
        this.rolRepositorio = rolRepositorio;
    }

    
}
