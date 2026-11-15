package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.TipoTransporteRepositorio;
import com.reservasvuelos.reservas_backend.model.TipoTransporte;

@Service
public class TipoTransporteServicio extends CrudServicio<TipoTransporte, Long> {

    public TipoTransporteServicio(TipoTransporteRepositorio repositorio) {
        super(repositorio, "el tipo de transporte");
    }

    @Override
    protected void asignarId(TipoTransporte tipoTransporte, Long id) {
        tipoTransporte.setCodTipo(id);
    }
}
