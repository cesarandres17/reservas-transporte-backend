package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.reservasvuelos.reservas_backend.model.Checkin;

public interface CheckinRepositorio extends JpaRepository<Checkin, Long> {

    @Query("""
            select c from Checkin c join fetch c.tiquete t
            where t.reserva.codReserva = :codReserva
            order by t.codTiquete
            """)
    List<Checkin> deReserva(Long codReserva);

    boolean existsByTiquete_Reserva_CodReserva(Long codReserva);
}
