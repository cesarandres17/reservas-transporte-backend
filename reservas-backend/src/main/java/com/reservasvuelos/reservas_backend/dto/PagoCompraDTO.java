package com.reservasvuelos.reservas_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PagoCompraDTO(
        @NotNull Long codMetodo,
        @Min(1) @Max(36) Integer cuotas,
        @Size(max = 40) String referenciaTransaccion) {
}
