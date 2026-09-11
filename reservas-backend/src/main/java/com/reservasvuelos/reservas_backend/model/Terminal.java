package com.reservasvuelos.reservas_backend.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
@NotBlank 
public class Terminal {

    private Long idTerminal;
    private String name;
    private String city;
    private String country;
    private String type;
    
    
    
}
