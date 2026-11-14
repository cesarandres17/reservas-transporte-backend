package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.PaisRepositorio;
import com.reservasvuelos.reservas_backend.model.Pais;

@Service
public class PaisServicio extends CrudServicio<Pais, Long> {

    public PaisServicio(PaisRepositorio repositorio) {
        super(repositorio, "el país");
    }

    @Override
    protected void asignarId(Pais pais, Long id) {
        pais.setCodPais(id);
    }
}
