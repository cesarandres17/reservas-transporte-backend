package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Busqueda;

public interface BusquedaRepositorio extends JpaRepository<Busqueda, Long> {
}
