package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// A que clientes les llega cada notificacion, y en que estado esta para cada uno.
@Entity
@Table(name = "cliente_notificacion")
@IdClass(ClienteNotificacionId.class)
@Getter
@Setter
@NoArgsConstructor
public class ClienteNotificacion {

    @Id
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_cliente")
    private Cliente cliente;

    @Id
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_notificacion")
    private Notificacion notificacion;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_estado_notificacion", nullable = false)
    private EstadoNotificacion estadoNotificacion;

    private LocalDateTime fechaEnvio;

    private LocalDateTime fechaLectura;
}
