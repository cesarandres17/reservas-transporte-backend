package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "clase_servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaseServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codClase;

    @NotBlank
    @Size(max = 30)
    @Column(length = 30, nullable = false, unique = true)
    private String nombre;

    @Size(max = 200)
    @Column(length = 200)
    private String descripcion;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal equipajeIncluidoKg;

    @NotNull
    @Column(nullable = false)
    private Boolean permiteCambios = false;

    @NotNull
    @Column(nullable = false)
    private Boolean permiteReembolso = false;
}
