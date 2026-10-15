package com.reservasvuelos.reservas_backend.model;

import com.reservasvuelos.reservas_backend.model.enums.CanalReserva;
import com.reservasvuelos.reservas_backend.model.enums.EstadoReserva;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codReserva;

    // Codigo de 6 caracteres que ve el cliente, por ejemplo "X7K2PQ".
    @NotBlank
    @Size(max = 6)
    @Column(length = 6, nullable = false, unique = true)
    private String codigoReserva;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaReserva;

    private LocalDateTime fechaLimitePago;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CanalReserva canal;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoReserva estado;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal descuento;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_cliente", nullable = false)
    private Cliente cliente;

    // Opcional: no todas las reservas usan promocion.
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_promocion")
    private Promocion promocion;
}
