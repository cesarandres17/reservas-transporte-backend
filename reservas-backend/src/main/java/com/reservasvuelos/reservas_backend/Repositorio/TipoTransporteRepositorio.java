package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.TipoTransporte;

public interface TipoTransporteRepositorio extends JpaRepository<TipoTransporte, Long> {
    
}
