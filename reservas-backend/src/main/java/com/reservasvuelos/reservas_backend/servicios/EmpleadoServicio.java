package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.CargoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EmpleadoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EmpresaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.UsuarioRepositorio;
import com.reservasvuelos.reservas_backend.model.Cargo;
import com.reservasvuelos.reservas_backend.model.Empleado;
import com.reservasvuelos.reservas_backend.model.Empresa;
import com.reservasvuelos.reservas_backend.model.Usuario;

@Service
public class EmpleadoServicio extends CrudServicio<Empleado, Long> {

    private final UsuarioRepositorio usuarioRepositorio;
    private final CargoRepositorio cargoRepositorio;
    private final EmpresaRepositorio empresaRepositorio;

    public EmpleadoServicio(EmpleadoRepositorio repositorio,
            UsuarioRepositorio usuarioRepositorio,
            CargoRepositorio cargoRepositorio,
            EmpresaRepositorio empresaRepositorio) {
        super(repositorio, "el empleado");
        this.usuarioRepositorio = usuarioRepositorio;
        this.cargoRepositorio = cargoRepositorio;
        this.empresaRepositorio = empresaRepositorio;
    }

    @Override
    protected void asignarId(Empleado empleado, Long id) {
        empleado.setCodEmpleado(id);
    }

    @Override
    protected void completar(Empleado empleado, Empleado actual) {
        if (actual != null) {
            empleado.setUsuario(actual.getUsuario());
        } else {
            empleado.setUsuario(obligatorio(referencia(empleado.getUsuario(), Usuario::getCodUsuario,
                    usuarioRepositorio, "el usuario"), "el usuario"));
        }
        empleado.setCargo(referencia(empleado.getCargo(), Cargo::getCodCargo, cargoRepositorio, "el cargo"));
        empleado.setEmpresa(referencia(empleado.getEmpresa(), Empresa::getCodEmpresa, empresaRepositorio, "la empresa"));
    }

    @Override
    protected void validarNuevo(Empleado empleado) {
        rechazarSiExiste(empleado.getUsuario().getCodUsuario());
    }
}
