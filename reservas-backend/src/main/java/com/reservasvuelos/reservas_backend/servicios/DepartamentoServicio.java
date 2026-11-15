package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.DepartamentoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PaisRepositorio;
import com.reservasvuelos.reservas_backend.model.Departamento;
import com.reservasvuelos.reservas_backend.model.Pais;

@Service
public class DepartamentoServicio extends CrudServicio<Departamento, Long> {

    private final DepartamentoRepositorio departamentoRepositorio;
    private final PaisRepositorio paisRepositorio;

    public DepartamentoServicio(DepartamentoRepositorio repositorio, PaisRepositorio paisRepositorio) {
        super(repositorio, "el departamento");
        this.departamentoRepositorio = repositorio;
        this.paisRepositorio = paisRepositorio;
    }

    @Override
    protected void asignarId(Departamento departamento, Long id) {
        departamento.setCodDepartamento(id);
    }

    @Override
    protected void completar(Departamento departamento, Departamento actual) {
        departamento.setPais(referencia(departamento.getPais(), Pais::getCodPais, paisRepositorio, "el país"));
    }

    public List<Departamento> buscarPorPais(Long codPais) {
        return departamentoRepositorio.dePais(codPais);
    }
}
