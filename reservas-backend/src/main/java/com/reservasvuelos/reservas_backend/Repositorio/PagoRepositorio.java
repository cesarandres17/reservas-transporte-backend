package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Pago;

public interface PagoRepositorio extends JpaRepository<Pago, Long> {
}
