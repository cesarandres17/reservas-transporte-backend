package com.reservasvuelos.reservas_backend.Repositorio;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.model.Tiquete;

public interface TiqueteRepositorio extends JpaRepository<Tiquete, Long> {

    List<Tiquete> findByReserva_CodReservaOrderByCodTiquete(Long codReserva);

    boolean existsByNumeroTiquete(String numeroTiquete);

    @Query("select a.numeroAsiento from Tiquete t join t.asiento a where t.tarifa.viaje.codViaje = :codViaje")
    Set<String> asientosOcupados(Long codViaje);

    @Query("""
            select count(t) > 0 from Tiquete t
            where t.tarifa.viaje.codViaje = :codViaje and t.pasajero.codPasajero = :codPasajero
              and t.estado.nombre in ('EMITIDO', 'USADO')
            """)
    boolean pasajeroEnViaje(Long codViaje, Long codPasajero);

    @Query("""
            select distinct r.cliente from Tiquete t join t.reserva r
            where t.tarifa.viaje.codViaje = :codViaje and t.estado.nombre = 'EMITIDO'
              and r.estado.nombre in ('PENDIENTE', 'CONFIRMADA', 'PAGADA')
            """)
    List<Cliente> clientesDelViaje(Long codViaje);

    @Query("""
            select t from Tiquete t
            join fetch t.reserva r join fetch r.cliente c join fetch c.usuario
            join fetch t.tarifa ta join fetch ta.viaje v
            where t.estado.nombre = 'EMITIDO' and r.estado.nombre = 'PAGADA' and v.estado.nombre in :estados
              and v.fechaHoraSalida > :desde and v.fechaHoraSalida <= :hasta
            order by v.fechaHoraSalida, r.codReserva
            """)
    List<Tiquete> pagadosPorSalir(Collection<String> estados, LocalDateTime desde, LocalDateTime hasta);
}
