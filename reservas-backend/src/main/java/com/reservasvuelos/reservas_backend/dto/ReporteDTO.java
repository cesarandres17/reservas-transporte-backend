package com.reservasvuelos.reservas_backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ReporteDTO(
        int numero,
        String titulo,
        String tipo,
        String descripcion,
        String grafico,
        String columnaEtiqueta,
        String columnaValor,
        List<String> columnasTotal,
        String sql,
        LocalDateTime generado,
        List<String> columnas,
        List<Map<String, Object>> filas) {
}
