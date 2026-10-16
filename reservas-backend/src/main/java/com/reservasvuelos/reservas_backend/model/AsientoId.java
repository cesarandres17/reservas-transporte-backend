package com.reservasvuelos.reservas_backend.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Getter 
@Setter 
@NoArgsConstructor
@EqualsAndHashCode  
public class AsientoId implements Serializable {
    private Long vehiculo;
    private String numeroAsiento;
    
}
