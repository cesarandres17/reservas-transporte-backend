package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.model.AsientoId;

public interface AsientoRepositorio extends JpaRepository<Asiento, AsientoId> {
}
