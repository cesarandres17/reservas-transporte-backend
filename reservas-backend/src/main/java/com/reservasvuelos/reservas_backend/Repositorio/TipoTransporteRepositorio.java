package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.TipoTransporte;

@Repository
public class TipoTransporteRepositorio extends RepositorioSql<TipoTransporte, Long> {

    private static final String SELECCION = """
            SELECT t.cod_tipo, t.nombre, t.descripcion
            FROM tipo_transporte t
            """;

    public TipoTransporteRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected TipoTransporte mapear(ResultSet fila) throws SQLException {
        TipoTransporte tipo = new TipoTransporte();
        tipo.setCodTipo(fila.getLong("cod_tipo"));
        tipo.setNombre(fila.getString("nombre"));
        tipo.setDescripcion(fila.getString("descripcion"));
        return tipo;
    }

    @Override
    public List<TipoTransporte> listar() {
        return consultar(SELECCION + "ORDER BY t.cod_tipo");
    }

    @Override
    public Optional<TipoTransporte> buscarPorId(Long codTipo) {
        return consultarUno(SELECCION + "WHERE t.cod_tipo = ?", codTipo);
    }

    public Map<Long, TipoTransporte> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE t.cod_tipo = ANY(?)", arreglo(codigos)),
                TipoTransporte::getCodTipo);
    }

    @Override
    public boolean existePorId(Long codTipo) {
        return existe("SELECT EXISTS (SELECT 1 FROM tipo_transporte WHERE cod_tipo = ?)", codTipo);
    }

    @Override
    public TipoTransporte insertar(TipoTransporte tipo) {
        tipo.setCodTipo(insertarDevolviendoCodigo("""
                INSERT INTO tipo_transporte (nombre, descripcion)
                VALUES (?, ?)
                RETURNING cod_tipo
                """, tipo.getNombre(), tipo.getDescripcion()));
        return tipo;
    }

    @Override
    public TipoTransporte actualizar(TipoTransporte tipo) {
        jdbc.update("""
                UPDATE tipo_transporte
                SET nombre = ?, descripcion = ?
                WHERE cod_tipo = ?
                """, tipo.getNombre(), tipo.getDescripcion(), tipo.getCodTipo());
        return tipo;
    }

    @Override
    public void eliminar(Long codTipo) {
        jdbc.update("DELETE FROM tipo_transporte WHERE cod_tipo = ?", codTipo);
    }
}
