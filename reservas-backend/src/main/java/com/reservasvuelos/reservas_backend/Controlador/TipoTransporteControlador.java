package com.reservasvuelos.reservas_backend.Controlador;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.reservasvuelos.reservas_backend.servicios.TipoTransporteServicio;

@Controller
@RequestMapping ("/tipo-transporte")
public class TipoTransporteControlador {

    private final TipoTransporteServicio tipoTransporteServicio;
    
    public TipoTransporteControlador(TipoTransporteServicio tipoTransporteServicio) {
        this.tipoTransporteServicio = tipoTransporteServicio;
    }
    
    @GetMapping
    public List<TipoTransporte> findAll() {
        return tipoTransporteServicio.findAll();
    }
    
}
