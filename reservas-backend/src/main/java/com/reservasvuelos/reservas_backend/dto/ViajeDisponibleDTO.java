package com.reservasvuelos.reservas_backend.dto;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.reservasvuelos.reservas_backend.model.ClaseServicio;
import com.reservasvuelos.reservas_backend.model.Ruta;
import com.reservasvuelos.reservas_backend.model.Tarifa;
import com.reservasvuelos.reservas_backend.model.Viaje;

public record ViajeDisponibleDTO(
        Long codViaje,
        String codigoViaje,
        String tipoTransporte,
        String empresa,
        String vehiculo,
        String ciudadOrigen,
        String terminalOrigen,
        String ciudadDestino,
        String terminalDestino,
        LocalDateTime fechaHoraSalida,
        LocalDateTime fechaHoraLlegada,
        long duracionMin,
        String puertaAnden,
        String estado,
        List<Clase> clases) {

    public record Clase(
            Long codClase,
            String nombre,
            String descripcion,
            BigDecimal precioBase,
            BigDecimal impuestos,
            BigDecimal precioTotal,
            Integer cuposDisponibles,
            BigDecimal equipajeIncluidoKg,
            Boolean permiteCambios,
            Boolean permiteReembolso) {

        public static Clase desde(Tarifa tarifa) {
            ClaseServicio clase = tarifa.getClaseServicio();
            return new Clase(
                    clase.getCodClase(),
                    clase.getNombre(),
                    clase.getDescripcion(),
                    tarifa.getPrecioBase(),
                    tarifa.getImpuestos(),
                    tarifa.getPrecioBase().add(tarifa.getImpuestos()),
                    tarifa.getCuposDisponibles(),
                    clase.getEquipajeIncluidoKg(),
                    clase.getPermiteCambios(),
                    clase.getPermiteReembolso());
        }
    }

    public static ViajeDisponibleDTO desde(Viaje viaje, List<Clase> clases) {
        Ruta ruta = viaje.getRuta();
        return new ViajeDisponibleDTO(
                viaje.getCodViaje(),
                viaje.getCodigoViaje(),
                ruta.getTerminalOrigen().getTipoTransporte().getNombre(),
                viaje.getVehiculo().getEmpresa().getNombre(),
                viaje.getVehiculo().getModelo(),
                ruta.getTerminalOrigen().getCiudad().getNombre(),
                ruta.getTerminalOrigen().getNombre(),
                ruta.getTerminalDestino().getCiudad().getNombre(),
                ruta.getTerminalDestino().getNombre(),
                viaje.getFechaHoraSalida(),
                viaje.getFechaHoraLlegada(),
                Duration.between(viaje.getFechaHoraSalida(), viaje.getFechaHoraLlegada()).toMinutes(),
                viaje.getPuertaAnden(),
                viaje.getEstado().getNombre(),
                clases);
    }
}
