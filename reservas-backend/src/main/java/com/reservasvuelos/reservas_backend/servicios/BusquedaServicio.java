package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.BusquedaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.CiudadRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.model.Busqueda;
import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.model.Cliente;

@Service
public class BusquedaServicio extends CrudServicio<Busqueda, Long> {

    private final ClienteRepositorio clienteRepositorio;
    private final CiudadRepositorio ciudadRepositorio;

    public BusquedaServicio(BusquedaRepositorio repositorio,
            ClienteRepositorio clienteRepositorio,
            CiudadRepositorio ciudadRepositorio) {
        super(repositorio, "la búsqueda");
        this.clienteRepositorio = clienteRepositorio;
        this.ciudadRepositorio = ciudadRepositorio;
    }

    @Override
    protected void asignarId(Busqueda busqueda, Long id) {
        busqueda.setCodBusqueda(id);
    }

    @Override
    protected void completar(Busqueda busqueda, Busqueda actual) {
        busqueda.setCliente(referencia(busqueda.getCliente(), Cliente::getCodCliente,
                clienteRepositorio, "el cliente"));
        busqueda.setCiudadOrigen(referencia(busqueda.getCiudadOrigen(), Ciudad::getCodCiudad,
                ciudadRepositorio, "la ciudad de origen"));
        busqueda.setCiudadDestino(referencia(busqueda.getCiudadDestino(), Ciudad::getCodCiudad,
                ciudadRepositorio, "la ciudad de destino"));
        if (busqueda.getFechaHoraBusqueda() == null) {
            busqueda.setFechaHoraBusqueda(actual == null ? LocalDateTime.now() : actual.getFechaHoraBusqueda());
        }
    }
}
