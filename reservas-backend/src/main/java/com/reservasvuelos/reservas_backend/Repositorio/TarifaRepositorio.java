package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.reservasvuelos.reservas_backend.model.Tarifa;
import com.reservasvuelos.reservas_backend.model.TarifaId;

import jakarta.persistence.LockModeType;

public interface TarifaRepositorio extends JpaRepository<Tarifa, TarifaId> {

    @Query("""
            select t from Tarifa t join fetch t.claseServicio
            where t.viaje.codViaje in :viajes
            order by t.precioBase
            """)
    List<Tarifa> deLosViajes(Collection<Long> viajes);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tarifa t where t.viaje.codViaje = :codViaje and t.claseServicio.codClase = :codClase")
    Optional<Tarifa> bloquear(Long codViaje, Long codClase);

    @Modifying(flushAutomatically = true)
    @Query("""
            update Tarifa t set t.cuposDisponibles = t.cuposDisponibles + :cantidad
            where t.viaje.codViaje = :codViaje and t.claseServicio.codClase = :codClase
            """)
    int devolverCupos(Long codViaje, Long codClase, int cantidad);
}
