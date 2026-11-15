package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.EstadoNotificacion;

@Repository
public class EstadoNotificacionRepositorio extends RepositorioSql<EstadoNotificacion, Long> {

    private static final String SELECCION = """
            SELECT e.cod_estado, e.nombre
            FROM estado_notificacion e
            """;

    public EstadoNotificacionRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected EstadoNotificacion mapear(ResultSet fila) throws SQLException {
        EstadoNotificacion estado = new EstadoNotificacion();
        estado.setCodEstado(fila.getLong("cod_estado"));
        estado.setNombre(fila.getString("nombre"));
        return estado;
    }

    @Override
    public List<EstadoNotificacion> listar() {
        return consultar(SELECCION + "ORDER BY e.cod_estado");
    }

    @Override
    public Optional<EstadoNotificacion> buscarPorId(Long codEstado) {
        return consultarUno(SELECCION + "WHERE e.cod_estado = ?", codEstado);
    }

    public Map<Long, EstadoNotificacion> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE e.cod_estado = ANY(?)", arreglo(codigos)),
                EstadoNotificacion::getCodEstado);
    }

    public Optional<EstadoNotificacion> buscarPorNombre(String nombre) {
        return consultarUno(SELECCION + "WHERE e.nombre = ?", nombre);
    }

    @Override
    public boolean existePorId(Long codEstado) {
        return existe("SELECT EXISTS (SELECT 1 FROM estado_notificacion WHERE cod_estado = ?)", codEstado);
    }

    @Override
    public EstadoNotificacion insertar(EstadoNotificacion estado) {
        estado.setCodEstado(insertarDevolviendoCodigo("""
                INSERT INTO estado_notificacion (nombre)
                VALUES (?)
                RETURNING cod_estado
                """, estado.getNombre()));
        return estado;
    }

    @Override
    public EstadoNotificacion actualizar(EstadoNotificacion estado) {
        jdbc.update("""
                UPDATE estado_notificacion
                SET nombre = ?
                WHERE cod_estado = ?
                """, estado.getNombre(), estado.getCodEstado());
        return estado;
    }

    @Override
    public void eliminar(Long codEstado) {
        jdbc.update("DELETE FROM estado_notificacion WHERE cod_estado = ?", codEstado);
    }
}
