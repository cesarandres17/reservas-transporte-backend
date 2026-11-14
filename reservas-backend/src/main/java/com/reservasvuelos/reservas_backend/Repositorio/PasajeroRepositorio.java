package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.TipoDocumento;

public interface PasajeroRepositorio extends JpaRepository<Pasajero, Long> {

    Optional<Pasajero> findByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);
}
