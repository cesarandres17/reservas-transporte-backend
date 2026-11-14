package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.EstadoNotificacion;

public interface EstadoNotificacionRepositorio extends JpaRepository<EstadoNotificacion, Long> {

    Optional<EstadoNotificacion> findByNombre(String nombre);
}
