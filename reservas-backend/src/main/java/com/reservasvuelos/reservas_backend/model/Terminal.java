package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "terminal")
@Getter
@Setter
@NoArgsConstructor
public class Terminal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codTerminal;

    @NotBlank
    @Size(max = 10)
    @Column(length = 10, nullable = false, unique = true)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    @Column(length = 120, nullable = false)
    private String nombre;

    @Size(max = 200)
    @Column(length = 200)
    private String direccion;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_ciudad", nullable = false)
    private Ciudad ciudad;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_tipo", nullable = false)
    private TipoTransporte tipoTransporte;
}
