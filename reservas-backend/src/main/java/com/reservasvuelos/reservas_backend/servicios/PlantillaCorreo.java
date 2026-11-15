package com.reservasvuelos.reservas_backend.servicios;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.reservasvuelos.reservas_backend.Repositorio.FacturaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PagoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ReservaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TiqueteRepositorio;
import com.reservasvuelos.reservas_backend.model.Factura;
import com.reservasvuelos.reservas_backend.model.Notificacion;
import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.Promocion;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.Viaje;
import com.reservasvuelos.reservas_backend.servicios.AvisoCreado.Adjunto;

@Component
public class PlantillaCorreo {

    private final ReservaRepositorio reservaRepositorio;
    private final TiqueteRepositorio tiqueteRepositorio;
    private final PagoRepositorio pagoRepositorio;
    private final FacturaRepositorio facturaRepositorio;
    private final DocumentoPdfServicio documentoPdfServicio;
    private final PaseAbordarPdf paseAbordarPdf;
    private final String empresa;

    public PlantillaCorreo(ReservaRepositorio reservaRepositorio, TiqueteRepositorio tiqueteRepositorio,
            PagoRepositorio pagoRepositorio, FacturaRepositorio facturaRepositorio,
            DocumentoPdfServicio documentoPdfServicio, PaseAbordarPdf paseAbordarPdf,
            @Value("${app.empresa.nombre:Reservas de Transporte S.A.S.}") String empresa) {
        this.reservaRepositorio = reservaRepositorio;
        this.tiqueteRepositorio = tiqueteRepositorio;
        this.pagoRepositorio = pagoRepositorio;
        this.facturaRepositorio = facturaRepositorio;
        this.documentoPdfServicio = documentoPdfServicio;
        this.paseAbordarPdf = paseAbordarPdf;
        this.empresa = empresa;
    }

    public record ContenidoCorreo(String asunto, String html, String texto, String nombreAdjunto, byte[] adjunto) {
    }

    public ContenidoCorreo contenido(Notificacion notificacion, Long codReserva, Adjunto tipoAdjunto,
            String nombreCliente, String avisoPrueba) {
        Map<String, String> datos = new LinkedHashMap<>();
        List<String[]> tiquetes = new ArrayList<>();
        String nombreAdjunto = null;
        byte[] adjunto = null;

        Reserva reserva = codReserva == null ? null : reservaRepositorio.buscarPorId(codReserva).orElse(null);
        if (reserva != null) {
            datos.put("Código de reserva", reserva.getCodigoReserva());
        }
        Viaje viaje = notificacion.getViaje();
        if (viaje != null) {
            datos.put("Viaje", viaje.getCodigoViaje() + ", " + viaje.getRuta().getTerminalOrigen().getCiudad().getNombre()
                    + " a " + viaje.getRuta().getTerminalDestino().getCiudad().getNombre());
            datos.put("Salida", Formatos.fechaHora(viaje.getFechaHoraSalida()) + ", "
                    + viaje.getRuta().getTerminalOrigen().getNombre());
            if (viaje.getPuertaAnden() != null) {
                datos.put("Puerta o andén", viaje.getPuertaAnden());
            }
            datos.put("Estado del viaje", Formatos.legible(viaje.getEstado().getNombre()));
        }
        if (reserva != null) {
            String estado = reserva.getEstado().getNombre();
            datos.put("Estado de la reserva", Formatos.legible(estado));
            datos.put("Total", Formatos.dinero(reserva.getTotal()));
            if (estado.equals("PENDIENTE") && reserva.getFechaLimitePago() != null) {
                datos.put("Paga antes del", Formatos.fechaHora(reserva.getFechaLimitePago()));
            }
            List<Tiquete> lista = tiqueteRepositorio.deReserva(codReserva);
            for (Tiquete tiquete : lista) {
                Pasajero pasajero = tiquete.getPasajero();
                tiquetes.add(new String[] {
                        pasajero.getPrimerNombre() + " " + pasajero.getPrimerApellido(),
                        tiquete.getAsiento() == null ? "Sin asignar" : tiquete.getAsiento().getNumeroAsiento(),
                        Formatos.legible(tiquete.getTarifa().getClaseServicio().getNombre()),
                        tiquete.getNumeroTiquete(),
                        Formatos.legible(tiquete.getEstado().getNombre())});
            }
            Factura factura = tipoAdjunto == Adjunto.FACTURA ? facturaRepositorio.buscarPorId(codReserva).orElse(null)
                    : null;
            if (factura != null) {
                adjunto = documentoPdfServicio.factura(factura, lista,
                        pagoRepositorio.deReserva(codReserva));
                nombreAdjunto = "factura-" + factura.getNumeroFactura() + ".pdf";
            }
            if (tipoAdjunto == Adjunto.PASES_ABORDAR) {
                adjunto = paseAbordarPdf.deReserva(codReserva);
                nombreAdjunto = adjunto == null ? null : "pases-abordar-" + reserva.getCodigoReserva() + ".pdf";
                if (viaje != null) {
                    datos.put("Abordaje", Formatos.hora(ReglasAbordaje.inicioAbordaje(viaje)) + " a "
                            + Formatos.hora(ReglasAbordaje.cierrePuerta(viaje)));
                }
            }
        }
        Promocion promocion = notificacion.getPromocion();
        if (promocion != null) {
            datos.put("Código promocional", promocion.getCodigo());
            datos.put("Descuento", Formatos.porcentaje(promocion.getPorcentajeDescuento()));
            datos.put("Vigencia", Formatos.fecha(promocion.getFechaInicio()) + " al "
                    + Formatos.fecha(promocion.getFechaFin()));
        }

        String saludo = nombreCliente == null ? "Hola," : "Hola, " + nombreCliente + ":";
        boolean promocional = promocion != null;
        String html = html(notificacion.getTitulo(), saludo, notificacion.getMensaje(), datos, tiquetes, promocional,
                avisoPrueba);
        String texto = texto(notificacion.getTitulo(), saludo, notificacion.getMensaje(), datos, tiquetes,
                avisoPrueba);
        return new ContenidoCorreo(notificacion.getTitulo(), html, texto, nombreAdjunto, adjunto);
    }

    private String html(String titulo, String saludo, String mensaje, Map<String, String> datos,
            List<String[]> tiquetes, boolean promocional, String avisoPrueba) {
        StringBuilder cuerpo = new StringBuilder();
        if (avisoPrueba != null) {
            cuerpo.append("<div style=\"margin:0 0 20px;padding:12px 14px;background:#fef3d6;border:1px solid #f2d27a;"
                    + "border-radius:6px;font-size:13px;color:#7a4f00\">")
                    .append(e(avisoPrueba)).append("</div>");
        }
        cuerpo.append("<h1 style=\"margin:0 0 16px;font-size:21px;line-height:1.3;color:#1f3a5f\">")
                .append(e(titulo)).append("</h1>")
                .append("<p style=\"margin:0 0 8px;font-size:15px;line-height:1.6\">").append(e(saludo)).append("</p>")
                .append("<p style=\"margin:0 0 22px;font-size:15px;line-height:1.6\">").append(e(mensaje)).append("</p>");
        if (!datos.isEmpty()) {
            cuerpo.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                    + "style=\"border-collapse:collapse;margin:0 0 22px;background:#f4f7fb;border-radius:6px\">");
            datos.forEach((etiqueta, valor) -> cuerpo
                    .append("<tr><td style=\"padding:9px 14px;font-size:13px;color:#5b6573;width:38%;"
                            + "border-bottom:1px solid #e3e8ef\">").append(e(etiqueta)).append("</td>")
                    .append("<td style=\"padding:9px 14px;font-size:14px;color:#1f2933;font-weight:bold;"
                            + "border-bottom:1px solid #e3e8ef\">").append(e(valor)).append("</td></tr>"));
            cuerpo.append("</table>");
        }
        if (!tiquetes.isEmpty()) {
            cuerpo.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                    + "style=\"border-collapse:collapse;margin:0 0 22px\"><tr>");
            for (String encabezado : new String[] {"Pasajero", "Silla", "Clase", "Tiquete", "Estado"}) {
                cuerpo.append("<th align=\"left\" style=\"padding:8px 10px;background:#1f3a5f;color:#ffffff;"
                        + "font-size:12px;font-weight:bold\">").append(encabezado).append("</th>");
            }
            cuerpo.append("</tr>");
            for (String[] fila : tiquetes) {
                cuerpo.append("<tr>");
                for (String celda : fila) {
                    cuerpo.append("<td style=\"padding:8px 10px;font-size:13px;color:#1f2933;"
                            + "border-bottom:1px solid #e3e8ef\">").append(e(celda)).append("</td>");
                }
                cuerpo.append("</tr>");
            }
            cuerpo.append("</table>");
        }
        String pie = promocional
                ? "Recibes promociones porque las activaste en tu cuenta de " + conPunto(empresa)
                : "Este es un mensaje automático de " + conPunto(empresa)
                        + " También lo puedes ver en la sección de notificaciones de tu cuenta.";
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#eef2f7;font-family:Arial,Helvetica,sans-serif;color:#1f2933">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#eef2f7;padding:24px 12px">
                <tr><td align="center">
                <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="width:100%%;max-width:600px;background:#ffffff;border-radius:8px;overflow:hidden">
                <tr><td style="background:#1f3a5f;padding:18px 28px;color:#ffffff;font-size:17px;font-weight:bold">%s</td></tr>
                <tr><td style="padding:28px">%s</td></tr>
                <tr><td style="padding:16px 28px;background:#f4f7fb;color:#5b6573;font-size:12px;line-height:1.5">%s</td></tr>
                </table>
                </td></tr>
                </table>
                </body>
                </html>
                """.formatted(e(empresa), cuerpo, e(pie));
    }

    private String texto(String titulo, String saludo, String mensaje, Map<String, String> datos,
            List<String[]> tiquetes, String avisoPrueba) {
        StringBuilder texto = new StringBuilder();
        if (avisoPrueba != null) {
            texto.append(avisoPrueba).append("\n\n");
        }
        texto.append(titulo).append("\n\n").append(saludo).append("\n\n").append(mensaje).append("\n\n");
        datos.forEach((etiqueta, valor) -> texto.append(etiqueta).append(": ").append(valor).append('\n'));
        for (String[] fila : tiquetes) {
            texto.append("- ").append(fila[0]).append(", silla ").append(fila[1]).append(", tiquete ").append(fila[3])
                    .append('\n');
        }
        return texto.append('\n').append(empresa).toString();
    }

    private static String conPunto(String texto) {
        return texto.endsWith(".") ? texto : texto + ".";
    }

    private static String e(String texto) {
        return HtmlUtils.htmlEscape(texto == null ? "" : texto, "UTF-8");
    }
}
