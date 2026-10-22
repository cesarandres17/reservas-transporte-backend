package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.servicios.MetodoPagoServicio;

@RestController
@RequestMapping("/metodos-pago")
public class MetodoPagoControlador {

    private final MetodoPagoServicio metodoPagoServicio;
    
    public MetodoPagoControlador(MetodoPagoServicio metodoPagoServicio) {
        this.metodoPagoServicio = metodoPagoServicio;
    }
    
    
}
