package com.reservasvuelos.reservas_backend.Controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Pais;
import com.reservasvuelos.reservas_backend.servicios.PaisServicio;

@RestController 
@RequestMapping ("/paises")
public class PaisControlador {

    private final PaisServicio paisServicio;
    
    public PaisControlador(PaisServicio paisServicio) {
        this.paisServicio = paisServicio;
    }
    
    @GetMapping
    public ResponseEntity<List<Pais>> getAllPaises() {
        return ResponseEntity.ok(paisServicio.findAll());
    }
    
}
