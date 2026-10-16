package com.reservasvuelos.reservas_backend.model;

import com.reservasvuelos.reservas_backend.model.enums.EstadoTiquete;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tiquete", uniqueConstraints = {
        // Un pasajero no puede tener dos tiquetes en el mismo viaje.
        @UniqueConstraint(columnNames = {"cod_viaje", "cod_pasajero"}),
        // Un asiento no se puede vender dos veces en el mismo viaje.
        @UniqueConstraint(columnNames = {"cod_viaje", "cod_vehiculo", "numero_asiento"})
})
@Getter
@Setter
@NoArgsConstructor
public class Tiquete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codTiquete;

    @NotBlank
    @Size(max = 14)
    @Column(length = 14, nullable = false, unique = true)
    private String numeroTiquete;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioPagado;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoTiquete estado;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_reserva", nullable = false)
    private Reserva reserva;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_pasajero", nullable = false)
    private Pasajero pasajero;

    // Tarifa tiene llave compuesta (viaje + clase), por eso la FK tiene dos columnas.
    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "cod_viaje", referencedColumnName = "cod_viaje", nullable = false),
            @JoinColumn(name = "cod_clase", referencedColumnName = "cod_clase", nullable = false)
    })
    private Tarifa tarifa;

    // Asiento tiene llave compuesta (vehiculo + numero). Es opcional: se puede asignar despues.
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "cod_vehiculo", referencedColumnName = "cod_vehiculo"),
            @JoinColumn(name = "numero_asiento", referencedColumnName = "numeroAsiento")
    })
    private Asiento asiento;
}