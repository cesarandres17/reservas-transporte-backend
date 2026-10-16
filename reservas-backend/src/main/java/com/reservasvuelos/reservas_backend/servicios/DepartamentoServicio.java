package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.DepartamentoRepositorio;
import com.reservasvuelos.reservas_backend.model.Departamento;

@Service
public class DepartamentoServicio {

    @Autowired 
    private DepartamentoRepositorio departamentoRepositorio;


    public List<Departamento> buscarTodos() {
        return departamentoRepositorio.findAll();
    }

    public List<Departamento> buscarPorPais(Long codPais) {
        return departamentoRepositorio.findByPais_CodPais(codPais);
    }


    
}
