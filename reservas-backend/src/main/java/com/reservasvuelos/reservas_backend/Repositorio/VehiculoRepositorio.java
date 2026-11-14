package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Vehiculo;

public interface VehiculoRepositorio extends JpaRepository<Vehiculo, Long> {
}
