package com.reservasvuelos.reservas_backend.dto;

public record ReporteInfoDTO(
        int numero,
        String titulo,
        String tipo,
        String descripcion,
        String grafico) {
}
