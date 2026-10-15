package com.reservasvuelos.reservas_backend.model;

import com.reservasvuelos.reservas_backend.model.enums.TipoPasajero;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "pasajero", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo_documento", "numero_documento"}))
@Getter
@Setter
@NoArgsConstructor
public class Pasajero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codPasajero;

    @NotBlank
    @Size(max = 3)
    @Column(length = 3, nullable = false)
    private String tipoDocumento;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false)
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
    @Past
    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Size(max = 1)
    @Column(length = 1)
    private String genero;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoPasajero tipoPasajero;

    // Nacionalidad del pasajero.
    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_pais", nullable = false)
    private Pais pais;
}
