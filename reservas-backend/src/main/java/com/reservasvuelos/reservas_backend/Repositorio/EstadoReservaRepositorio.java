package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.EstadoReserva;

@Repository
public class EstadoReservaRepositorio extends RepositorioSql<EstadoReserva, Long> {

    private static final String SELECCION = """
            SELECT e.cod_estado, e.nombre
            FROM estado_reserva e
            """;

    public EstadoReservaRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected EstadoReserva mapear(ResultSet fila) throws SQLException {
        EstadoReserva estado = new EstadoReserva();
        estado.setCodEstado(fila.getLong("cod_estado"));
        estado.setNombre(fila.getString("nombre"));
        return estado;
    }

    @Override
    public List<EstadoReserva> listar() {
        return consultar(SELECCION + "ORDER BY e.cod_estado");
    }

    @Override
    public Optional<EstadoReserva> buscarPorId(Long codEstado) {
        return consultarUno(SELECCION + "WHERE e.cod_estado = ?", codEstado);
    }

    public Map<Long, EstadoReserva> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE e.cod_estado = ANY(?)", arreglo(codigos)),
                EstadoReserva::getCodEstado);
    }

    public Optional<EstadoReserva> buscarPorNombre(String nombre) {
        return consultarUno(SELECCION + "WHERE e.nombre = ?", nombre);
    }

    @Override
    public boolean existePorId(Long codEstado) {
        return existe("SELECT EXISTS (SELECT 1 FROM estado_reserva WHERE cod_estado = ?)", codEstado);
    }

    @Override
    public EstadoReserva insertar(EstadoReserva estado) {
        estado.setCodEstado(insertarDevolviendoCodigo("""
                INSERT INTO estado_reserva (nombre)
                VALUES (?)
                RETURNING cod_estado
                """, estado.getNombre()));
        return estado;
    }

    @Override
    public EstadoReserva actualizar(EstadoReserva estado) {
        jdbc.update("""
                UPDATE estado_reserva
                SET nombre = ?
                WHERE cod_estado = ?
                """, estado.getNombre(), estado.getCodEstado());
        return estado;
    }

    @Override
    public void eliminar(Long codEstado) {
        jdbc.update("DELETE FROM estado_reserva WHERE cod_estado = ?", codEstado);
    }
}
