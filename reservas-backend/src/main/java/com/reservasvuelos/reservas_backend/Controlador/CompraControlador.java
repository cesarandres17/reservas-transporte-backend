package com.reservasvuelos.reservas_backend.Controlador;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.dto.CompraDTO;
import com.reservasvuelos.reservas_backend.dto.MapaAsientosDTO;
import com.reservasvuelos.reservas_backend.dto.PagoCompraDTO;
import com.reservasvuelos.reservas_backend.dto.ReservaDetalleDTO;
import com.reservasvuelos.reservas_backend.dto.ViajeDisponibleDTO;
import com.reservasvuelos.reservas_backend.servicios.CompraServicio;
import com.reservasvuelos.reservas_backend.servicios.DisponibilidadServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/compras")
public class CompraControlador {

    private final DisponibilidadServicio disponibilidadServicio;
    private final CompraServicio compraServicio;

    public CompraControlador(DisponibilidadServicio disponibilidadServicio, CompraServicio compraServicio) {
        this.disponibilidadServicio = disponibilidadServicio;
        this.compraServicio = compraServicio;
    }

    @GetMapping("/viajes")
    public List<ViajeDisponibleDTO> buscar(@RequestParam Long origen, @RequestParam Long destino,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "1") int pasajeros, Authentication autenticacion) {
        return disponibilidadServicio.buscar(origen, destino, fecha, pasajeros, autenticacion);
    }

    @GetMapping("/viajes/{codViaje}/asientos")
    public MapaAsientosDTO asientos(@PathVariable Long codViaje) {
        return disponibilidadServicio.asientos(codViaje);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaDetalleDTO comprar(@Valid @RequestBody CompraDTO datos, Authentication autenticacion) {
        return compraServicio.comprar(datos, autenticacion);
    }

    @GetMapping("/mis-reservas")
    public List<ReservaDetalleDTO> misReservas(@RequestParam(required = false) Long cliente,
            Authentication autenticacion) {
        return compraServicio.misReservas(cliente, autenticacion);
    }

    @GetMapping("/{codReserva}")
    public ReservaDetalleDTO consultar(@PathVariable Long codReserva, Authentication autenticacion) {
        return compraServicio.consultar(codReserva, autenticacion);
    }

    @PostMapping("/{codReserva}/pagar")
    public ReservaDetalleDTO pagar(@PathVariable Long codReserva, @Valid @RequestBody PagoCompraDTO datos,
            Authentication autenticacion) {
        return compraServicio.pagar(codReserva, datos, autenticacion);
    }

    @PostMapping("/{codReserva}/cancelar")
    public ReservaDetalleDTO cancelar(@PathVariable Long codReserva, Authentication autenticacion) {
        return compraServicio.cancelar(codReserva, autenticacion);
    }
}
