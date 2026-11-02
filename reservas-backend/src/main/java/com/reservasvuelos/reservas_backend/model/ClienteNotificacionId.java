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
public class ClienteNotificacionId implements Serializable {

    private Long cliente;
    private Long notificacion;
}
