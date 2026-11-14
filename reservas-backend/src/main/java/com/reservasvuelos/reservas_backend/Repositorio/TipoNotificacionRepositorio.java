package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.TipoNotificacion;

public interface TipoNotificacionRepositorio extends JpaRepository<TipoNotificacion, Long> {

    Optional<TipoNotificacion> findByNombre(String nombre);
}
