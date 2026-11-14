package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Ciudad;

public interface CiudadRepositorio extends JpaRepository<Ciudad, Long> {

    List<Ciudad> findByDepartamento_CodDepartamento(Long codDepartamento);
}
