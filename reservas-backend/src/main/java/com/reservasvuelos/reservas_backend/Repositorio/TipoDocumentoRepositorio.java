package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.TipoDocumento;

public interface TipoDocumentoRepositorio extends JpaRepository<TipoDocumento, Long> {

    Optional<TipoDocumento> findBySiglaIgnoreCase(String sigla);
}
