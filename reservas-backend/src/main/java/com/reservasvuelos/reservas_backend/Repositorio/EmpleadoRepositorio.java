package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Cargo;
import com.reservasvuelos.reservas_backend.model.Empleado;
import com.reservasvuelos.reservas_backend.model.Empresa;
import com.reservasvuelos.reservas_backend.model.Usuario;

@Repository
public class EmpleadoRepositorio extends RepositorioSql<Empleado, Long> {

    private static final String SELECCION = """
            SELECT e.cod_empleado, e.cod_cargo, e.salario, e.fecha_ingreso, e.cod_empresa
            FROM empleado e
            """;

    private final UsuarioRepositorio usuarioRepositorio;
    private final CargoRepositorio cargoRepositorio;
    private final EmpresaRepositorio empresaRepositorio;

    public EmpleadoRepositorio(JdbcTemplate jdbc, UsuarioRepositorio usuarioRepositorio,
            CargoRepositorio cargoRepositorio, EmpresaRepositorio empresaRepositorio) {
        super(jdbc);
        this.usuarioRepositorio = usuarioRepositorio;
        this.cargoRepositorio = cargoRepositorio;
        this.empresaRepositorio = empresaRepositorio;
    }

    @Override
    protected Empleado mapear(ResultSet fila) throws SQLException {
        Empleado empleado = new Empleado();
        empleado.setCodEmpleado(fila.getLong("cod_empleado"));
        empleado.setUsuario(referencia(Usuario::new, Usuario::setCodUsuario, empleado.getCodEmpleado()));
        empleado.setCargo(referencia(Cargo::new, Cargo::setCodCargo, largo(fila, "cod_cargo")));
        empleado.setSalario(fila.getBigDecimal("salario"));
        empleado.setFechaIngreso(fecha(fila, "fecha_ingreso"));
        empleado.setEmpresa(referencia(Empresa::new, Empresa::setCodEmpresa, largo(fila, "cod_empresa")));
        return empleado;
    }

    @Override
    protected List<Empleado> completar(List<Empleado> empleados) {
        cargar(empleados, Empleado::getUsuario, Usuario::getCodUsuario, usuarioRepositorio::porCodigos,
                Empleado::setUsuario);
        cargar(empleados, Empleado::getCargo, Cargo::getCodCargo, cargoRepositorio::porCodigos, Empleado::setCargo);
        cargar(empleados, Empleado::getEmpresa, Empresa::getCodEmpresa, empresaRepositorio::porCodigos,
                Empleado::setEmpresa);
        return empleados;
    }

    @Override
    public List<Empleado> listar() {
        return consultar(SELECCION + "ORDER BY e.cod_empleado");
    }

    @Override
    public Optional<Empleado> buscarPorId(Long codEmpleado) {
        return consultarUno(SELECCION + "WHERE e.cod_empleado = ?", codEmpleado);
    }

    public Map<Long, Empleado> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE e.cod_empleado = ANY(?)", arreglo(codigos)),
                Empleado::getCodEmpleado);
    }

    @Override
    public boolean existePorId(Long codEmpleado) {
        return existe("SELECT EXISTS (SELECT 1 FROM empleado WHERE cod_empleado = ?)", codEmpleado);
    }

    @Override
    public Empleado insertar(Empleado empleado) {
        empleado.setCodEmpleado(empleado.getUsuario().getCodUsuario());
        jdbc.update("""
                INSERT INTO empleado (cod_empleado, cod_cargo, salario, fecha_ingreso, cod_empresa)
                VALUES (?, ?, ?, ?, ?)
                """, empleado.getCodEmpleado(), codigo(empleado.getCargo(), Cargo::getCodCargo),
                empleado.getSalario(), empleado.getFechaIngreso(),
                codigo(empleado.getEmpresa(), Empresa::getCodEmpresa));
        return empleado;
    }

    @Override
    public Empleado actualizar(Empleado empleado) {
        jdbc.update("""
                UPDATE empleado
                SET cod_cargo = ?, salario = ?, fecha_ingreso = ?, cod_empresa = ?
                WHERE cod_empleado = ?
                """, codigo(empleado.getCargo(), Cargo::getCodCargo), empleado.getSalario(),
                empleado.getFechaIngreso(), codigo(empleado.getEmpresa(), Empresa::getCodEmpresa),
                empleado.getCodEmpleado());
        return empleado;
    }

    @Override
    public void eliminar(Long codEmpleado) {
        jdbc.update("DELETE FROM empleado WHERE cod_empleado = ?", codEmpleado);
    }
}
