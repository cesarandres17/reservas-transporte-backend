package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "pais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pais {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codPais;

    @NotBlank
    @Size(max = 80)
    @Column(length = 80, nullable = false)
    private String nombre;

    @NotBlank
    @Size(max = 3)
    @Column(length = 3, nullable = false, unique = true)
    private String codigoIso;
}
