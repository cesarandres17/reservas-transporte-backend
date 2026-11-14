package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Cliente;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    List<Cliente> findByRecibeNotificacionesTrue();
}
