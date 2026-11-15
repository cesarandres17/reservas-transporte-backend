package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Pais;

@Repository
public class PaisRepositorio extends RepositorioSql<Pais, Long> {

    private static final String SELECCION = """
            SELECT p.cod_pais, p.nombre, p.codigo_iso
            FROM pais p
            """;

    public PaisRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected Pais mapear(ResultSet fila) throws SQLException {
        Pais pais = new Pais();
        pais.setCodPais(fila.getLong("cod_pais"));
        pais.setNombre(fila.getString("nombre"));
        pais.setCodigoIso(fila.getString("codigo_iso"));
        return pais;
    }

    @Override
    public List<Pais> listar() {
        return consultar(SELECCION + "ORDER BY p.cod_pais");
    }

    @Override
    public Optional<Pais> buscarPorId(Long codPais) {
        return consultarUno(SELECCION + "WHERE p.cod_pais = ?", codPais);
    }

    public Map<Long, Pais> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE p.cod_pais = ANY(?)", arreglo(codigos)), Pais::getCodPais);
    }

    public Optional<Pais> buscarPorCodigoIso(String codigoIso) {
        return consultarUno(SELECCION + "WHERE UPPER(p.codigo_iso) = UPPER(?)", codigoIso);
    }

    @Override
    public boolean existePorId(Long codPais) {
        return existe("SELECT EXISTS (SELECT 1 FROM pais WHERE cod_pais = ?)", codPais);
    }

    @Override
    public Pais insertar(Pais pais) {
        pais.setCodPais(insertarDevolviendoCodigo("""
                INSERT INTO pais (nombre, codigo_iso)
                VALUES (?, ?)
                RETURNING cod_pais
                """, pais.getNombre(), pais.getCodigoIso()));
        return pais;
    }

    @Override
    public Pais actualizar(Pais pais) {
        jdbc.update("""
                UPDATE pais
                SET nombre = ?, codigo_iso = ?
                WHERE cod_pais = ?
                """, pais.getNombre(), pais.getCodigoIso(), pais.getCodPais());
        return pais;
    }

    @Override
    public void eliminar(Long codPais) {
        jdbc.update("DELETE FROM pais WHERE cod_pais = ?", codPais);
    }
}
