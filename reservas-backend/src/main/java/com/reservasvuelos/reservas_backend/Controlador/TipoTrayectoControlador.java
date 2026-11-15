package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.TipoTrayecto;
import com.reservasvuelos.reservas_backend.servicios.TipoTrayectoServicio;

@RestController
@RequestMapping("/tipos-trayecto")
public class TipoTrayectoControlador extends CrudControlador<TipoTrayecto, Long> {

    public TipoTrayectoControlador(TipoTrayectoServicio tipoTrayectoServicio) {
        super(tipoTrayectoServicio);
    }
}
