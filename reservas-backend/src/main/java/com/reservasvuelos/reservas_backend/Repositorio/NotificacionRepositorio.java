package com.reservasvuelos.reservas_backend.Repositorio;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.reservasvuelos.reservas_backend.model.Notificacion;

public interface NotificacionRepositorio extends JpaRepository<Notificacion, Long> {

    @Query("""
            select distinct n from ClienteNotificacion cn join cn.notificacion n left join n.viaje v
            where cn.estadoNotificacion.nombre = 'PENDIENTE' and cn.fechaEnvio is null
              and n.fechaCreacion > :desde and n.fechaCreacion <= :hasta
              and (v is null or v.fechaHoraSalida > :hasta)
            order by n.fechaCreacion
            """)
    List<Notificacion> programadasPorEnviar(LocalDateTime desde, LocalDateTime hasta);
}
