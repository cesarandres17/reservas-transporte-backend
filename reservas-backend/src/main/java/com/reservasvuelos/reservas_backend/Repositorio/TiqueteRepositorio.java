package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Tiquete;

public interface TiqueteRepositorio extends JpaRepository<Tiquete, Long> {

    List<Tiquete> findByReserva_CodReservaOrderByCodTiquete(Long codReserva);
}
