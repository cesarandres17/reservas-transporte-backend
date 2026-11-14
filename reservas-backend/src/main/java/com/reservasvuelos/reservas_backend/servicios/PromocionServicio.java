package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.PromocionRepositorio;
import com.reservasvuelos.reservas_backend.model.Promocion;

@Service
public class PromocionServicio extends CrudServicio<Promocion, Long> {

    public PromocionServicio(PromocionRepositorio repositorio) {
        super(repositorio, "la promoción");
    }

    @Override
    protected void asignarId(Promocion promocion, Long id) {
        promocion.setCodPromocion(id);
    }
}
