package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.TipoTransporte;
import com.reservasvuelos.reservas_backend.servicios.TipoTransporteServicio;

@RestController
@RequestMapping("/tipos-transporte")
public class TipoTransporteControlador extends CrudControlador<TipoTransporte, Long> {

    public TipoTransporteControlador(TipoTransporteServicio tipoTransporteServicio) {
        super(tipoTransporteServicio);
    }
}
