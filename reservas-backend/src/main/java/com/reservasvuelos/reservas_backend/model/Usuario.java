package com.reservasvuelos.reservas_backend.model;

import com.reservasvuelos.reservas_backend.model.enums.EstadoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codUsuario;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String nombre;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false, unique = true)
    private String login;

    // Se guarda el hash BCrypt, nunca la clave en texto plano.
    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoUsuario estado;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_rol", nullable = false)
    private Rol rol;
}
