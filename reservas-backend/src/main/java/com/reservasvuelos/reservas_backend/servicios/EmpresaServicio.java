package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.EmpresaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PaisRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoTransporteRepositorio;
import com.reservasvuelos.reservas_backend.model.Empresa;
import com.reservasvuelos.reservas_backend.model.Pais;
import com.reservasvuelos.reservas_backend.model.TipoTransporte;

@Service
public class EmpresaServicio extends CrudServicio<Empresa, Long> {

    private final TipoTransporteRepositorio tipoTransporteRepositorio;
    private final PaisRepositorio paisRepositorio;

    public EmpresaServicio(EmpresaRepositorio repositorio,
            TipoTransporteRepositorio tipoTransporteRepositorio,
            PaisRepositorio paisRepositorio) {
        super(repositorio, "la empresa");
        this.tipoTransporteRepositorio = tipoTransporteRepositorio;
        this.paisRepositorio = paisRepositorio;
    }

    @Override
    protected void asignarId(Empresa empresa, Long id) {
        empresa.setCodEmpresa(id);
    }

    @Override
    protected void completar(Empresa empresa, Empresa actual) {
        empresa.setTipoTransporte(referencia(empresa.getTipoTransporte(), TipoTransporte::getCodTipo,
                tipoTransporteRepositorio, "el tipo de transporte"));
        empresa.setPais(referencia(empresa.getPais(), Pais::getCodPais, paisRepositorio, "el país"));
    }
}
