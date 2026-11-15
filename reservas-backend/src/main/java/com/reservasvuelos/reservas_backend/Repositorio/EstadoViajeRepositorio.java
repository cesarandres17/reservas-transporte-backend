package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.EstadoViaje;

@Repository
public class EstadoViajeRepositorio extends RepositorioSql<EstadoViaje, Long> {

    private static final String SELECCION = """
            SELECT e.cod_estado, e.nombre
            FROM estado_viaje e
            """;

    public EstadoViajeRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected EstadoViaje mapear(ResultSet fila) throws SQLException {
        EstadoViaje estado = new EstadoViaje();
        estado.setCodEstado(fila.getLong("cod_estado"));
        estado.setNombre(fila.getString("nombre"));
        return estado;
    }

    @Override
    public List<EstadoViaje> listar() {
        return consultar(SELECCION + "ORDER BY e.cod_estado");
    }

    @Override
    public Optional<EstadoViaje> buscarPorId(Long codEstado) {
        return consultarUno(SELECCION + "WHERE e.cod_estado = ?", codEstado);
    }

    public Map<Long, EstadoViaje> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE e.cod_estado = ANY(?)", arreglo(codigos)),
                EstadoViaje::getCodEstado);
    }

    @Override
    public boolean existePorId(Long codEstado) {
        return existe("SELECT EXISTS (SELECT 1 FROM estado_viaje WHERE cod_estado = ?)", codEstado);
    }

    @Override
    public EstadoViaje insertar(EstadoViaje estado) {
        estado.setCodEstado(insertarDevolviendoCodigo("""
                INSERT INTO estado_viaje (nombre)
                VALUES (?)
                RETURNING cod_estado
                """, estado.getNombre()));
        return estado;
    }

    @Override
    public EstadoViaje actualizar(EstadoViaje estado) {
        jdbc.update("""
                UPDATE estado_viaje
                SET nombre = ?
                WHERE cod_estado = ?
                """, estado.getNombre(), estado.getCodEstado());
        return estado;
    }

    @Override
    public void eliminar(Long codEstado) {
        jdbc.update("DELETE FROM estado_viaje WHERE cod_estado = ?", codEstado);
    }
}
