package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.TipoDocumento;

@Repository
public class TipoDocumentoRepositorio extends RepositorioSql<TipoDocumento, Long> {

    private static final String SELECCION = """
            SELECT t.cod_tipo, t.nombre, t.sigla
            FROM tipo_documento t
            """;

    public TipoDocumentoRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected TipoDocumento mapear(ResultSet fila) throws SQLException {
        TipoDocumento tipo = new TipoDocumento();
        tipo.setCodTipo(fila.getLong("cod_tipo"));
        tipo.setNombre(fila.getString("nombre"));
        tipo.setSigla(fila.getString("sigla"));
        return tipo;
    }

    @Override
    public List<TipoDocumento> listar() {
        return consultar(SELECCION + "ORDER BY t.cod_tipo");
    }

    @Override
    public Optional<TipoDocumento> buscarPorId(Long codTipo) {
        return consultarUno(SELECCION + "WHERE t.cod_tipo = ?", codTipo);
    }

    public Map<Long, TipoDocumento> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE t.cod_tipo = ANY(?)", arreglo(codigos)),
                TipoDocumento::getCodTipo);
    }

    public Optional<TipoDocumento> buscarPorSigla(String sigla) {
        return consultarUno(SELECCION + "WHERE UPPER(t.sigla) = UPPER(?)", sigla);
    }

    @Override
    public boolean existePorId(Long codTipo) {
        return existe("SELECT EXISTS (SELECT 1 FROM tipo_documento WHERE cod_tipo = ?)", codTipo);
    }

    @Override
    public TipoDocumento insertar(TipoDocumento tipo) {
        tipo.setCodTipo(insertarDevolviendoCodigo("""
                INSERT INTO tipo_documento (nombre, sigla)
                VALUES (?, ?)
                RETURNING cod_tipo
                """, tipo.getNombre(), tipo.getSigla()));
        return tipo;
    }

    @Override
    public TipoDocumento actualizar(TipoDocumento tipo) {
        jdbc.update("""
                UPDATE tipo_documento
                SET nombre = ?, sigla = ?
                WHERE cod_tipo = ?
                """, tipo.getNombre(), tipo.getSigla(), tipo.getCodTipo());
        return tipo;
    }

    @Override
    public void eliminar(Long codTipo) {
        jdbc.update("DELETE FROM tipo_documento WHERE cod_tipo = ?", codTipo);
    }
}
