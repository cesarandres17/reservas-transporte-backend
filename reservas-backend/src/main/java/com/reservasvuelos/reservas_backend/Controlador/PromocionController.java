package com.reservasvuelos.reservas_backend.Controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.reservasvuelos.reservas_backend.model.Promocion;
import com.reservasvuelos.reservas_backend.servicios.PromocionServicio;

@Controller
@RequestMapping("/promociones")
public class PromocionController {

    private final PromocionServicio promocionServicio;

    public PromocionController(PromocionServicio promocionServicio) {
        this.promocionServicio = promocionServicio;
    }

    @GetMapping
    public ResponseEntity<List<Promocion>> getAllPromociones() {
        return ResponseEntity.ok(promocionServicio.findAll());
    }

}
