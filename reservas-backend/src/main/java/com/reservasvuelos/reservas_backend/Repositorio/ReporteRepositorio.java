package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

@Repository
public class ReporteRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public ReporteRepositorio(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Resultado consultar(String sql) {
        ResultSetExtractor<Resultado> extractor = rs -> {
            ResultSetMetaData meta = rs.getMetaData();
            List<String> columnas = new ArrayList<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                columnas.add(meta.getColumnLabel(i));
            }
            List<Map<String, Object>> filas = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                for (int i = 1; i <= columnas.size(); i++) {
                    fila.put(columnas.get(i - 1), convertir(rs.getObject(i)));
                }
                filas.add(fila);
            }
            return new Resultado(columnas, filas);
        };
        return jdbcTemplate.query(sql, extractor);
    }

    private Object convertir(Object valor) {
        if (valor instanceof Timestamp fechaHora) {
            return fechaHora.toLocalDateTime();
        }
        if (valor instanceof Date fecha) {
            return fecha.toLocalDate();
        }
        return valor;
    }

    public record Resultado(List<String> columnas, List<Map<String, Object>> filas) {
    }
}
