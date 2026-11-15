package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.AsientoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ClaseServicioRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.VehiculoRepositorio;
import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.model.AsientoId;
import com.reservasvuelos.reservas_backend.model.ClaseServicio;
import com.reservasvuelos.reservas_backend.model.Vehiculo;

@Service
public class AsientoServicio extends CrudServicio<Asiento, AsientoId> {

    private final VehiculoRepositorio vehiculoRepositorio;
    private final ClaseServicioRepositorio claseServicioRepositorio;

    public AsientoServicio(AsientoRepositorio repositorio,
            VehiculoRepositorio vehiculoRepositorio,
            ClaseServicioRepositorio claseServicioRepositorio) {
        super(repositorio, "el asiento");
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.claseServicioRepositorio = claseServicioRepositorio;
    }

    public static AsientoId id(Long codVehiculo, String numeroAsiento) {
        AsientoId id = new AsientoId();
        id.setVehiculo(codVehiculo);
        id.setNumeroAsiento(numeroAsiento);
        return id;
    }

    public static AsientoId id(Asiento asiento) {
        if (asiento.getVehiculo() == null || asiento.getVehiculo().getCodVehiculo() == null
                || asiento.getNumeroAsiento() == null) {
            return null;
        }
        return id(asiento.getVehiculo().getCodVehiculo(), asiento.getNumeroAsiento());
    }

    @Override
    protected void asignarId(Asiento asiento, AsientoId id) {
    }

    @Override
    protected void completar(Asiento asiento, Asiento actual) {
        if (actual != null) {
            asiento.setVehiculo(actual.getVehiculo());
            asiento.setNumeroAsiento(actual.getNumeroAsiento());
        } else {
            asiento.setVehiculo(obligatorio(referencia(asiento.getVehiculo(), Vehiculo::getCodVehiculo,
                    vehiculoRepositorio, "el vehículo"), "el vehículo"));
            obligatorio(asiento.getNumeroAsiento(), "el número del asiento");
        }
        asiento.setClaseServicio(referencia(asiento.getClaseServicio(), ClaseServicio::getCodClase,
                claseServicioRepositorio, "la clase de servicio"));
    }

    @Override
    protected void validarNuevo(Asiento asiento) {
        rechazarSiExiste(id(asiento));
    }
}
