package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cliente", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo_documento", "numero_documento"}))
@Getter
@Setter
@NoArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codCliente;

    @NotBlank
    @Size(max = 3)
    @Column(length = 3, nullable = false)
    private String tipoDocumento;

    @NotBlank
    @Size(max = 20)
    @Column(name= "Documento", length = 20, nullable = false)
    private String numeroDocumento;

    @NotBlank
    @Size(max = 80)
    @Column(length = 80, nullable = false)
    private String nombres;

    @NotBlank
    @Size(max = 80)
    @Column(length = 80, nullable = false)
    private String apellidos;

    @Email
    @NotBlank
    @Size(max = 120)
    @Column(length = 120, nullable = false, unique = true)
    private String email;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Past
    private LocalDate fechaNacimiento;

    @Size(max = 200)
    @Column(length = 200)
    private String direccion;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaRegistro;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_ciudad", nullable = false)
    private Ciudad ciudad;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_pais", nullable = false)
    private Pais pais;
}
