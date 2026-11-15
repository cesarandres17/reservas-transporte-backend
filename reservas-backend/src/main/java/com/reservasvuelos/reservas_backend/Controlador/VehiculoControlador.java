package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Vehiculo;
import com.reservasvuelos.reservas_backend.servicios.VehiculoServicio;

@RestController
@RequestMapping("/vehiculos")
public class VehiculoControlador extends CrudControlador<Vehiculo, Long> {

    public VehiculoControlador(VehiculoServicio vehiculoServicio) {
        super(vehiculoServicio);
    }
}
