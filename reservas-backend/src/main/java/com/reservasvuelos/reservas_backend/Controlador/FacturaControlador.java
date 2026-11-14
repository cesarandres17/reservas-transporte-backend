package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Factura;
import com.reservasvuelos.reservas_backend.servicios.FacturaServicio;

@RestController
@RequestMapping("/facturas")
public class FacturaControlador extends CrudControlador<Factura, Long> {

    public FacturaControlador(FacturaServicio facturaServicio) {
        super(facturaServicio);
    }
}
