package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Pais;

public interface PaisRepositorio extends JpaRepository<Pais, Long> {

    Optional<Pais> findByCodigoIsoIgnoreCase(String codigoIso);
}
