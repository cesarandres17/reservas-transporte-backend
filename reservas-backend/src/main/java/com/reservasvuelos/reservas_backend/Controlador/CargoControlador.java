package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Cargo;
import com.reservasvuelos.reservas_backend.servicios.CargoServicio;

@RestController
@RequestMapping("/cargos")
public class CargoControlador extends CrudControlador<Cargo, Long> {

    public CargoControlador(CargoServicio cargoServicio) {
        super(cargoServicio);
    }
}
