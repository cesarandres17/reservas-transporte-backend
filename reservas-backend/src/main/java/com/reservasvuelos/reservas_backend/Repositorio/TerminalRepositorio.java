package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Terminal;

public interface TerminalRepositorio extends JpaRepository<Terminal, Long> {
}
