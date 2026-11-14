package com.reservasvuelos.reservas_backend.Repositorio;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.reservasvuelos.reservas_backend.model.Viaje;

public interface ViajeRepositorio extends JpaRepository<Viaje, Long> {

    @Query("""
            select v from Viaje v
            join fetch v.estado e
            join fetch v.ruta r
            join fetch r.terminalOrigen o join fetch o.ciudad co join fetch o.tipoTransporte
            join fetch r.terminalDestino d join fetch d.ciudad cd
            join fetch v.vehiculo ve join fetch ve.empresa
            where co.codCiudad = :origen and cd.codCiudad = :destino
              and v.fechaHoraSalida >= :desde and v.fechaHoraSalida < :hasta
              and e.nombre in :estados
            order by v.fechaHoraSalida
            """)
    List<Viaje> buscarDisponibles(Long origen, Long destino, LocalDateTime desde, LocalDateTime hasta,
            Collection<String> estados);
}
