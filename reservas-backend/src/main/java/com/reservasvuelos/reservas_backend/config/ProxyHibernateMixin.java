package com.reservasvuelos.reservas_backend.config;

import org.springframework.boot.jackson.JacksonMixin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JacksonMixin(Object.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class ProxyHibernateMixin {
}
