package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Promocion;
import com.reservasvuelos.reservas_backend.servicios.PromocionServicio;

@RestController
@RequestMapping("/promociones")
public class PromocionControlador extends CrudControlador<Promocion, Long> {

    public PromocionControlador(PromocionServicio promocionServicio) {
        super(promocionServicio);
    }
}
