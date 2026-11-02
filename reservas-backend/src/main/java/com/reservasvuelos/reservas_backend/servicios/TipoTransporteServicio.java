package com.reservasvuelos.reservas_backend.servicios;
import java.util.List;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.TipoTransporteRepositorio;
import com.reservasvuelos.reservas_backend.model.TipoTransporte;

@Service
public class TipoTransporteServicio {
    
    private final TipoTransporteRepositorio tipoTransporteRepositorio;
    
    public TipoTransporteServicio(TipoTransporteRepositorio tipoTransporteRepositorio) {
        this.tipoTransporteRepositorio = tipoTransporteRepositorio;
    }
    
    public List<TipoTransporte> findAll() {
        return tipoTransporteRepositorio.findAll();
    }
    
}
