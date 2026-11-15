package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Empresa;
import com.reservasvuelos.reservas_backend.model.Vehiculo;
import com.reservasvuelos.reservas_backend.model.enums.EstadoVehiculo;

@Repository
public class VehiculoRepositorio extends RepositorioSql<Vehiculo, Long> {

    private static final String SELECCION = """
            SELECT v.cod_vehiculo, v.matricula, v.modelo, v.fabricante, v.capacidad, v.anio_fabricacion, v.estado,
                   v.cod_empresa
            FROM vehiculo v
            """;

    private final EmpresaRepositorio empresaRepositorio;

    public VehiculoRepositorio(JdbcTemplate jdbc, EmpresaRepositorio empresaRepositorio) {
        super(jdbc);
        this.empresaRepositorio = empresaRepositorio;
    }

    @Override
    protected Vehiculo mapear(ResultSet fila) throws SQLException {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setCodVehiculo(fila.getLong("cod_vehiculo"));
        vehiculo.setMatricula(fila.getString("matricula"));
        vehiculo.setModelo(fila.getString("modelo"));
        vehiculo.setFabricante(fila.getString("fabricante"));
        vehiculo.setCapacidad(entero(fila, "capacidad"));
        vehiculo.setAnioFabricacion(entero(fila, "anio_fabricacion"));
        vehiculo.setEstado(enumerado(fila, "estado", EstadoVehiculo.class));
        vehiculo.setEmpresa(referencia(Empresa::new, Empresa::setCodEmpresa, largo(fila, "cod_empresa")));
        return vehiculo;
    }

    @Override
    protected List<Vehiculo> completar(List<Vehiculo> vehiculos) {
        cargar(vehiculos, Vehiculo::getEmpresa, Empresa::getCodEmpresa, empresaRepositorio::porCodigos,
                Vehiculo::setEmpresa);
        return vehiculos;
    }

    @Override
    public List<Vehiculo> listar() {
        return consultar(SELECCION + "ORDER BY v.cod_vehiculo");
    }

    @Override
    public Optional<Vehiculo> buscarPorId(Long codVehiculo) {
        return consultarUno(SELECCION + "WHERE v.cod_vehiculo = ?", codVehiculo);
    }

    public Map<Long, Vehiculo> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE v.cod_vehiculo = ANY(?)", arreglo(codigos)),
                Vehiculo::getCodVehiculo);
    }

    @Override
    public boolean existePorId(Long codVehiculo) {
        return existe("SELECT EXISTS (SELECT 1 FROM vehiculo WHERE cod_vehiculo = ?)", codVehiculo);
    }

    @Override
    public Vehiculo insertar(Vehiculo vehiculo) {
        vehiculo.setCodVehiculo(insertarDevolviendoCodigo("""
                INSERT INTO vehiculo (matricula, modelo, fabricante, capacidad, anio_fabricacion, estado, cod_empresa)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING cod_vehiculo
                """, vehiculo.getMatricula(), vehiculo.getModelo(), vehiculo.getFabricante(), vehiculo.getCapacidad(),
                vehiculo.getAnioFabricacion(), nombre(vehiculo.getEstado()),
                codigo(vehiculo.getEmpresa(), Empresa::getCodEmpresa)));
        return vehiculo;
    }

    @Override
    public Vehiculo actualizar(Vehiculo vehiculo) {
        jdbc.update("""
                UPDATE vehiculo
                SET matricula = ?, modelo = ?, fabricante = ?, capacidad = ?, anio_fabricacion = ?, estado = ?,
                    cod_empresa = ?
                WHERE cod_vehiculo = ?
                """, vehiculo.getMatricula(), vehiculo.getModelo(), vehiculo.getFabricante(), vehiculo.getCapacidad(),
                vehiculo.getAnioFabricacion(), nombre(vehiculo.getEstado()),
                codigo(vehiculo.getEmpresa(), Empresa::getCodEmpresa), vehiculo.getCodVehiculo());
        return vehiculo;
    }

    @Override
    public void eliminar(Long codVehiculo) {
        jdbc.update("DELETE FROM vehiculo WHERE cod_vehiculo = ?", codVehiculo);
    }
}
