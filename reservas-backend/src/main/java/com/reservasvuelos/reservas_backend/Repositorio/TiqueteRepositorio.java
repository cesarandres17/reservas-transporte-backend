package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.reservasvuelos.reservas_backend.model.Tiquete;

public interface TiqueteRepositorio extends JpaRepository<Tiquete, Long> {

    List<Tiquete> findByReserva_CodReservaOrderByCodTiquete(Long codReserva);

    boolean existsByNumeroTiquete(String numeroTiquete);

    @Query("select a.numeroAsiento from Tiquete t join t.asiento a where t.tarifa.viaje.codViaje = :codViaje")
    Set<String> asientosOcupados(Long codViaje);

    @Query("""
            select count(t) > 0 from Tiquete t
            where t.tarifa.viaje.codViaje = :codViaje and t.pasajero.codPasajero = :codPasajero
            """)
    boolean pasajeroEnViaje(Long codViaje, Long codPasajero);
}
