package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.EstadoViaje;

public interface EstadoViajeRepositorio extends JpaRepository<EstadoViaje, Long> {
}
