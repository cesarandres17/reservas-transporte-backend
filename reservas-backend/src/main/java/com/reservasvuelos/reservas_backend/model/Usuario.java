package com.reservasvuelos.reservas_backend.model;

import com.reservasvuelos.reservas_backend.model.enums.EstadoUsuario;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "usuario", uniqueConstraints = @UniqueConstraint(columnNames = {"cod_tipo_documento", "numero_documento"}))
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codUsuario;

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

    @Email
    @NotBlank
    @Size(max = 120)
    @Column(length = 120, nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String password;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Past
    private LocalDate fechaNacimiento;

    @Size(max = 200)
    @Column(length = 200)
    private String direccion;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_ciudad")
    private Ciudad ciudad;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaRegistro;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoUsuario estado;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_rol", nullable = false)
    private Rol rol;
}
