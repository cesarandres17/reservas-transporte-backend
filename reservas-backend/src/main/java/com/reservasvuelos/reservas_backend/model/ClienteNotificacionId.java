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
public class ClienteNotificacionId implements Serializable {

    private Long cliente;
    private Long notificacion;
}
