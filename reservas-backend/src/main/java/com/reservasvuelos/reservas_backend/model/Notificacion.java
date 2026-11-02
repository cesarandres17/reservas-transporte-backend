package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codNotificacion;

    @NotBlank
    @Size(max = 80)
    @Column(length = 80, nullable = false)
    private String titulo;

    @NotBlank
    @Size(max = 500)
    @Column(length = 500, nullable = false)
    private String mensaje;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_tipo_notificacion", nullable = false)
    private TipoNotificacion tipoNotificacion;

    // Opcional: solo cuando la notificacion anuncia una promocion.
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_promocion")
    private Promocion promocion;

    // Opcional: solo cuando es un recordatorio o un cambio de un viaje.
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_viaje")
    private Viaje viaje;
}
