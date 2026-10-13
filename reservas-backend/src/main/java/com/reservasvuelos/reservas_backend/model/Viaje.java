package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import com.reservasvuelos.reservas_backend.model.enums.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "viaje", uniqueConstraints = @UniqueConstraint(columnNames = {"codigo_viaje", "fecha_hora_salida"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Viaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codViaje;

    @NotBlank
    @Size(max = 12)
    @Column(length = 12, nullable = false)
    private String codigoViaje;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaHoraSalida;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaHoraLlegada;

    @Size(max = 10)
    @Column(length = 10)
    private String puertaAnden;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoViaje estado;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_ruta", nullable = false)
    private Ruta ruta;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_vehiculo", nullable = false)
    private Vehiculo vehiculo;
}
