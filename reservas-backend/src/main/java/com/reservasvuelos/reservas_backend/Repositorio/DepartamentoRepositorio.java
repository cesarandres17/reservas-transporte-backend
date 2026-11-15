package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Departamento;
import com.reservasvuelos.reservas_backend.model.Pais;

@Repository
public class DepartamentoRepositorio extends RepositorioSql<Departamento, Long> {

    private static final String SELECCION = """
            SELECT d.cod_departamento, d.nombre, d.cod_pais
            FROM departamento d
            """;

    private final PaisRepositorio paisRepositorio;

    public DepartamentoRepositorio(JdbcTemplate jdbc, PaisRepositorio paisRepositorio) {
        super(jdbc);
        this.paisRepositorio = paisRepositorio;
    }

    @Override
    protected Departamento mapear(ResultSet fila) throws SQLException {
        Departamento departamento = new Departamento();
        departamento.setCodDepartamento(fila.getLong("cod_departamento"));
        departamento.setNombre(fila.getString("nombre"));
        departamento.setPais(referencia(Pais::new, Pais::setCodPais, largo(fila, "cod_pais")));
        return departamento;
    }

    @Override
    protected List<Departamento> completar(List<Departamento> departamentos) {
        cargar(departamentos, Departamento::getPais, Pais::getCodPais, paisRepositorio::porCodigos,
                Departamento::setPais);
        return departamentos;
    }

    @Override
    public List<Departamento> listar() {
        return consultar(SELECCION + "ORDER BY d.cod_departamento");
    }

    @Override
    public Optional<Departamento> buscarPorId(Long codDepartamento) {
        return consultarUno(SELECCION + "WHERE d.cod_departamento = ?", codDepartamento);
    }

    public Map<Long, Departamento> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE d.cod_departamento = ANY(?)", arreglo(codigos)),
                Departamento::getCodDepartamento);
    }

    public List<Departamento> dePais(Long codPais) {
        return consultar(SELECCION + "WHERE d.cod_pais = ? ORDER BY d.cod_departamento", codPais);
    }

    @Override
    public boolean existePorId(Long codDepartamento) {
        return existe("SELECT EXISTS (SELECT 1 FROM departamento WHERE cod_departamento = ?)", codDepartamento);
    }

    @Override
    public Departamento insertar(Departamento departamento) {
        departamento.setCodDepartamento(insertarDevolviendoCodigo("""
                INSERT INTO departamento (nombre, cod_pais)
                VALUES (?, ?)
                RETURNING cod_departamento
                """, departamento.getNombre(), codigo(departamento.getPais(), Pais::getCodPais)));
        return departamento;
    }

    @Override
    public Departamento actualizar(Departamento departamento) {
        jdbc.update("""
                UPDATE departamento
                SET nombre = ?, cod_pais = ?
                WHERE cod_departamento = ?
                """, departamento.getNombre(), codigo(departamento.getPais(), Pais::getCodPais),
                departamento.getCodDepartamento());
        return departamento;
    }

    @Override
    public void eliminar(Long codDepartamento) {
        jdbc.update("DELETE FROM departamento WHERE cod_departamento = ?", codDepartamento);
    }
}
