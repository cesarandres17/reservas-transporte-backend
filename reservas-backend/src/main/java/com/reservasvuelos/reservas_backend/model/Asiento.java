package com.reservasvuelos.reservas_backend.model;

import com.reservasvuelos.reservas_backend.model.enums.UbicacionAsiento;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "asiento")
@IdClass(AsientoId.class)        
@Getter
@Setter
@NoArgsConstructor
public class Asiento {

    @Id                             
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_vehiculo")
    private Vehiculo vehiculo;      

    @Id                            
    @Column(length = 5)
    private String numeroAsiento;  
    @NotNull
    @Column(nullable = false)
    private Integer fila;

    @Size(max = 1)
    @Column(length = 1)
    private String letra;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UbicacionAsiento ubicacion;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_clase", nullable = false)
    private ClaseServicio claseServicio;
}