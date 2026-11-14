package com.reservasvuelos.reservas_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.reservasvuelos.reservas_backend.servicios.AuthServicio;

@Configuration
public class SeguridadConfig {

    private static final String ADMINISTRADOR = AuthServicio.ROL_ADMINISTRADOR;
    private static final String EMPLEADO = AuthServicio.ROL_EMPLEADO;

    private static final String[] CONSULTA_PUBLICA = {
            "/paises/**", "/departamentos/**", "/ciudades/**", "/tipos-documento/**", "/tipos-transporte/**",
            "/clases-servicio/**", "/promociones/**", "/metodos-pago/**", "/empresas/**", "/terminales/**",
            "/rutas/**", "/compras/viajes/**"
    };

    private static final String[] CONFIGURACION = {
            "/paises/**", "/departamentos/**", "/ciudades/**", "/tipos-documento/**", "/tipos-transporte/**",
            "/clases-servicio/**", "/promociones/**", "/metodos-pago/**", "/empresas/**", "/terminales/**",
            "/rutas/**", "/vehiculos/**", "/asientos/**", "/estados-reserva/**", "/estados-pago/**",
            "/estados-tiquete/**", "/estados-viaje/**", "/estados-notificacion/**", "/tipos-trayecto/**",
            "/tipos-notificacion/**"
    };

    private static final String[] SOLO_ADMINISTRADOR = {
            "/usuarios/**", "/empleados/**", "/roles/**", "/cargos/**"
    };

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/registro").permitAll()
                .requestMatchers(HttpMethod.GET, CONSULTA_PUBLICA).permitAll()
                .requestMatchers("/auth/**", "/compras/**", "/notificaciones/mias/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/facturas/*/pdf", "/pagos/*/comprobante").authenticated()
                .requestMatchers(HttpMethod.DELETE).hasRole(ADMINISTRADOR)
                .requestMatchers(SOLO_ADMINISTRADOR).hasRole(ADMINISTRADOR)
                .requestMatchers(HttpMethod.POST, CONFIGURACION).hasRole(ADMINISTRADOR)
                .requestMatchers(HttpMethod.PUT, CONFIGURACION).hasRole(ADMINISTRADOR)
                .anyRequest().hasAnyRole(ADMINISTRADOR, EMPLEADO))
            .exceptionHandling(errores -> errores
                .accessDeniedHandler((solicitud, respuesta, excepcion) -> respuesta.sendError(
                        HttpStatus.FORBIDDEN.value(), "Tu rol no tiene permiso para esta operación")))
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
