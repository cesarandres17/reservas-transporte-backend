package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.MetodoPago;
import com.reservasvuelos.reservas_backend.servicios.MetodoPagoServicio;

@RestController
@RequestMapping("/metodos-pago")
public class MetodoPagoControlador extends CrudControlador<MetodoPago, Long> {

    public MetodoPagoControlador(MetodoPagoServicio metodoPagoServicio) {
        super(metodoPagoServicio);
    }
}
