package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.ClaseServicio;

public interface ClaseServicioRepositorio extends JpaRepository<ClaseServicio, Long> {
    
}
