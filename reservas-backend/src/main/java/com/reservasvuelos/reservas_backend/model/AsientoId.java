package com.reservasvuelos.reservas_backend.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Getter 
@Setter 
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AsientoId implements Serializable {
    private Long vehiculo;
    private String numeroAsiento;
    
}
