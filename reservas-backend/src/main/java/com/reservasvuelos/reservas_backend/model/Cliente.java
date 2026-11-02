package com.reservasvuelos.reservas_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// Subtipo de Usuario: codCliente es el mismo codUsuario.
@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    private Long codCliente;

    @NotNull
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "cod_cliente")
    private Usuario usuario;

    @NotNull
    @Column(nullable = false)
    private Boolean recibeNotificaciones = true;

    // Cuantas horas antes del viaje se envia el recordatorio.
    @PositiveOrZero
    private Integer horasRecordatorio;
}
