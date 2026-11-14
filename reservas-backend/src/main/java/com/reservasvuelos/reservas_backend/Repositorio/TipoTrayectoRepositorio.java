package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.TipoTrayecto;

public interface TipoTrayectoRepositorio extends JpaRepository<TipoTrayecto, Long> {
}
