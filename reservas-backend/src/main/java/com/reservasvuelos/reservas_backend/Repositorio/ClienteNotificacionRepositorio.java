package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.ClienteNotificacion;
import com.reservasvuelos.reservas_backend.model.ClienteNotificacionId;

public interface ClienteNotificacionRepositorio extends JpaRepository<ClienteNotificacion, ClienteNotificacionId> {
}
