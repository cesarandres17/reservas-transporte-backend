package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.MetodoPago;

public interface MetodoPagoRepositorio extends JpaRepository<MetodoPago, Long> {
    
}
