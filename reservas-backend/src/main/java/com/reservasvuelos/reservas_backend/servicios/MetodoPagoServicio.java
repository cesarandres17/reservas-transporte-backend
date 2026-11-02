package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.MetodoPagoRepositorio;

@Service
public class MetodoPagoServicio {    
    
    private final MetodoPagoRepositorio metodoPagoRepositorio;
    
    public MetodoPagoServicio(MetodoPagoRepositorio metodoPagoRepositorio) {
        this.metodoPagoRepositorio = metodoPagoRepositorio;
    }
    
}
