package com.reservasvuelos.reservas_backend.Repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservasvuelos.reservas_backend.model.Departamento;

public interface DepartamentoRepositorio extends JpaRepository<Departamento, Long> {
    

    
    public List<Departamento> findByPais_CodPais(Long pais);
}
