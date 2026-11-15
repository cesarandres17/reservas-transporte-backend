package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_trayecto")
@Getter
@Setter
@NoArgsConstructor
public class TipoTrayecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codTipo;

    @NotBlank
    @Size(max = 30)
    @Column(length = 30, nullable = false, unique = true)
    private String nombre;
}
