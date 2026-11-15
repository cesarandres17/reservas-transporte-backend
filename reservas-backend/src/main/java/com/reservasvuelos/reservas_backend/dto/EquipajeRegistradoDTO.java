package com.reservasvuelos.reservas_backend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.reservasvuelos.reservas_backend.dto.CheckinReservaDTO.EquipajeRegistrado;

public record EquipajeRegistradoDTO(
        Long codTiquete,
        String pasajero,
        BigDecimal equipajeIncluidoKg,
        List<EquipajeRegistrado> registradas,
        List<EquipajeRegistrado> equipajes,
        BigDecimal costoTotal,
        Long codPago,
        String referenciaPago) {
}
