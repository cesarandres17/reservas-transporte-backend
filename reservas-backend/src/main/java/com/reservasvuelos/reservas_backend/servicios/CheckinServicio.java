package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.CheckinRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EmpleadoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TiqueteRepositorio;
import com.reservasvuelos.reservas_backend.model.Checkin;
import com.reservasvuelos.reservas_backend.model.Empleado;
import com.reservasvuelos.reservas_backend.model.Tiquete;

@Service
public class CheckinServicio extends CrudServicio<Checkin, Long> {

    private final TiqueteRepositorio tiqueteRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    public CheckinServicio(CheckinRepositorio repositorio,
            TiqueteRepositorio tiqueteRepositorio,
            EmpleadoRepositorio empleadoRepositorio) {
        super(repositorio, "el check-in");
        this.tiqueteRepositorio = tiqueteRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
    }

    @Override
    protected void asignarId(Checkin checkin, Long id) {
        checkin.setCodTiquete(id);
    }

    @Override
    protected void completar(Checkin checkin, Checkin actual) {
        if (actual != null) {
            checkin.setTiquete(actual.getTiquete());
        } else {
            checkin.setTiquete(obligatorio(referencia(checkin.getTiquete(), Tiquete::getCodTiquete,
                    tiqueteRepositorio, "el tiquete"), "el tiquete"));
        }
        checkin.setEmpleado(referencia(checkin.getEmpleado(), Empleado::getCodEmpleado,
                empleadoRepositorio, "el empleado"));
        if (checkin.getFechaHora() == null) {
            checkin.setFechaHora(actual == null ? LocalDateTime.now() : actual.getFechaHora());
        }
    }

    @Override
    protected void validarNuevo(Checkin checkin) {
        rechazarSiExiste(checkin.getTiquete().getCodTiquete());
    }
}
