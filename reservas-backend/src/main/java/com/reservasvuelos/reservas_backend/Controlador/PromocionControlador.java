package com.reservasvuelos.reservas_backend.Controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Promocion;
import com.reservasvuelos.reservas_backend.servicios.PromocionServicio;

@RestController
@RequestMapping("/promociones")
public class PromocionControlador {

    private final PromocionServicio promocionServicio;

    public PromocionControlador(PromocionServicio promocionServicio) {
        this.promocionServicio = promocionServicio;
    }

    @GetMapping
    public ResponseEntity<List<Promocion>> getAllPromociones() {
        return ResponseEntity.ok(promocionServicio.findAll());
    }

}
