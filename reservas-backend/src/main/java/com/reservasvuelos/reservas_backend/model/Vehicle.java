package com.reservasvuelos.reservas_backend.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
@NotBlank 
public class Vehicle {

    private Long idVehicle;
    private TransportOperator transportOperator;
    private String type;
    private String model;
    private String licensePlate;
    private int capacity;





    
}
