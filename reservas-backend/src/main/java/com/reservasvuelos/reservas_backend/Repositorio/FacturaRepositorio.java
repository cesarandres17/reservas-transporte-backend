package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Factura;

public interface FacturaRepositorio extends JpaRepository<Factura, Long> {
}
