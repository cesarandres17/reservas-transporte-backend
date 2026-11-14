package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Pago;

public interface PagoRepositorio extends JpaRepository<Pago, Long> {

    List<Pago> findByReserva_CodReservaOrderByFechaPago(Long codReserva);
}
