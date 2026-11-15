package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.MetodoPago;
import com.reservasvuelos.reservas_backend.model.enums.TipoMetodoPago;

@Repository
public class MetodoPagoRepositorio extends RepositorioSql<MetodoPago, Long> {

    private static final String SELECCION = """
            SELECT m.cod_metodo, m.nombre, m.tipo, m.comision_pct
            FROM metodo_pago m
            """;

    public MetodoPagoRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected MetodoPago mapear(ResultSet fila) throws SQLException {
        MetodoPago metodo = new MetodoPago();
        metodo.setCodMetodo(fila.getLong("cod_metodo"));
        metodo.setNombre(fila.getString("nombre"));
        metodo.setTipo(enumerado(fila, "tipo", TipoMetodoPago.class));
        metodo.setComisionPct(fila.getBigDecimal("comision_pct"));
        return metodo;
    }

    @Override
    public List<MetodoPago> listar() {
        return consultar(SELECCION + "ORDER BY m.cod_metodo");
    }

    @Override
    public Optional<MetodoPago> buscarPorId(Long codMetodo) {
        return consultarUno(SELECCION + "WHERE m.cod_metodo = ?", codMetodo);
    }

    public Map<Long, MetodoPago> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE m.cod_metodo = ANY(?)", arreglo(codigos)),
                MetodoPago::getCodMetodo);
    }

    @Override
    public boolean existePorId(Long codMetodo) {
        return existe("SELECT EXISTS (SELECT 1 FROM metodo_pago WHERE cod_metodo = ?)", codMetodo);
    }

    @Override
    public MetodoPago insertar(MetodoPago metodo) {
        metodo.setCodMetodo(insertarDevolviendoCodigo("""
                INSERT INTO metodo_pago (nombre, tipo, comision_pct)
                VALUES (?, ?, ?)
                RETURNING cod_metodo
                """, metodo.getNombre(), nombre(metodo.getTipo()), metodo.getComisionPct()));
        return metodo;
    }

    @Override
    public MetodoPago actualizar(MetodoPago metodo) {
        jdbc.update("""
                UPDATE metodo_pago
                SET nombre = ?, tipo = ?, comision_pct = ?
                WHERE cod_metodo = ?
                """, metodo.getNombre(), nombre(metodo.getTipo()), metodo.getComisionPct(), metodo.getCodMetodo());
        return metodo;
    }

    @Override
    public void eliminar(Long codMetodo) {
        jdbc.update("DELETE FROM metodo_pago WHERE cod_metodo = ?", codMetodo);
    }
}
