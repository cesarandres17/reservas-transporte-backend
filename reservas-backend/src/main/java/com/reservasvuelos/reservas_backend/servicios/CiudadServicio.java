package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.CiudadRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.DepartamentoRepositorio;
import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.model.Departamento;

@Service
public class CiudadServicio extends CrudServicio<Ciudad, Long> {

    private final CiudadRepositorio ciudadRepositorio;
    private final DepartamentoRepositorio departamentoRepositorio;

    public CiudadServicio(CiudadRepositorio repositorio, DepartamentoRepositorio departamentoRepositorio) {
        super(repositorio, "la ciudad");
        this.ciudadRepositorio = repositorio;
        this.departamentoRepositorio = departamentoRepositorio;
    }

    @Override
    protected void asignarId(Ciudad ciudad, Long id) {
        ciudad.setCodCiudad(id);
    }

    @Override
    protected void completar(Ciudad ciudad, Ciudad actual) {
        ciudad.setDepartamento(referencia(ciudad.getDepartamento(), Departamento::getCodDepartamento,
                departamentoRepositorio, "el departamento"));
    }

    public List<Ciudad> buscarPorDepartamento(Long codDepartamento) {
        return ciudadRepositorio.findByDepartamento_CodDepartamento(codDepartamento);
    }
}
