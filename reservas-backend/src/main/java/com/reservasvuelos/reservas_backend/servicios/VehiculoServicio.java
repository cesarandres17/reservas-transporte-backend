package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.EmpresaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.VehiculoRepositorio;
import com.reservasvuelos.reservas_backend.model.Empresa;
import com.reservasvuelos.reservas_backend.model.Vehiculo;

@Service
public class VehiculoServicio extends CrudServicio<Vehiculo, Long> {

    private final EmpresaRepositorio empresaRepositorio;

    public VehiculoServicio(VehiculoRepositorio repositorio, EmpresaRepositorio empresaRepositorio) {
        super(repositorio, "el vehículo");
        this.empresaRepositorio = empresaRepositorio;
    }

    @Override
    protected void asignarId(Vehiculo vehiculo, Long id) {
        vehiculo.setCodVehiculo(id);
    }

    @Override
    protected void completar(Vehiculo vehiculo, Vehiculo actual) {
        vehiculo.setEmpresa(referencia(vehiculo.getEmpresa(), Empresa::getCodEmpresa,
                empresaRepositorio, "la empresa"));
    }
}
