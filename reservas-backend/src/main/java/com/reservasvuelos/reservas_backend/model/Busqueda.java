package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "busqueda")
@Getter
@Setter
@NoArgsConstructor
public class Busqueda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codBusqueda;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaHoraBusqueda;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaIda;

    private LocalDate fechaRegreso;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer numeroPasajeros;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer resultados;

    // Opcional: una persona puede buscar sin haber iniciado sesion.
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_cliente")
    private Cliente cliente;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_ciudad_origen", nullable = false)
    private Ciudad ciudadOrigen;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_ciudad_destino", nullable = false)
    private Ciudad ciudadDestino;
}