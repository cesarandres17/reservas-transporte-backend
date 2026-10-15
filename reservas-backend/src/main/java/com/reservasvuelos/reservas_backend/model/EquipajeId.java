package com.reservasvuelos.reservas_backend.model;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class EquipajeId implements Serializable {
    // Mismos nombres que los campos @Id de Equipaje; tiquete guarda el codTiquete.
    private Long tiquete;
    private Integer numeroPieza;
}
