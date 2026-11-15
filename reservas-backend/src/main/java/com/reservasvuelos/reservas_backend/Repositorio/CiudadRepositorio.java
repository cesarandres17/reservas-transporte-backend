package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.model.Departamento;

@Repository
public class CiudadRepositorio extends RepositorioSql<Ciudad, Long> {

    private static final String SELECCION = """
            SELECT c.cod_ciudad, c.nombre, c.zona_horaria, c.cod_departamento
            FROM ciudad c
            """;

    private final DepartamentoRepositorio departamentoRepositorio;

    public CiudadRepositorio(JdbcTemplate jdbc, DepartamentoRepositorio departamentoRepositorio) {
        super(jdbc);
        this.departamentoRepositorio = departamentoRepositorio;
    }

    @Override
    protected Ciudad mapear(ResultSet fila) throws SQLException {
        Ciudad ciudad = new Ciudad();
        ciudad.setCodCiudad(fila.getLong("cod_ciudad"));
        ciudad.setNombre(fila.getString("nombre"));
        ciudad.setZonaHoraria(fila.getString("zona_horaria"));
        ciudad.setDepartamento(referencia(Departamento::new, Departamento::setCodDepartamento,
                largo(fila, "cod_departamento")));
        return ciudad;
    }

    @Override
    protected List<Ciudad> completar(List<Ciudad> ciudades) {
        cargar(ciudades, Ciudad::getDepartamento, Departamento::getCodDepartamento, departamentoRepositorio::porCodigos,
                Ciudad::setDepartamento);
        return ciudades;
    }

    @Override
    public List<Ciudad> listar() {
        return consultar(SELECCION + "ORDER BY c.cod_ciudad");
    }

    @Override
    public Optional<Ciudad> buscarPorId(Long codCiudad) {
        return consultarUno(SELECCION + "WHERE c.cod_ciudad = ?", codCiudad);
    }

    public Map<Long, Ciudad> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE c.cod_ciudad = ANY(?)", arreglo(codigos)), Ciudad::getCodCiudad);
    }

    public List<Ciudad> deDepartamento(Long codDepartamento) {
        return consultar(SELECCION + "WHERE c.cod_departamento = ? ORDER BY c.cod_ciudad", codDepartamento);
    }

    @Override
    public boolean existePorId(Long codCiudad) {
        return existe("SELECT EXISTS (SELECT 1 FROM ciudad WHERE cod_ciudad = ?)", codCiudad);
    }

    @Override
    public Ciudad insertar(Ciudad ciudad) {
        ciudad.setCodCiudad(insertarDevolviendoCodigo("""
                INSERT INTO ciudad (nombre, zona_horaria, cod_departamento)
                VALUES (?, ?, ?)
                RETURNING cod_ciudad
                """, ciudad.getNombre(), ciudad.getZonaHoraria(),
                codigo(ciudad.getDepartamento(), Departamento::getCodDepartamento)));
        return ciudad;
    }

    @Override
    public Ciudad actualizar(Ciudad ciudad) {
        jdbc.update("""
                UPDATE ciudad
                SET nombre = ?, zona_horaria = ?, cod_departamento = ?
                WHERE cod_ciudad = ?
                """, ciudad.getNombre(), ciudad.getZonaHoraria(),
                codigo(ciudad.getDepartamento(), Departamento::getCodDepartamento), ciudad.getCodCiudad());
        return ciudad;
    }

    @Override
    public void eliminar(Long codCiudad) {
        jdbc.update("DELETE FROM ciudad WHERE cod_ciudad = ?", codCiudad);
    }
}
