package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.reservasvuelos.reservas_backend.model.Equipaje;
import com.reservasvuelos.reservas_backend.model.EquipajeId;

public interface EquipajeRepositorio extends JpaRepository<Equipaje, EquipajeId> {

    List<Equipaje> findByTiquete_CodTiqueteOrderByNumeroPieza(Long codTiquete);

    @Query("""
            select e from Equipaje e join fetch e.tiquete t
            where t.reserva.codReserva = :codReserva
            order by t.codTiquete, e.numeroPieza
            """)
    List<Equipaje> deReserva(Long codReserva);
}
