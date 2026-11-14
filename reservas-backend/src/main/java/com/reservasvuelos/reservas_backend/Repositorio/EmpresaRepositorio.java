package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Empresa;

public interface EmpresaRepositorio extends JpaRepository<Empresa, Long> {
}
