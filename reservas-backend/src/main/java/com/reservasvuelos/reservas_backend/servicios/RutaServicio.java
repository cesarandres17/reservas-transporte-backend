package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.RutaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TerminalRepositorio;
import com.reservasvuelos.reservas_backend.model.Ruta;
import com.reservasvuelos.reservas_backend.model.Terminal;

@Service
public class RutaServicio extends CrudServicio<Ruta, Long> {

    private final TerminalRepositorio terminalRepositorio;

    public RutaServicio(RutaRepositorio repositorio, TerminalRepositorio terminalRepositorio) {
        super(repositorio, "la ruta");
        this.terminalRepositorio = terminalRepositorio;
    }

    @Override
    protected void asignarId(Ruta ruta, Long id) {
        ruta.setCodRuta(id);
    }

    @Override
    protected void completar(Ruta ruta, Ruta actual) {
        ruta.setTerminalOrigen(referencia(ruta.getTerminalOrigen(), Terminal::getCodTerminal,
                terminalRepositorio, "la terminal de origen"));
        ruta.setTerminalDestino(referencia(ruta.getTerminalDestino(), Terminal::getCodTerminal,
                terminalRepositorio, "la terminal de destino"));
    }
}
