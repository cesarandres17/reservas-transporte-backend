package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.EstadoViajeRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.RutaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.VehiculoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ViajeRepositorio;
import com.reservasvuelos.reservas_backend.model.EstadoViaje;
import com.reservasvuelos.reservas_backend.model.Ruta;
import com.reservasvuelos.reservas_backend.model.Vehiculo;
import com.reservasvuelos.reservas_backend.model.Viaje;

@Service
public class ViajeServicio extends CrudServicio<Viaje, Long> {

    private final EstadoViajeRepositorio estadoViajeRepositorio;
    private final RutaRepositorio rutaRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;

    public ViajeServicio(ViajeRepositorio repositorio,
            EstadoViajeRepositorio estadoViajeRepositorio,
            RutaRepositorio rutaRepositorio,
            VehiculoRepositorio vehiculoRepositorio) {
        super(repositorio, "el viaje");
        this.estadoViajeRepositorio = estadoViajeRepositorio;
        this.rutaRepositorio = rutaRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
    }

    @Override
    protected void asignarId(Viaje viaje, Long id) {
        viaje.setCodViaje(id);
    }

    @Override
    protected void completar(Viaje viaje, Viaje actual) {
        viaje.setEstado(referencia(viaje.getEstado(), EstadoViaje::getCodEstado,
                estadoViajeRepositorio, "el estado de viaje"));
        viaje.setRuta(referencia(viaje.getRuta(), Ruta::getCodRuta, rutaRepositorio, "la ruta"));
        viaje.setVehiculo(referencia(viaje.getVehiculo(), Vehiculo::getCodVehiculo,
                vehiculoRepositorio, "el vehículo"));
    }
}
