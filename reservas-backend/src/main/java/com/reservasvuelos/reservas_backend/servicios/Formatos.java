package com.reservasvuelos.reservas_backend.servicios;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public final class Formatos {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter CUANDO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");
    private static final Map<String, String> NOMBRES = Map.ofEntries(
            Map.entry("ECONOMICA", "Económica"),
            Map.entry("APP_MOVIL", "App móvil"),
            Map.entry("AEREO", "Aéreo"),
            Map.entry("EMITIDO", "Emitido"),
            Map.entry("MANO", "De mano"),
            Map.entry("BODEGA", "De bodega"),
            Map.entry("ESPECIAL", "Especial"),
            Map.entry("NINO", "Niño"));

    private Formatos() {
    }

    public static String dinero(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return "$ " + new DecimalFormat("#,##0", simbolos).format(valor == null ? BigDecimal.ZERO : valor);
    }

    public static String porcentaje(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString().replace('.', ',') + " %";
    }

    public static String fecha(LocalDate fecha) {
        return fecha.format(FECHA);
    }

    public static String fechaHora(LocalDateTime fechaHora) {
        return fechaHora.format(FECHA_HORA);
    }

    public static String hora(LocalDateTime fechaHora) {
        return fechaHora.format(HORA);
    }

    public static String peso(BigDecimal kilos) {
        return kilos.stripTrailingZeros().toPlainString().replace('.', ',') + " kg";
    }

    public static String cuando(LocalDateTime fechaHora) {
        return fechaHora.format(CUANDO);
    }

    public static String legible(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return "";
        }
        String conocido = NOMBRES.get(codigo);
        if (conocido != null) {
            return conocido;
        }
        String texto = codigo.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    public static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 3).trim() + "...";
    }
}
