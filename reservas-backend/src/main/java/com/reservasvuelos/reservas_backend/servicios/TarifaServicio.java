package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.ClaseServicioRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TarifaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ViajeRepositorio;
import com.reservasvuelos.reservas_backend.model.ClaseServicio;
import com.reservasvuelos.reservas_backend.model.Tarifa;
import com.reservasvuelos.reservas_backend.model.TarifaId;
import com.reservasvuelos.reservas_backend.model.Viaje;

@Service
public class TarifaServicio extends CrudServicio<Tarifa, TarifaId> {

    private final ViajeRepositorio viajeRepositorio;
    private final ClaseServicioRepositorio claseServicioRepositorio;

    public TarifaServicio(TarifaRepositorio repositorio,
            ViajeRepositorio viajeRepositorio,
            ClaseServicioRepositorio claseServicioRepositorio) {
        super(repositorio, "la tarifa");
        this.viajeRepositorio = viajeRepositorio;
        this.claseServicioRepositorio = claseServicioRepositorio;
    }

    public static TarifaId id(Long codViaje, Long codClase) {
        TarifaId id = new TarifaId();
        id.setViaje(codViaje);
        id.setClaseServicio(codClase);
        return id;
    }

    public static TarifaId id(Tarifa tarifa) {
        if (tarifa.getViaje() == null || tarifa.getViaje().getCodViaje() == null
                || tarifa.getClaseServicio() == null || tarifa.getClaseServicio().getCodClase() == null) {
            return null;
        }
        return id(tarifa.getViaje().getCodViaje(), tarifa.getClaseServicio().getCodClase());
    }

    @Override
    protected void asignarId(Tarifa tarifa, TarifaId id) {
    }

    @Override
    protected void completar(Tarifa tarifa, Tarifa actual) {
        if (actual != null) {
            tarifa.setViaje(actual.getViaje());
            tarifa.setClaseServicio(actual.getClaseServicio());
        } else {
            tarifa.setViaje(obligatorio(referencia(tarifa.getViaje(), Viaje::getCodViaje,
                    viajeRepositorio, "el viaje"), "el viaje"));
            tarifa.setClaseServicio(obligatorio(referencia(tarifa.getClaseServicio(), ClaseServicio::getCodClase,
                    claseServicioRepositorio, "la clase de servicio"), "la clase de servicio"));
        }
    }

    @Override
    protected void validarNuevo(Tarifa tarifa) {
        rechazarSiExiste(id(tarifa));
    }
}
