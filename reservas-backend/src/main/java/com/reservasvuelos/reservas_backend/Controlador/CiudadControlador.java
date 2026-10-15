package com.reservasvuelos.reservas_backend.Controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.servicios.CiudadServicio;

@RestController
@RequestMapping("/ciudades")
public class CiudadControlador {
    @Autowired 
    private final CiudadServicio ciudadServicio;
    
    public CiudadControlador(CiudadServicio ciudadServicio) {
        this.ciudadServicio = ciudadServicio;
    }
    
    @GetMapping 
    private ResponseEntity<List<Ciudad>> getAllCiudades() {
        return ResponseEntity.ok(ciudadServicio.findAll());
    }
    

    
}
