package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.EstadoTiquete;

@Repository
public class EstadoTiqueteRepositorio extends RepositorioSql<EstadoTiquete, Long> {

    private static final String SELECCION = """
            SELECT e.cod_estado, e.nombre
            FROM estado_tiquete e
            """;

    public EstadoTiqueteRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected EstadoTiquete mapear(ResultSet fila) throws SQLException {
        EstadoTiquete estado = new EstadoTiquete();
        estado.setCodEstado(fila.getLong("cod_estado"));
        estado.setNombre(fila.getString("nombre"));
        return estado;
    }

    @Override
    public List<EstadoTiquete> listar() {
        return consultar(SELECCION + "ORDER BY e.cod_estado");
    }

    @Override
    public Optional<EstadoTiquete> buscarPorId(Long codEstado) {
        return consultarUno(SELECCION + "WHERE e.cod_estado = ?", codEstado);
    }

    public Map<Long, EstadoTiquete> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE e.cod_estado = ANY(?)", arreglo(codigos)),
                EstadoTiquete::getCodEstado);
    }

    public Optional<EstadoTiquete> buscarPorNombre(String nombre) {
        return consultarUno(SELECCION + "WHERE e.nombre = ?", nombre);
    }

    @Override
    public boolean existePorId(Long codEstado) {
        return existe("SELECT EXISTS (SELECT 1 FROM estado_tiquete WHERE cod_estado = ?)", codEstado);
    }

    @Override
    public EstadoTiquete insertar(EstadoTiquete estado) {
        estado.setCodEstado(insertarDevolviendoCodigo("""
                INSERT INTO estado_tiquete (nombre)
                VALUES (?)
                RETURNING cod_estado
                """, estado.getNombre()));
        return estado;
    }

    @Override
    public EstadoTiquete actualizar(EstadoTiquete estado) {
        jdbc.update("""
                UPDATE estado_tiquete
                SET nombre = ?
                WHERE cod_estado = ?
                """, estado.getNombre(), estado.getCodEstado());
        return estado;
    }

    @Override
    public void eliminar(Long codEstado) {
        jdbc.update("DELETE FROM estado_tiquete WHERE cod_estado = ?", codEstado);
    }
}
