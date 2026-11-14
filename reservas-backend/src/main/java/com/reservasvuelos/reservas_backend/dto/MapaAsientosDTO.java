package com.reservasvuelos.reservas_backend.dto;

import java.util.List;

public record MapaAsientosDTO(
        Long codViaje,
        String codigoViaje,
        String vehiculo,
        int totalAsientos,
        int asientosLibres,
        List<AsientoMapa> asientos) {

    public record AsientoMapa(
            String numeroAsiento,
            Integer fila,
            String letra,
            String ubicacion,
            Long codClase,
            String clase,
            boolean ocupado) {
    }
}
