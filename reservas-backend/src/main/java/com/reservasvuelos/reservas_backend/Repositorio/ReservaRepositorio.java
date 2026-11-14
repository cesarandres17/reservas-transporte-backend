package com.reservasvuelos.reservas_backend.Repositorio;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Reserva;

public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {

    boolean existsByCodigoReserva(String codigoReserva);

    List<Reserva> findByCliente_CodClienteOrderByFechaReservaDesc(Long codCliente);

    List<Reserva> findByEstado_NombreAndFechaLimitePagoBefore(String estado, LocalDateTime limite);
}
