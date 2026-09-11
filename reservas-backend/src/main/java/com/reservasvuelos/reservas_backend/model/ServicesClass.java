package com.reservasvuelos.reservas_backend.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor 
@NotBlank 
public class ServicesClass {


    private Long idServicesClass;
    private String name;
    private String description;
    private double priceFactor;
    
}
