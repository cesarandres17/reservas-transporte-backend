package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.PromocionRepositorio;
import com.reservasvuelos.reservas_backend.model.Promocion;

@Service
public class PromocionServicio {

    private final PromocionRepositorio promocionRepositorio;

    public PromocionServicio(PromocionRepositorio promocionRepositorio) {
        this.promocionRepositorio = promocionRepositorio;
    }

    public List<Promocion> findAll() {
        return promocionRepositorio.findAll();
    }

}
