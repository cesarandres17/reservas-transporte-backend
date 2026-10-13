package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.reservasvuelos.reservas_backend.model.enums.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "vehiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codVehiculo;

    @NotBlank
    @Size(max = 15)
    @Column(length = 15, nullable = false, unique = true)
    private String matricula;

    @NotBlank
    @Size(max = 60)
    @Column(length = 60, nullable = false)
    private String modelo;

    @Size(max = 60)
    @Column(length = 60)
    private String fabricante;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer capacidad;

    private Integer anioFabricacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoVehiculo estado;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_empresa", nullable = false)
    private Empresa empresa;
}
