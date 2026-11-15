package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "promocion")
@Getter
@Setter
@NoArgsConstructor
public class Promocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codPromocion;

    @NotBlank
    @Size(max = 15)
    @Column(length = 15, nullable = false, unique = true)
    private String codigo;

    @Size(max = 200)
    @Column(length = 200)
    private String descripcion;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaInicio;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaFin;
}