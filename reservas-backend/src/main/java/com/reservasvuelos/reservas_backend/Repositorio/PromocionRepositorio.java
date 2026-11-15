package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Promocion;

@Repository
public class PromocionRepositorio extends RepositorioSql<Promocion, Long> {

    private static final String SELECCION = """
            SELECT p.cod_promocion, p.codigo, p.descripcion, p.porcentaje_descuento, p.fecha_inicio, p.fecha_fin
            FROM promocion p
            """;

    public PromocionRepositorio(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected Promocion mapear(ResultSet fila) throws SQLException {
        Promocion promocion = new Promocion();
        promocion.setCodPromocion(fila.getLong("cod_promocion"));
        promocion.setCodigo(fila.getString("codigo"));
        promocion.setDescripcion(fila.getString("descripcion"));
        promocion.setPorcentajeDescuento(fila.getBigDecimal("porcentaje_descuento"));
        promocion.setFechaInicio(fecha(fila, "fecha_inicio"));
        promocion.setFechaFin(fecha(fila, "fecha_fin"));
        return promocion;
    }

    @Override
    public List<Promocion> listar() {
        return consultar(SELECCION + "ORDER BY p.cod_promocion");
    }

    @Override
    public Optional<Promocion> buscarPorId(Long codPromocion) {
        return consultarUno(SELECCION + "WHERE p.cod_promocion = ?", codPromocion);
    }

    public Map<Long, Promocion> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE p.cod_promocion = ANY(?)", arreglo(codigos)),
                Promocion::getCodPromocion);
    }

    public Optional<Promocion> buscarPorCodigo(String codigo) {
        return consultarUno(SELECCION + "WHERE UPPER(p.codigo) = UPPER(?)", codigo);
    }

    @Override
    public boolean existePorId(Long codPromocion) {
        return existe("SELECT EXISTS (SELECT 1 FROM promocion WHERE cod_promocion = ?)", codPromocion);
    }

    @Override
    public Promocion insertar(Promocion promocion) {
        promocion.setCodPromocion(insertarDevolviendoCodigo("""
                INSERT INTO promocion (codigo, descripcion, porcentaje_descuento, fecha_inicio, fecha_fin)
                VALUES (?, ?, ?, ?, ?)
                RETURNING cod_promocion
                """, promocion.getCodigo(), promocion.getDescripcion(), promocion.getPorcentajeDescuento(),
                promocion.getFechaInicio(), promocion.getFechaFin()));
        return promocion;
    }

    @Override
    public Promocion actualizar(Promocion promocion) {
        jdbc.update("""
                UPDATE promocion
                SET codigo = ?, descripcion = ?, porcentaje_descuento = ?, fecha_inicio = ?, fecha_fin = ?
                WHERE cod_promocion = ?
                """, promocion.getCodigo(), promocion.getDescripcion(), promocion.getPorcentajeDescuento(),
                promocion.getFechaInicio(), promocion.getFechaFin(), promocion.getCodPromocion());
        return promocion;
    }

    @Override
    public void eliminar(Long codPromocion) {
        jdbc.update("DELETE FROM promocion WHERE cod_promocion = ?", codPromocion);
    }
}
