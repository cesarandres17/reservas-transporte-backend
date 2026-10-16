package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.reservasvuelos.reservas_backend.model.enums.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codEmpresa;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false, unique = true)
    private String nit;

    @NotBlank
    @Size(max = 120)
    @Column(length = 120, nullable = false)
    private String nombre;

    @Size(max = 4)
    @Column(length = 4)
    private String codigoOperador;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Email
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoEmpresa estado;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_tipo", nullable = false)
    private TipoTransporte tipoTransporte;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_pais", nullable = false)
    private Pais pais;
}
