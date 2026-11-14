package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.EstadoPago;
import com.reservasvuelos.reservas_backend.servicios.EstadoPagoServicio;

@RestController
@RequestMapping("/estados-pago")
public class EstadoPagoControlador extends CrudControlador<EstadoPago, Long> {

    public EstadoPagoControlador(EstadoPagoServicio estadoPagoServicio) {
        super(estadoPagoServicio);
    }
}
