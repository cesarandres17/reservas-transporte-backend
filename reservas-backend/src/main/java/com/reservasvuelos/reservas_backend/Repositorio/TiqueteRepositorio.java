package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Tiquete;

public interface TiqueteRepositorio extends JpaRepository<Tiquete, Long> {
}
