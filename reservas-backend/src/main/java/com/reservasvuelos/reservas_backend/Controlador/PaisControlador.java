package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Pais;
import com.reservasvuelos.reservas_backend.servicios.PaisServicio;

@RestController
@RequestMapping("/paises")
public class PaisControlador extends CrudControlador<Pais, Long> {

    public PaisControlador(PaisServicio paisServicio) {
        super(paisServicio);
    }
}
