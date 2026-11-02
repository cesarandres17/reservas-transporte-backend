package com.reservasvuelos.reservas_backend.servicios;
import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.RolRepositorio;

@Service
public class RolServicio {
    private final RolRepositorio rolRepositorio;

    public RolServicio(RolRepositorio rolRepositorio) {
        this.rolRepositorio = rolRepositorio;
    }

    
}
