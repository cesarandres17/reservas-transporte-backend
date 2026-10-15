package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codRol;

    @NotBlank
    @Size(max = 70)
    @Column(length = 70, nullable = false, unique = true)
    private String nombre;

    @Size(max = 200)
    @Column(length = 200)
    private String descripcion;
}
