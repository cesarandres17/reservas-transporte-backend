package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.TipoNotificacion;

@Repository
public class TipoNotificacionRepositorio extends RepositorioSql<TipoNotificacion, Long> {

    private static final String SELECCION = """
            SELECT t.cod_tipo, t.nombre
            FROM tipo_notificacion t
            """;

    public TipoNotificacionRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected TipoNotificacion mapear(ResultSet fila) throws SQLException {
        TipoNotificacion tipo = new TipoNotificacion();
        tipo.setCodTipo(fila.getLong("cod_tipo"));
        tipo.setNombre(fila.getString("nombre"));
        return tipo;
    }

    @Override
    public List<TipoNotificacion> listar() {
        return consultar(SELECCION + "ORDER BY t.cod_tipo");
    }

    @Override
    public Optional<TipoNotificacion> buscarPorId(Long codTipo) {
        return consultarUno(SELECCION + "WHERE t.cod_tipo = ?", codTipo);
    }

    public Map<Long, TipoNotificacion> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE t.cod_tipo = ANY(?)", arreglo(codigos)),
                TipoNotificacion::getCodTipo);
    }

    public Optional<TipoNotificacion> buscarPorNombre(String nombre) {
        return consultarUno(SELECCION + "WHERE t.nombre = ?", nombre);
    }

    @Override
    public boolean existePorId(Long codTipo) {
        return existe("SELECT EXISTS (SELECT 1 FROM tipo_notificacion WHERE cod_tipo = ?)", codTipo);
    }

    @Override
    public TipoNotificacion insertar(TipoNotificacion tipo) {
        tipo.setCodTipo(insertarDevolviendoCodigo("""
                INSERT INTO tipo_notificacion (nombre)
                VALUES (?)
                RETURNING cod_tipo
                """, tipo.getNombre()));
        return tipo;
    }

    @Override
    public TipoNotificacion actualizar(TipoNotificacion tipo) {
        jdbc.update("""
                UPDATE tipo_notificacion
                SET nombre = ?
                WHERE cod_tipo = ?
                """, tipo.getNombre(), tipo.getCodTipo());
        return tipo;
    }

    @Override
    public void eliminar(Long codTipo) {
        jdbc.update("DELETE FROM tipo_notificacion WHERE cod_tipo = ?", codTipo);
    }
}
