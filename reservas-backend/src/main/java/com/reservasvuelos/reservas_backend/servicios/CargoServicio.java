package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.CargoRepositorio;
import com.reservasvuelos.reservas_backend.model.Cargo;

@Service
public class CargoServicio extends CrudServicio<Cargo, Long> {

    public CargoServicio(CargoRepositorio repositorio) {
        super(repositorio, "el cargo");
    }

    @Override
    protected void asignarId(Cargo cargo, Long id) {
        cargo.setCodCargo(id);
    }
}
