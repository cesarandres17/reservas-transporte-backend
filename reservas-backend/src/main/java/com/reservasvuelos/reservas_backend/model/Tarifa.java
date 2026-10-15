package com.reservasvuelos.reservas_backend.model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Getter 
@Setter 
@NoArgsConstructor
@Entity 
@IdClass(TarifaId.class)
@Table(name = "tarifa")

public class Tarifa {

    @Id                             
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_vehiculo")
    private Viaje viaje;   
    
    

    @Id
@ToString.Exclude
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "cod_clase")
private ClaseServicio claseServicio;

    

   
    
    
    
}
