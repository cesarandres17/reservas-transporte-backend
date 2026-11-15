package com.reservasvuelos.reservas_backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record RegistroClienteDTO(
        @NotBlank @Size(max = 5) String siglaTipoDocumento,
        @NotBlank @Size(max = 20) String numeroDocumento,
        @NotBlank @Size(max = 40) String primerNombre,
        @Size(max = 40) String segundoNombre,
        @NotBlank @Size(max = 40) String primerApellido,
        @Size(max = 40) String segundoApellido,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 20) String telefono,
        @Past LocalDate fechaNacimiento,
        @Size(max = 200) String direccion,
        Long codCiudad,
        Boolean recibeNotificaciones) {
}
