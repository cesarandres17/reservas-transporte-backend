package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;

public record DefinicionReporte(
        int numero,
        String titulo,
        String tipo,
        String descripcion,
        String sql,
        Grafico grafico,
        String columnaEtiqueta,
        String columnaValor,
        List<String> columnasTotal) {

    public enum Grafico {
        BARRAS, BARRAS_HORIZONTALES, TORTA
    }
}
