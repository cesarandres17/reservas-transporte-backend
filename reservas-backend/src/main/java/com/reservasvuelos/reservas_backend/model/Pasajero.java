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
@Table(name = "pasajero", uniqueConstraints = @UniqueConstraint(columnNames = {"cod_tipo_documento", "numero_documento"}))
@Getter
@Setter
@NoArgsConstructor
public class Pasajero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codPasajero;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false)
    private String numeroDocumento;

    @NotBlank
    @Size(max = 40)
    @Column(length = 40, nullable = false)
    private String primerNombre;

    @Size(max = 40)
    @Column(length = 40)
    private String segundoNombre;

    @NotBlank
    @Size(max = 40)
    @Column(length = 40, nullable = false)
    private String primerApellido;

    @Size(max = 40)
    @Column(length = 40)
    private String segundoApellido;

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