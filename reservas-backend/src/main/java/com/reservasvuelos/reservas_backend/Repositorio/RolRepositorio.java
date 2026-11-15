package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Rol;

@Repository
public class RolRepositorio extends RepositorioSql<Rol, Long> {

    private static final String SELECCION = """
            SELECT r.cod_rol, r.nombre, r.descripcion
            FROM rol r
            """;

    public RolRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected Rol mapear(ResultSet fila) throws SQLException {
        Rol rol = new Rol();
        rol.setCodRol(fila.getLong("cod_rol"));
        rol.setNombre(fila.getString("nombre"));
        rol.setDescripcion(fila.getString("descripcion"));
        return rol;
    }

    @Override
    public List<Rol> listar() {
        return consultar(SELECCION + "ORDER BY r.cod_rol");
    }

    @Override
    public Optional<Rol> buscarPorId(Long codRol) {
        return consultarUno(SELECCION + "WHERE r.cod_rol = ?", codRol);
    }

    public Map<Long, Rol> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE r.cod_rol = ANY(?)", arreglo(codigos)), Rol::getCodRol);
    }

    public Optional<Rol> buscarPorNombre(String nombre) {
        return consultarUno(SELECCION + "WHERE r.nombre = ?", nombre);
    }

    @Override
    public boolean existePorId(Long codRol) {
        return existe("SELECT EXISTS (SELECT 1 FROM rol WHERE cod_rol = ?)", codRol);
    }

    @Override
    public Rol insertar(Rol rol) {
        rol.setCodRol(insertarDevolviendoCodigo("""
                INSERT INTO rol (nombre, descripcion)
                VALUES (?, ?)
                RETURNING cod_rol
                """, rol.getNombre(), rol.getDescripcion()));
        return rol;
    }

    @Override
    public Rol actualizar(Rol rol) {
        jdbc.update("""
                UPDATE rol
                SET nombre = ?, descripcion = ?
                WHERE cod_rol = ?
                """, rol.getNombre(), rol.getDescripcion(), rol.getCodRol());
        return rol;
    }

    @Override
    public void eliminar(Long codRol) {
        jdbc.update("DELETE FROM rol WHERE cod_rol = ?", codRol);
    }
}
