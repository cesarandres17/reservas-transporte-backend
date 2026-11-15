package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.TipoTrayecto;

@Repository
public class TipoTrayectoRepositorio extends RepositorioSql<TipoTrayecto, Long> {

    private static final String SELECCION = """
            SELECT t.cod_tipo, t.nombre
            FROM tipo_trayecto t
            """;

    public TipoTrayectoRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected TipoTrayecto mapear(ResultSet fila) throws SQLException {
        TipoTrayecto tipo = new TipoTrayecto();
        tipo.setCodTipo(fila.getLong("cod_tipo"));
        tipo.setNombre(fila.getString("nombre"));
        return tipo;
    }

    @Override
    public List<TipoTrayecto> listar() {
        return consultar(SELECCION + "ORDER BY t.cod_tipo");
    }

    @Override
    public Optional<TipoTrayecto> buscarPorId(Long codTipo) {
        return consultarUno(SELECCION + "WHERE t.cod_tipo = ?", codTipo);
    }

    public Map<Long, TipoTrayecto> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE t.cod_tipo = ANY(?)", arreglo(codigos)), TipoTrayecto::getCodTipo);
    }

    public Optional<TipoTrayecto> buscarPorNombre(String nombre) {
        return consultarUno(SELECCION + "WHERE t.nombre = ?", nombre);
    }

    @Override
    public boolean existePorId(Long codTipo) {
        return existe("SELECT EXISTS (SELECT 1 FROM tipo_trayecto WHERE cod_tipo = ?)", codTipo);
    }

    @Override
    public TipoTrayecto insertar(TipoTrayecto tipo) {
        tipo.setCodTipo(insertarDevolviendoCodigo("""
                INSERT INTO tipo_trayecto (nombre)
                VALUES (?)
                RETURNING cod_tipo
                """, tipo.getNombre()));
        return tipo;
    }

    @Override
    public TipoTrayecto actualizar(TipoTrayecto tipo) {
        jdbc.update("""
                UPDATE tipo_trayecto
                SET nombre = ?
                WHERE cod_tipo = ?
                """, tipo.getNombre(), tipo.getCodTipo());
        return tipo;
    }

    @Override
    public void eliminar(Long codTipo) {
        jdbc.update("DELETE FROM tipo_trayecto WHERE cod_tipo = ?", codTipo);
    }
}
