package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codPago;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaPago;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    // Opcional: solo aplica a tarjeta de credito.
    @Positive
    private Integer cuotas;

    // Opcional: un pago en efectivo no tiene referencia.
    @Size(max = 40)
    @Column(length = 40, unique = true)
    private String referenciaTransaccion;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_estado_pago", nullable = false)
    private EstadoPago estado;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_reserva", nullable = false)
    private Reserva reserva;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_metodo", nullable = false)
    private MetodoPago metodoPago;
}