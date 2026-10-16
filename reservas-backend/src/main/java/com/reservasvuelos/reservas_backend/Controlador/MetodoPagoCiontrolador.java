package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.reservasvuelos.reservas_backend.servicios.MetodoPagoServicio;

@Controller
@RequestMapping("/metodos-pago")
public class MetodoPagoCiontrolador {

    private final MetodoPagoServicio metodoPagoServicio;
    
    public MetodoPagoCiontrolador(MetodoPagoServicio metodoPagoServicio) {
        this.metodoPagoServicio = metodoPagoServicio;
    }
    
    
}
