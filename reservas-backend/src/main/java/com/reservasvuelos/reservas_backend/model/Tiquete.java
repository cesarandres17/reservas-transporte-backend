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
@Table(name = "tiquete", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cod_viaje", "cod_pasajero"}),
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
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_estado_tiquete", nullable = false)
    private EstadoTiquete estado;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_tipo_trayecto", nullable = false)
    private TipoTrayecto tipoTrayecto;

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

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "cod_viaje", referencedColumnName = "cod_viaje", nullable = false),
            @JoinColumn(name = "cod_clase", referencedColumnName = "cod_clase", nullable = false)
    })
    private Tarifa tarifa;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "cod_vehiculo", referencedColumnName = "cod_vehiculo"),
            @JoinColumn(name = "numero_asiento", referencedColumnName = "numeroAsiento")
    })
    private Asiento asiento;
}