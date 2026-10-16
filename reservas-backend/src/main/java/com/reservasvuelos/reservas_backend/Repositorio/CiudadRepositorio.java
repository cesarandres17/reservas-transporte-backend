package com.reservasvuelos.reservas_backend.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;


import com.reservasvuelos.reservas_backend.model.Ciudad;

public interface CiudadRepositorio extends JpaRepository<Ciudad, Long>  {


    
}
