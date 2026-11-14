package com.reservasvuelos.reservas_backend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.reservasvuelos.reservas_backend.model.enums.TipoEquipaje;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EquipajeSolicitudDTO(
        @NotEmpty @Size(max = 4) List<@NotNull @Valid Pieza> piezas,
        Long codMetodo,
        @Min(1) @Max(36) Integer cuotas) {

    public record Pieza(
            @NotNull TipoEquipaje tipo,
            @NotNull @Positive @DecimalMax("999.99") @Digits(integer = 3, fraction = 2) BigDecimal pesoKg,
            @Pattern(regexp = "\\d{1,3}x\\d{1,3}x\\d{1,3}", message = "debe tener el formato largoxanchoxalto, por ejemplo 70x45x30") String dimensionesCm) {
    }
}
