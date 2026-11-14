package com.reservasvuelos.reservas_backend.dto;

import java.time.LocalDate;
import java.util.List;

import com.reservasvuelos.reservas_backend.model.enums.CanalReserva;
import com.reservasvuelos.reservas_backend.model.enums.TipoPasajero;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompraDTO(
        @NotNull Long codViaje,
        @NotNull Long codClase,
        Long codCliente,
        @Size(max = 15) String codigoPromocion,
        CanalReserva canal,
        @NotEmpty @Size(max = 9) List<@NotNull @Valid PasajeroCompra> pasajeros,
        @Valid PagoCompraDTO pago) {

    public record PasajeroCompra(
            Long codPasajero,
            @Size(max = 5) String siglaTipoDocumento,
            @Size(max = 20) String numeroDocumento,
            @Size(max = 40) String primerNombre,
            @Size(max = 40) String segundoNombre,
            @Size(max = 40) String primerApellido,
            @Size(max = 40) String segundoApellido,
            @Past LocalDate fechaNacimiento,
            @Pattern(regexp = "[MF]") String genero,
            TipoPasajero tipoPasajero,
            @Size(min = 3, max = 3) String codigoPais,
            @Size(max = 5) String numeroAsiento) {
    }
}
