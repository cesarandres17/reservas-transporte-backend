package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "tipo_transporte")
@Getter
@Setter
@NoArgsConstructor
public class TipoTransporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codTipo;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false, unique = true)
    private String nombre;

    @Size(max = 200)
    @Column(length = 200)
    private String descripcion;
}
