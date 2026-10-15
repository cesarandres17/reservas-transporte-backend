package com.reservasvuelos.reservas_backend.model;

import com.reservasvuelos.reservas_backend.model.enums.EstadoEquipaje;
import com.reservasvuelos.reservas_backend.model.enums.TipoEquipaje;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "equipaje")
@IdClass(EquipajeId.class)
@Getter
@Setter
@NoArgsConstructor
public class Equipaje {
    @Id
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_tiquete")
    private Tiquete tiquete;

    // 1, 2, 3... dentro del mismo tiquete.
    @Id
    @Positive
    private Integer numeroPieza;

    @NotBlank
    @Size(max = 12)
    @Column(length = 12, nullable = false, unique = true)
    private String codigoEtiqueta;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoEquipaje tipo;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Size(max = 20)
    @Column(length = 20)
    private String dimensionesCm;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costoAdicional;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoEquipaje estado;
}