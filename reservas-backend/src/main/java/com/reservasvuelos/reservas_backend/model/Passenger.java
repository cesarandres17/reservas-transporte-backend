package com.reservasvuelos.reservas_backend.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;



@Data 
@AllArgsConstructor 
@NotBlank 
public class Passenger {

    private Long idPassenger;
    private Long docuemnt;
    private String name;
    private String lastName;
    private LocalDate birthDay;
    private String email;
    private String phone;

    @ToString.Exclude
    private String password;
    private String nationality;
    private LocalDate registrationDate;
    private String state;
    
}
