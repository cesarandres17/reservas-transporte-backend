package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.EstadoTiquete;

public interface EstadoTiqueteRepositorio extends JpaRepository<EstadoTiquete, Long> {

    Optional<EstadoTiquete> findByNombre(String nombre);
}
