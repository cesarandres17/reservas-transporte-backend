package com.reservasvuelos.reservas_backend.model;

import com.reservasvuelos.reservas_backend.model.enums.CanalCheckin;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "checkin")
@Getter
@Setter
@NoArgsConstructor
public class Checkin {
    // La llave es la misma del tiquete: cada tiquete tiene como maximo un check-in.
    @Id
    private Long codTiquete;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "cod_tiquete")
    private Tiquete tiquete;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CanalCheckin canal;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false, unique = true)
    private String codigoPaseAbordar;

    @Size(max = 3)
    @Column(length = 3)
    private String grupoAbordaje;

    // Opcional: solo cuando el check-in se hace en mostrador.
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_empleado")
    private Empleado empleado;
}