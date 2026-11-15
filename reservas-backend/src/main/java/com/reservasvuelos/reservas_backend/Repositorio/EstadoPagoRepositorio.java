package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.EstadoPago;

@Repository
public class EstadoPagoRepositorio extends RepositorioSql<EstadoPago, Long> {

    private static final String SELECCION = """
            SELECT e.cod_estado, e.nombre
            FROM estado_pago e
            """;

    public EstadoPagoRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected EstadoPago mapear(ResultSet fila) throws SQLException {
        EstadoPago estado = new EstadoPago();
        estado.setCodEstado(fila.getLong("cod_estado"));
        estado.setNombre(fila.getString("nombre"));
        return estado;
    }

    @Override
    public List<EstadoPago> listar() {
        return consultar(SELECCION + "ORDER BY e.cod_estado");
    }

    @Override
    public Optional<EstadoPago> buscarPorId(Long codEstado) {
        return consultarUno(SELECCION + "WHERE e.cod_estado = ?", codEstado);
    }

    public Map<Long, EstadoPago> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE e.cod_estado = ANY(?)", arreglo(codigos)),
                EstadoPago::getCodEstado);
    }

    public Optional<EstadoPago> buscarPorNombre(String nombre) {
        return consultarUno(SELECCION + "WHERE e.nombre = ?", nombre);
    }

    @Override
    public boolean existePorId(Long codEstado) {
        return existe("SELECT EXISTS (SELECT 1 FROM estado_pago WHERE cod_estado = ?)", codEstado);
    }

    @Override
    public EstadoPago insertar(EstadoPago estado) {
        estado.setCodEstado(insertarDevolviendoCodigo("""
                INSERT INTO estado_pago (nombre)
                VALUES (?)
                RETURNING cod_estado
                """, estado.getNombre()));
        return estado;
    }

    @Override
    public EstadoPago actualizar(EstadoPago estado) {
        jdbc.update("""
                UPDATE estado_pago
                SET nombre = ?
                WHERE cod_estado = ?
                """, estado.getNombre(), estado.getCodEstado());
        return estado;
    }

    @Override
    public void eliminar(Long codEstado) {
        jdbc.update("DELETE FROM estado_pago WHERE cod_estado = ?", codEstado);
    }
}
