package com.reservasvuelos.reservas_backend.model;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class EquipajeId implements Serializable {
    
    private Long tiquete;
    private Integer numeroPieza;
}