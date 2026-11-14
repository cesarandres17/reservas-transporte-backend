package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.EquipajeRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TiqueteRepositorio;
import com.reservasvuelos.reservas_backend.model.Equipaje;
import com.reservasvuelos.reservas_backend.model.EquipajeId;
import com.reservasvuelos.reservas_backend.model.Tiquete;

@Service
public class EquipajeServicio extends CrudServicio<Equipaje, EquipajeId> {

    private final TiqueteRepositorio tiqueteRepositorio;

    public EquipajeServicio(EquipajeRepositorio repositorio, TiqueteRepositorio tiqueteRepositorio) {
        super(repositorio, "el equipaje");
        this.tiqueteRepositorio = tiqueteRepositorio;
    }

    public static EquipajeId id(Long codTiquete, Integer numeroPieza) {
        EquipajeId id = new EquipajeId();
        id.setTiquete(codTiquete);
        id.setNumeroPieza(numeroPieza);
        return id;
    }

    @Override
    protected void asignarId(Equipaje equipaje, EquipajeId id) {
    }

    @Override
    protected void completar(Equipaje equipaje, Equipaje actual) {
        if (actual != null) {
            equipaje.setTiquete(actual.getTiquete());
            equipaje.setNumeroPieza(actual.getNumeroPieza());
        } else {
            equipaje.setTiquete(obligatorio(referencia(equipaje.getTiquete(), Tiquete::getCodTiquete,
                    tiqueteRepositorio, "el tiquete"), "el tiquete"));
            obligatorio(equipaje.getNumeroPieza(), "el número de pieza");
        }
    }

    @Override
    protected void validarNuevo(Equipaje equipaje) {
        rechazarSiExiste(id(equipaje.getTiquete().getCodTiquete(), equipaje.getNumeroPieza()));
    }
}
