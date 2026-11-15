package com.reservasvuelos.reservas_backend.Controlador;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.dto.NotificacionClienteDTO;
import com.reservasvuelos.reservas_backend.servicios.BandejaServicio;

@RestController
@RequestMapping("/notificaciones/mias")
public class BandejaControlador {

    private final BandejaServicio bandejaServicio;

    public BandejaControlador(BandejaServicio bandejaServicio) {
        this.bandejaServicio = bandejaServicio;
    }

    @GetMapping
    public List<NotificacionClienteDTO> mias(Authentication autenticacion) {
        return bandejaServicio.mias(autenticacion);
    }

    @PostMapping("/{codNotificacion}/leida")
    public NotificacionClienteDTO marcarLeida(@PathVariable Long codNotificacion, Authentication autenticacion) {
        return bandejaServicio.marcarLeida(codNotificacion, autenticacion);
    }

    @PostMapping("/leidas")
    public Map<String, Integer> marcarTodas(Authentication autenticacion) {
        return Map.of("marcadas", bandejaServicio.marcarTodas(autenticacion));
    }
}
