package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.CiudadRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TerminalRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoTransporteRepositorio;
import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.model.Terminal;
import com.reservasvuelos.reservas_backend.model.TipoTransporte;

@Service
public class TerminalServicio extends CrudServicio<Terminal, Long> {

    private final CiudadRepositorio ciudadRepositorio;
    private final TipoTransporteRepositorio tipoTransporteRepositorio;

    public TerminalServicio(TerminalRepositorio repositorio,
            CiudadRepositorio ciudadRepositorio,
            TipoTransporteRepositorio tipoTransporteRepositorio) {
        super(repositorio, "la terminal");
        this.ciudadRepositorio = ciudadRepositorio;
        this.tipoTransporteRepositorio = tipoTransporteRepositorio;
    }

    @Override
    protected void asignarId(Terminal terminal, Long id) {
        terminal.setCodTerminal(id);
    }

    @Override
    protected void completar(Terminal terminal, Terminal actual) {
        terminal.setCiudad(referencia(terminal.getCiudad(), Ciudad::getCodCiudad, ciudadRepositorio, "la ciudad"));
        terminal.setTipoTransporte(referencia(terminal.getTipoTransporte(), TipoTransporte::getCodTipo,
                tipoTransporteRepositorio, "el tipo de transporte"));
    }
}
