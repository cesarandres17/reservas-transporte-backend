package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.reservasvuelos.reservas_backend.model.enums.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "empleado")
@Getter
@Setter
@NoArgsConstructor
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codEmpleado;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false, unique = true)
    private String numeroDocumento;

    @NotBlank
    @Size(max = 80)
    @Column(length = 80, nullable = false)
    private String nombres;

    @NotBlank
    @Size(max = 80)
    @Column(length = 80, nullable = false)
    private String apellidos;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CargoEmpleado cargo;

    @Email
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salario;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaIngreso;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_empresa", nullable = false)
    private Empresa empresa;
}
