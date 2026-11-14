package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Equipaje;
import com.reservasvuelos.reservas_backend.model.EquipajeId;

public interface EquipajeRepositorio extends JpaRepository<Equipaje, EquipajeId> {
}
