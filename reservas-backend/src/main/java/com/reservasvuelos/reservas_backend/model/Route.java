package com.reservasvuelos.reservas_backend.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
@NotBlank 
public class Route {

    private Long idRoute;
    private Terminal originTerminal;
    private Terminal destinationTerminal ;
    private Double distanceKm;
    private Integer estimatedDurationMinutes;
    
}
