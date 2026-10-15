package com.reservasvuelos.reservas_backend.model;

import com.reservasvuelos.reservas_backend.model.enums.TipoMetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "metodo_pago")
@Getter
@Setter
@NoArgsConstructor
public class MetodoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codMetodo;

    @NotBlank
    @Size(max = 40)
    @Column(length = 40, nullable = false, unique = true)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMetodoPago tipo;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal comisionPct;
}
