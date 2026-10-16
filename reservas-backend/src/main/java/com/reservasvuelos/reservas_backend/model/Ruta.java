package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ruta")
@Getter
@Setter
@NoArgsConstructor
public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codRuta;

    @NotBlank
    @Size(max = 15)
    @Column(length = 15, nullable = false, unique = true)
    private String codigo;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 7, scale = 1)
    private BigDecimal distanciaKm;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer duracionMin;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_terminal_origen", nullable = false)
    private Terminal terminalOrigen;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_terminal_destino", nullable = false)
    private Terminal terminalDestino;
}
