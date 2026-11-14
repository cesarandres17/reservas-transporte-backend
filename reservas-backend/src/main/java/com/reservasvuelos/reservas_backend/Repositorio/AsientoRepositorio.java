package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.model.AsientoId;

public interface AsientoRepositorio extends JpaRepository<Asiento, AsientoId> {

    @Query("""
            select a from Asiento a join fetch a.claseServicio
            where a.vehiculo.codVehiculo = :codVehiculo
            order by a.fila, a.letra
            """)
    List<Asiento> delVehiculo(Long codVehiculo);
}
