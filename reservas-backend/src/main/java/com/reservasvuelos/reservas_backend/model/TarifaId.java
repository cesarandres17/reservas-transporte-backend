package com.reservasvuelos.reservas_backend.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Getter 
@Setter 
@NoArgsConstructor
@EqualsAndHashCode
@ToString

public class TarifaId  implements Serializable {
    
    private Long viaje;
    private Long claseServicio;
    
}
