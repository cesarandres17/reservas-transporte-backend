package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "factura")
@Getter
@Setter
@NoArgsConstructor
public class Factura {

    // La llave es la misma de la reserva: cada reserva tiene como maximo una factura.
    @Id
    private Long codReserva;

    @NotNull
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "cod_reserva")
    private Reserva reserva;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false, unique = true)
    private String numeroFactura;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaEmision;
}
