package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.reservasvuelos.reservas_backend.model.Factura;

public interface FacturaRepositorio extends JpaRepository<Factura, Long> {

    @Query(value = """
            select coalesce(max(cast(substring(numero_factura from 4) as integer)), 0)
            from factura where numero_factura ~ '^FE-[0-9]{6}$'
            """, nativeQuery = true)
    int ultimoConsecutivo();
}
