package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Rol;

public interface RolRepositorio extends JpaRepository<Rol, Long> {
    
}
