package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.RolRepositorio;
import com.reservasvuelos.reservas_backend.model.Rol;

@Service
public class RolServicio extends CrudServicio<Rol, Long> {

    public RolServicio(RolRepositorio repositorio) {
        super(repositorio, "el rol");
    }

    @Override
    protected void asignarId(Rol rol, Long id) {
        rol.setCodRol(id);
    }
}
