package com.reservasvuelos.reservas_backend.servicios;
import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.PaisRepositorio;
import com.reservasvuelos.reservas_backend.model.Pais;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaisServicio {
    @Autowired
    private PaisRepositorio paisRepositorio;

    public List<Pais> findAll() {
        return paisRepositorio.findAll();
    }
    
}
