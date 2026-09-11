package com.reservasvuelos.reservas_backend.model;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NotBlank 
public class TransportOperator {
    private Long idTransportOperator;
    private String name;
    private int nit;
    private String tipe;
    private String country;
    private String webSite;



    
}
