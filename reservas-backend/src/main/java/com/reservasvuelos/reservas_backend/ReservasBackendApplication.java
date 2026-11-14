package com.reservasvuelos.reservas_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReservasBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservasBackendApplication.class, args);
	}

}
