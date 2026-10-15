package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tarifa")
@IdClass(TarifaId.class)
@Getter
@Setter
@NoArgsConstructor
public class Tarifa {

    @Id
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_viaje")
    private Viaje viaje;

    @Id
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_clase")
    private ClaseServicio claseServicio;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal impuestos;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer cuposDisponibles;
}