package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.EstadoPago;

public interface EstadoPagoRepositorio extends JpaRepository<EstadoPago, Long> {

    Optional<EstadoPago> findByNombre(String nombre);
}
