package com.reservasvuelos.reservas_backend.Controlador;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.TipoTransporte;
import com.reservasvuelos.reservas_backend.servicios.TipoTransporteServicio;

@RestController
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
