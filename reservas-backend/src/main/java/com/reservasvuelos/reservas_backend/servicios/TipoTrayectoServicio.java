package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.TipoTrayectoRepositorio;
import com.reservasvuelos.reservas_backend.model.TipoTrayecto;

@Service
public class TipoTrayectoServicio extends CrudServicio<TipoTrayecto, Long> {

    public TipoTrayectoServicio(TipoTrayectoRepositorio repositorio) {
        super(repositorio, "el tipo de trayecto");
    }

    @Override
    protected void asignarId(TipoTrayecto tipoTrayecto, Long id) {
        tipoTrayecto.setCodTipo(id);
    }
}
