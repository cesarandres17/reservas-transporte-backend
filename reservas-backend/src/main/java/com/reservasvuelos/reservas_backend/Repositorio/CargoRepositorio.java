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

@Repository
public class CargoRepositorio extends RepositorioSql<Cargo, Long> {

    private static final String SELECCION = """
            SELECT c.cod_cargo, c.nombre
            FROM cargo c
            """;

    public CargoRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected Cargo mapear(ResultSet fila) throws SQLException {
        Cargo cargo = new Cargo();
        cargo.setCodCargo(fila.getLong("cod_cargo"));
        cargo.setNombre(fila.getString("nombre"));
        return cargo;
    }

    @Override
    public List<Cargo> listar() {
        return consultar(SELECCION + "ORDER BY c.cod_cargo");
    }

    @Override
    public Optional<Cargo> buscarPorId(Long codCargo) {
        return consultarUno(SELECCION + "WHERE c.cod_cargo = ?", codCargo);
    }

    public Map<Long, Cargo> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE c.cod_cargo = ANY(?)", arreglo(codigos)), Cargo::getCodCargo);
    }

    @Override
    public boolean existePorId(Long codCargo) {
        return existe("SELECT EXISTS (SELECT 1 FROM cargo WHERE cod_cargo = ?)", codCargo);
    }

    @Override
    public Cargo insertar(Cargo cargo) {
        cargo.setCodCargo(insertarDevolviendoCodigo("""
                INSERT INTO cargo (nombre)
                VALUES (?)
                RETURNING cod_cargo
                """, cargo.getNombre()));
        return cargo;
    }

    @Override
    public Cargo actualizar(Cargo cargo) {
        jdbc.update("""
                UPDATE cargo
                SET nombre = ?
                WHERE cod_cargo = ?
                """, cargo.getNombre(), cargo.getCodCargo());
        return cargo;
    }

    @Override
    public void eliminar(Long codCargo) {
        jdbc.update("DELETE FROM cargo WHERE cod_cargo = ?", codCargo);
    }
}
