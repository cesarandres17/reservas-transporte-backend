package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.servicios.TiqueteServicio;

@RestController
@RequestMapping("/tiquetes")
public class TiqueteControlador extends CrudControlador<Tiquete, Long> {

    public TiqueteControlador(TiqueteServicio tiqueteServicio) {
        super(tiqueteServicio);
    }
}
