package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Cliente;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
    
}
