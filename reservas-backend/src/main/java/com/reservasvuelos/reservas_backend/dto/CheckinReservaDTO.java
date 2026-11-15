package com.reservasvuelos.reservas_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.reservasvuelos.reservas_backend.model.Equipaje;

public record CheckinReservaDTO(
        Long codReserva,
        String codigoReserva,
        String estadoReserva,
        Long codViaje,
        String codigoViaje,
        String origen,
        String destino,
        LocalDateTime salida,
        LocalDateTime abordaje,
        String puertaAnden,
        String estadoViaje,
        LocalDateTime abreCheckin,
        LocalDateTime cierraCheckin,
        boolean disponible,
        String motivo,
        List<PasajeroCheckin> pasajeros) {

    public record PasajeroCheckin(
            Long codTiquete,
            String numeroTiquete,
            String pasajero,
            String tipoPasajero,
            String estadoTiquete,
            String clase,
            BigDecimal equipajeIncluidoKg,
            String numeroAsiento,
            boolean checkinHecho,
            LocalDateTime fechaCheckin,
            String canal,
            String codigoPaseAbordar,
            String grupoAbordaje,
            List<EquipajeRegistrado> equipajes) {
    }

    public record EquipajeRegistrado(
            Integer numeroPieza,
            String codigoEtiqueta,
            String tipo,
            BigDecimal pesoKg,
            String dimensionesCm,
            BigDecimal costoAdicional,
            String estado) {

        public static EquipajeRegistrado desde(Equipaje equipaje) {
            return new EquipajeRegistrado(equipaje.getNumeroPieza(), equipaje.getCodigoEtiqueta(),
                    equipaje.getTipo().name(), equipaje.getPesoKg(), equipaje.getDimensionesCm(),
                    equipaje.getCostoAdicional(), equipaje.getEstado().name());
        }
    }
}
