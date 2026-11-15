package com.reservasvuelos.reservas_backend.dto;

import java.util.List;

import com.reservasvuelos.reservas_backend.model.enums.CanalCheckin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckinSolicitudDTO(
        CanalCheckin canal,
        @Size(max = 9) List<@NotNull @Valid PasajeroCheckin> pasajeros) {

    public record PasajeroCheckin(
            @NotNull Long codTiquete,
            @Size(max = 5) String numeroAsiento) {
    }
}
