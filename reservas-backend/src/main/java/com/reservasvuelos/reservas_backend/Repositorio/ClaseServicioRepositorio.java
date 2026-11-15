package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.ClaseServicio;

@Repository
public class ClaseServicioRepositorio extends RepositorioSql<ClaseServicio, Long> {

    private static final String SELECCION = """
            SELECT c.cod_clase, c.nombre, c.descripcion, c.equipaje_incluido_kg, c.permite_cambios, c.permite_reembolso
            FROM clase_servicio c
            """;

    public ClaseServicioRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected ClaseServicio mapear(ResultSet fila) throws SQLException {
        ClaseServicio clase = new ClaseServicio();
        clase.setCodClase(fila.getLong("cod_clase"));
        clase.setNombre(fila.getString("nombre"));
        clase.setDescripcion(fila.getString("descripcion"));
        clase.setEquipajeIncluidoKg(fila.getBigDecimal("equipaje_incluido_kg"));
        clase.setPermiteCambios(logico(fila, "permite_cambios"));
        clase.setPermiteReembolso(logico(fila, "permite_reembolso"));
        return clase;
    }

    @Override
    public List<ClaseServicio> listar() {
        return consultar(SELECCION + "ORDER BY c.cod_clase");
    }

    @Override
    public Optional<ClaseServicio> buscarPorId(Long codClase) {
        return consultarUno(SELECCION + "WHERE c.cod_clase = ?", codClase);
    }

    public Map<Long, ClaseServicio> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE c.cod_clase = ANY(?)", arreglo(codigos)),
                ClaseServicio::getCodClase);
    }

    @Override
    public boolean existePorId(Long codClase) {
        return existe("SELECT EXISTS (SELECT 1 FROM clase_servicio WHERE cod_clase = ?)", codClase);
    }

    @Override
    public ClaseServicio insertar(ClaseServicio clase) {
        clase.setCodClase(insertarDevolviendoCodigo("""
                INSERT INTO clase_servicio (nombre, descripcion, equipaje_incluido_kg, permite_cambios,
                        permite_reembolso)
                VALUES (?, ?, ?, ?, ?)
                RETURNING cod_clase
                """, clase.getNombre(), clase.getDescripcion(), clase.getEquipajeIncluidoKg(),
                clase.getPermiteCambios(), clase.getPermiteReembolso()));
        return clase;
    }

    @Override
    public ClaseServicio actualizar(ClaseServicio clase) {
        jdbc.update("""
                UPDATE clase_servicio
                SET nombre = ?, descripcion = ?, equipaje_incluido_kg = ?, permite_cambios = ?, permite_reembolso = ?
                WHERE cod_clase = ?
                """, clase.getNombre(), clase.getDescripcion(), clase.getEquipajeIncluidoKg(),
                clase.getPermiteCambios(), clase.getPermiteReembolso(), clase.getCodClase());
        return clase;
    }

    @Override
    public void eliminar(Long codClase) {
        jdbc.update("DELETE FROM clase_servicio WHERE cod_clase = ?", codClase);
    }
}
