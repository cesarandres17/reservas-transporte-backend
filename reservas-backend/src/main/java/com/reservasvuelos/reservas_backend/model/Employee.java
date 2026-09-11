package com.reservasvuelos.reservas_backend.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data 
@NotBlank 
@AllArgsConstructor 
public class Employee {
    private Long idEmployee;
    private Long document;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private String state;
    
    
}
