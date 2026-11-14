package com.reservasvuelos.reservas_backend.Repositorio;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.reservasvuelos.reservas_backend.model.ClienteNotificacion;
import com.reservasvuelos.reservas_backend.model.ClienteNotificacionId;

public interface ClienteNotificacionRepositorio extends JpaRepository<ClienteNotificacion, ClienteNotificacionId> {

    @Query("""
            select cn from ClienteNotificacion cn
            join fetch cn.notificacion n join fetch n.tipoNotificacion join fetch cn.estadoNotificacion
            where cn.cliente.codCliente = :codCliente and n.fechaCreacion <= :hasta
            order by n.fechaCreacion desc, n.codNotificacion desc
            """)
    List<ClienteNotificacion> deCliente(Long codCliente, LocalDateTime hasta);

    @Query("""
            select cn from ClienteNotificacion cn
            join fetch cn.cliente c join fetch c.usuario join fetch cn.estadoNotificacion
            where cn.notificacion.codNotificacion = :codNotificacion
            order by c.codCliente
            """)
    List<ClienteNotificacion> destinatarios(Long codNotificacion);

    @Query("""
            select count(cn) > 0 from ClienteNotificacion cn
            where cn.cliente.codCliente = :codCliente and cn.notificacion.viaje.codViaje = :codViaje
              and cn.notificacion.tipoNotificacion.nombre = :tipo
            """)
    boolean yaNotificado(Long codCliente, Long codViaje, String tipo);
}
