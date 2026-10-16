package com.reservasvuelos.reservas_backend.model;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
@NotBlank 

public class Seat {
    private Long idSeat;
    private Vehiculo vehicle;
    private String seatNumber;
    private ClaseServicio serviceClass;
    private String position;
    private boolean isAvailable;


    
}
