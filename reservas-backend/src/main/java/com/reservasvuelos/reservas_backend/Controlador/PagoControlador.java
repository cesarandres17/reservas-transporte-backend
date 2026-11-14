package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Pago;
import com.reservasvuelos.reservas_backend.servicios.PagoServicio;

@RestController
@RequestMapping("/pagos")
public class PagoControlador extends CrudControlador<Pago, Long> {

    public PagoControlador(PagoServicio pagoServicio) {
        super(pagoServicio);
    }
}
