package com.reservasvuelos.reservas_backend.servicios;

import java.time.Duration;
import java.time.LocalDateTime;

import com.reservasvuelos.reservas_backend.model.Viaje;

public final class ReglasAbordaje {

    public static final Duration APERTURA_CHECKIN = Duration.ofHours(48);

    private static final Duration CIERRE_AEREO = Duration.ofMinutes(60);
    private static final Duration CIERRE_TERRESTRE = Duration.ofMinutes(15);
    private static final Duration ABORDAJE_AEREO = Duration.ofMinutes(40);
    private static final Duration ABORDAJE_TERRESTRE = Duration.ofMinutes(15);
    private static final Duration CIERRE_PUERTA_AEREO = Duration.ofMinutes(15);
    private static final Duration CIERRE_PUERTA_TERRESTRE = Duration.ofMinutes(5);

    private ReglasAbordaje() {
    }

    public static boolean esAereo(Viaje viaje) {
        return !"TERRESTRE".equalsIgnoreCase(viaje.getRuta().getTerminalOrigen().getTipoTransporte().getNombre());
    }

    public static LocalDateTime abreCheckin(Viaje viaje) {
        return viaje.getFechaHoraSalida().minus(APERTURA_CHECKIN);
    }

    public static LocalDateTime cierraCheckin(Viaje viaje) {
        return viaje.getFechaHoraSalida().minus(esAereo(viaje) ? CIERRE_AEREO : CIERRE_TERRESTRE);
    }

    public static Duration anticipacionCierre(Viaje viaje) {
        return esAereo(viaje) ? CIERRE_AEREO : CIERRE_TERRESTRE;
    }

    public static LocalDateTime inicioAbordaje(Viaje viaje) {
        return viaje.getFechaHoraSalida().minus(esAereo(viaje) ? ABORDAJE_AEREO : ABORDAJE_TERRESTRE);
    }

    public static LocalDateTime cierrePuerta(Viaje viaje) {
        return viaje.getFechaHoraSalida().minus(esAereo(viaje) ? CIERRE_PUERTA_AEREO : CIERRE_PUERTA_TERRESTRE);
    }

    public static String duracion(Duration duracion) {
        long minutos = duracion.toMinutes();
        if (minutos % 60 == 0) {
            long horas = minutos / 60;
            return horas + (horas == 1 ? " hora" : " horas");
        }
        return minutos + " minutos";
    }
}
