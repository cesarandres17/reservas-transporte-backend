package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.EstadoReserva;

public interface EstadoReservaRepositorio extends JpaRepository<EstadoReserva, Long> {

    Optional<EstadoReserva> findByNombre(String nombre);
}
