package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Promocion;

public interface PromocionRepositorio extends JpaRepository<Promocion, Long> {

    Optional<Promocion> findByCodigoIgnoreCase(String codigo);
}
