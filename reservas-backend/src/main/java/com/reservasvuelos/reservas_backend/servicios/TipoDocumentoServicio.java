package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.TipoDocumentoRepositorio;
import com.reservasvuelos.reservas_backend.model.TipoDocumento;

@Service
public class TipoDocumentoServicio extends CrudServicio<TipoDocumento, Long> {

    public TipoDocumentoServicio(TipoDocumentoRepositorio repositorio) {
        super(repositorio, "el tipo de documento");
    }

    @Override
    protected void asignarId(TipoDocumento tipoDocumento, Long id) {
        tipoDocumento.setCodTipo(id);
    }
}
