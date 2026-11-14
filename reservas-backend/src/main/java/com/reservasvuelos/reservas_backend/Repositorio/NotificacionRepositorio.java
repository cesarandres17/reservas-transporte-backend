package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Notificacion;

public interface NotificacionRepositorio extends JpaRepository<Notificacion, Long> {
}
