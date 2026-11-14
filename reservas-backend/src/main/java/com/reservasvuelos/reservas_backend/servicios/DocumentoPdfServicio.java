package com.reservasvuelos.reservas_backend.servicios;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfTemplate;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.model.Equipaje;
import com.reservasvuelos.reservas_backend.model.Factura;
import com.reservasvuelos.reservas_backend.model.Pago;
import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.Ruta;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.Usuario;
import com.reservasvuelos.reservas_backend.model.Viaje;

@Service
public class DocumentoPdfServicio {

    private static final Color AZUL_OSCURO = new Color(0x1F, 0x3A, 0x5F);
    private static final Color AZUL = new Color(0x2A, 0x78, 0xD6);
    private static final Color AZUL_CLARO = new Color(0xB7, 0xD3, 0xF6);
    private static final Color TEXTO = new Color(0x1F, 0x29, 0x33);
    private static final Color TEXTO_SUAVE = new Color(0x5B, 0x65, 0x73);
    private static final Color LINEA = new Color(0xD9, 0xDE, 0xE5);
    private static final Color FONDO = new Color(0xF4, 0xF7, 0xFB);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Map<String, String> NOMBRES = Map.ofEntries(
            Map.entry("APP_MOVIL", "App móvil"),
            Map.entry("TARJETA_CREDITO", "Tarjeta de crédito"),
            Map.entry("TARJETA_DEBITO", "Tarjeta débito"),
            Map.entry("PSE", "PSE"),
            Map.entry("ECONOMICA", "Económica"),
            Map.entry("AEREO", "Aéreo"));
    private static final Map<String, Color[]> COLORES_ESTADO = Map.of(
            "APROBADO", new Color[] {new Color(0xE3, 0xF5, 0xE3), new Color(0x0A, 0x6E, 0x0A)},
            "RECHAZADO", new Color[] {new Color(0xFB, 0xE4, 0xE4), new Color(0xB0, 0x2A, 0x2A)},
            "PENDIENTE", new Color[] {new Color(0xFE, 0xF3, 0xD6), new Color(0x8A, 0x5A, 0x00)},
            "REEMBOLSADO", new Color[] {new Color(0xE6, 0xEE, 0xF8), AZUL_OSCURO});
    private static final Map<String, String> MENSAJES_ESTADO = Map.of(
            "APROBADO", "La entidad aprobó el pago y el valor quedó abonado a la reserva.",
            "RECHAZADO", "La entidad rechazó el pago. No se realizó ningún cobro.",
            "PENDIENTE", "El pago está pendiente de confirmación por parte de la entidad.",
            "REEMBOLSADO", "El valor de este pago fue devuelto al cliente.");
    private static final String[] UNIDADES = {"", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO",
        "NUEVE", "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISÉIS", "DIECISIETE", "DIECIOCHO",
        "DIECINUEVE", "VEINTE", "VEINTIUNO", "VEINTIDÓS", "VEINTITRÉS", "VEINTICUATRO", "VEINTICINCO", "VEINTISÉIS",
        "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE"};
    private static final String[] DECENAS = {"", "", "", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA",
        "OCHENTA", "NOVENTA"};
    private static final String[] CENTENAS = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
        "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};

    private final String empresaNombre;
    private final String empresaNit;
    private final String empresaDireccion;
    private final String empresaTelefono;
    private final String empresaEmail;

    public DocumentoPdfServicio(
            @Value("${app.empresa.nombre:Reservas de Transporte S.A.S.}") String empresaNombre,
            @Value("${app.empresa.nit:900.000.000-1}") String empresaNit,
            @Value("${app.empresa.direccion:Armenia, Quindío, Colombia}") String empresaDireccion,
            @Value("${app.empresa.telefono:+57 300 000 0000}") String empresaTelefono,
            @Value("${app.empresa.email:facturacion@reservas.com}") String empresaEmail) {
        this.empresaNombre = empresaNombre;
        this.empresaNit = empresaNit;
        this.empresaDireccion = empresaDireccion;
        this.empresaTelefono = empresaTelefono;
        this.empresaEmail = empresaEmail;
    }

    public byte[] factura(Factura factura, List<Tiquete> tiquetes, List<Pago> pagos) {
        Reserva reserva = factura.getReserva();
        Document documento = new Document(PageSize.A4, 36, 36, 36, 54);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(documento, salida);
        writer.setPageEvent(new PiePagina(empresaNombre + "  ·  Factura " + factura.getNumeroFactura()));
        documento.addTitle("Factura " + factura.getNumeroFactura());
        documento.addAuthor(empresaNombre);
        documento.open();

        PdfPTable encabezado = new PdfPTable(new float[] {58, 42});
        encabezado.setWidthPercentage(100);
        encabezado.addCell(celdaEmpresa());
        PdfPCell caja = celdaVacia();
        caja.setBackgroundColor(AZUL_OSCURO);
        caja.setPadding(14);
        caja.addElement(parrafo("FACTURA DE VENTA", fuente(8.5f, Font.BOLD, AZUL_CLARO)));
        caja.addElement(parrafo("No. " + factura.getNumeroFactura(), fuente(17, Font.BOLD, Color.WHITE), 4));
        caja.addElement(parrafo("Emitida el " + FECHA_HORA.format(factura.getFechaEmision()),
                fuente(8.5f, Font.NORMAL, Color.WHITE), 6));
        caja.addElement(parrafo("Reserva " + reserva.getCodigoReserva(), fuente(8.5f, Font.NORMAL, Color.WHITE)));
        encabezado.addCell(caja);
        documento.add(encabezado);

        Usuario cliente = reserva.getCliente().getUsuario();
        PdfPTable datos = new PdfPTable(new float[] {50, 50});
        datos.setWidthPercentage(100);
        datos.setSpacingBefore(14);
        datos.addCell(recuadro("CLIENTE", List.of(
                new String[] {null, nombre(cliente)},
                new String[] {"Documento", cliente.getTipoDocumento().getSigla() + " " + cliente.getNumeroDocumento()},
                new String[] {"Correo", cliente.getEmail()},
                new String[] {"Teléfono", texto(cliente.getTelefono())},
                new String[] {"Dirección", direccion(cliente)})));
        datos.addCell(recuadro("RESERVA", List.of(
                new String[] {null, "Código " + reserva.getCodigoReserva()},
                new String[] {"Fecha", FECHA_HORA.format(reserva.getFechaReserva())},
                new String[] {"Canal", nombre(reserva.getCanal().name())},
                new String[] {"Estado", nombre(reserva.getEstado().getNombre())},
                new String[] {"Atendida por", reserva.getEmpleado() == null ? "Compra en línea"
                        : nombre(reserva.getEmpleado().getUsuario())},
                new String[] {"Promoción", reserva.getPromocion() == null ? "Sin promoción"
                        : reserva.getPromocion().getCodigo() + " ("
                                + porcentaje(reserva.getPromocion().getPorcentajeDescuento()) + ")"})));
        documento.add(datos);

        documento.add(subtitulo("Detalle de los tiquetes"));
        PdfPTable detalle = new PdfPTable(new float[] {16, 19, 27, 11, 12, 11, 12});
        detalle.setWidthPercentage(100);
        detalle.setHeaderRows(1);
        String[] columnas = {"Tiquete", "Pasajero", "Viaje", "Clase / silla", "Base", "Impuestos", "Valor"};
        for (int i = 0; i < columnas.length; i++) {
            detalle.addCell(celdaEncabezado(columnas[i], i >= 4 ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT));
        }
        BigDecimal totalImpuestos = BigDecimal.ZERO;
        int fila = 0;
        for (Tiquete tiquete : tiquetes) {
            Color fondo = fila++ % 2 == 1 ? FONDO : Color.WHITE;
            BigDecimal base = tiquete.getTarifa().getPrecioBase();
            BigDecimal impuestos = tiquete.getTarifa().getImpuestos();
            totalImpuestos = totalImpuestos.add(impuestos);
            Viaje viaje = tiquete.getTarifa().getViaje();
            detalle.addCell(celdaDato(tiquete.getNumeroTiquete(), Element.ALIGN_LEFT, fondo));
            detalle.addCell(celdaDato(nombre(tiquete.getPasajero()), Element.ALIGN_LEFT, fondo));
            detalle.addCell(celdaDosLineas(viaje.getCodigoViaje() + "  " + trayecto(viaje.getRuta()),
                    "Sale " + FECHA_HORA.format(viaje.getFechaHoraSalida()), fondo));
            detalle.addCell(celdaDosLineas(nombre(tiquete.getTarifa().getClaseServicio().getNombre()),
                    silla(tiquete.getAsiento()), fondo));
            detalle.addCell(celdaDato(dinero(base), Element.ALIGN_RIGHT, fondo));
            detalle.addCell(celdaDato(dinero(impuestos), Element.ALIGN_RIGHT, fondo));
            detalle.addCell(celdaDato(dinero(base.add(impuestos)), Element.ALIGN_RIGHT, fondo));
        }
        documento.add(detalle);

        PdfPTable resumen = new PdfPTable(new float[] {55, 45});
        resumen.setWidthPercentage(100);
        resumen.setSpacingBefore(10);
        resumen.setKeepTogether(true);
        PdfPCell letras = celdaVacia();
        letras.setPaddingRight(18);
        letras.addElement(parrafo("VALOR EN LETRAS", fuente(8, Font.BOLD, AZUL)));
        letras.addElement(parrafo(enLetras(reserva.getTotal()), fuente(9, Font.NORMAL, TEXTO), 3));
        String notaImpuestos = totalImpuestos.signum() == 0 ? "Esta compra no tiene impuestos."
                : "Incluye impuestos por " + dinero(totalImpuestos) + ".";
        letras.addElement(parrafo(notaImpuestos, fuente(8.5f, Font.NORMAL, TEXTO_SUAVE), 8));
        resumen.addCell(letras);
        PdfPTable totales = new PdfPTable(new float[] {60, 40});
        totales.setWidthPercentage(100);
        filaTotal(totales, "Subtotal", dinero(reserva.getSubtotal()), false);
        String etiquetaDescuento = reserva.getPromocion() == null ? "Descuento"
                : "Descuento " + reserva.getPromocion().getCodigo();
        filaTotal(totales, etiquetaDescuento, "- " + dinero(reserva.getDescuento()), false);
        filaTotal(totales, "Total", dinero(reserva.getTotal()), true);
        PdfPCell celdaTotales = celdaVacia();
        celdaTotales.addElement(totales);
        resumen.addCell(celdaTotales);
        documento.add(resumen);

        documento.add(subtitulo("Pagos registrados"));
        if (pagos.isEmpty()) {
            documento.add(parrafo("La reserva no tiene pagos registrados.", fuente(9, Font.ITALIC, TEXTO_SUAVE)));
        } else {
            PdfPTable tablaPagos = new PdfPTable(new float[] {17, 22, 9, 20, 15, 17});
            tablaPagos.setWidthPercentage(100);
            tablaPagos.setHeaderRows(1);
            String[] titulos = {"Fecha", "Método", "Cuotas", "Referencia", "Estado", "Valor"};
            for (int i = 0; i < titulos.length; i++) {
                tablaPagos.addCell(celdaEncabezado(titulos[i], i == 2 ? Element.ALIGN_CENTER
                        : i == 5 ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT));
            }
            fila = 0;
            for (Pago pago : pagos) {
                Color fondo = fila++ % 2 == 1 ? FONDO : Color.WHITE;
                tablaPagos.addCell(celdaDato(FECHA_HORA.format(pago.getFechaPago()), Element.ALIGN_LEFT, fondo));
                tablaPagos.addCell(celdaDato(pago.getMetodoPago().getNombre(), Element.ALIGN_LEFT, fondo));
                tablaPagos.addCell(celdaDato(pago.getCuotas() == null ? "-" : String.valueOf(pago.getCuotas()),
                        Element.ALIGN_CENTER, fondo));
                tablaPagos.addCell(celdaDato(texto(pago.getReferenciaTransaccion()), Element.ALIGN_LEFT, fondo));
                tablaPagos.addCell(celdaDato(nombre(pago.getEstado().getNombre()), Element.ALIGN_LEFT, fondo));
                tablaPagos.addCell(celdaDato(dinero(pago.getMonto()), Element.ALIGN_RIGHT, fondo));
            }
            documento.add(tablaPagos);
        }

        Paragraph nota = parrafo("Gracias por viajar con nosotros. Presenta tu documento de identidad y el código de "
                + "reserva " + reserva.getCodigoReserva() + " al momento de abordar.",
                fuente(8.5f, Font.NORMAL, TEXTO_SUAVE), 22);
        documento.add(nota);
        documento.close();
        return salida.toByteArray();
    }

    public byte[] comprobante(Pago pago, List<Tiquete> tiquetes, Factura factura, List<Equipaje> piezas) {
        Reserva reserva = pago.getReserva();
        String numero = String.format("CP-%06d", pago.getCodPago());
        String estado = pago.getEstado().getNombre();
        Document documento = new Document(PageSize.A4, 36, 36, 36, 54);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(documento, salida);
        writer.setPageEvent(new PiePagina(empresaNombre + "  ·  Comprobante " + numero));
        documento.addTitle("Comprobante de pago " + numero);
        documento.addAuthor(empresaNombre);
        documento.open();

        PdfPTable banda = new PdfPTable(new float[] {62, 38});
        banda.setWidthPercentage(100);
        PdfPCell izquierda = celdaVacia();
        izquierda.setBackgroundColor(AZUL_OSCURO);
        izquierda.setPadding(16);
        izquierda.addElement(parrafo(empresaNombre.toUpperCase(Locale.ROOT), fuente(8.5f, Font.BOLD, AZUL_CLARO)));
        izquierda.addElement(parrafo("Comprobante de pago", fuente(20, Font.BOLD, Color.WHITE), 4));
        banda.addCell(izquierda);
        PdfPCell derecha = celdaVacia();
        derecha.setBackgroundColor(AZUL_OSCURO);
        derecha.setPadding(16);
        derecha.addElement(parrafo("No. " + numero, fuente(12, Font.BOLD, Color.WHITE), 0, Element.ALIGN_RIGHT));
        derecha.addElement(parrafo("Generado el " + FECHA_HORA.format(LocalDateTime.now()),
                fuente(8.5f, Font.NORMAL, Color.WHITE), 6, Element.ALIGN_RIGHT));
        banda.addCell(derecha);
        documento.add(banda);

        PdfPTable valor = new PdfPTable(new float[] {62, 38});
        valor.setWidthPercentage(100);
        valor.setSpacingBefore(16);
        PdfPCell monto = celdaVacia();
        monto.setBorder(Rectangle.BOX);
        monto.setBorderColor(LINEA);
        monto.setPadding(14);
        monto.addElement(parrafo("VALOR DEL PAGO", fuente(8, Font.BOLD, AZUL)));
        monto.addElement(parrafo(dinero(pago.getMonto()), fuente(24, Font.BOLD, AZUL_OSCURO), 4));
        monto.addElement(parrafo(enLetras(pago.getMonto()), fuente(8, Font.NORMAL, TEXTO_SUAVE), 4));
        valor.addCell(monto);
        Color[] colores = COLORES_ESTADO.getOrDefault(estado, new Color[] {FONDO, TEXTO});
        PdfPCell celdaEstado = celdaVacia();
        celdaEstado.setBackgroundColor(colores[0]);
        celdaEstado.setPadding(14);
        celdaEstado.addElement(parrafo("ESTADO", fuente(8, Font.BOLD, colores[1])));
        celdaEstado.addElement(parrafo(nombre(estado), fuente(16, Font.BOLD, colores[1]), 4));
        celdaEstado.addElement(parrafo(MENSAJES_ESTADO.getOrDefault(estado, ""), fuente(8.5f, Font.NORMAL, TEXTO), 4));
        valor.addCell(celdaEstado);
        documento.add(valor);

        Usuario cliente = reserva.getCliente().getUsuario();
        documento.add(subtitulo("Datos del pago"));
        PdfPTable detalle = new PdfPTable(new float[] {32, 68});
        detalle.setWidthPercentage(100);
        filaDato(detalle, "Fecha y hora del pago", FECHA_HORA.format(pago.getFechaPago()));
        String metodo = pago.getMetodoPago().getNombre();
        String tipoMetodo = nombre(pago.getMetodoPago().getTipo().name());
        filaDato(detalle, "Método de pago", metodo.equalsIgnoreCase(tipoMetodo) ? metodo : metodo + "  ·  " + tipoMetodo);
        if (pago.getCuotas() != null) {
            filaDato(detalle, "Cuotas", String.valueOf(pago.getCuotas()));
        }
        filaDato(detalle, "Referencia de la transacción", texto(pago.getReferenciaTransaccion()));
        filaDato(detalle, "Código de reserva", reserva.getCodigoReserva());
        if (piezas == null) {
            filaDato(detalle, "Factura", factura == null ? "Sin factura emitida" : factura.getNumeroFactura());
            filaDato(detalle, "Total de la reserva", dinero(reserva.getTotal()));
        } else {
            filaDato(detalle, "Concepto", "Equipaje adicional");
        }
        filaDato(detalle, "Cliente", nombre(cliente));
        filaDato(detalle, "Documento", cliente.getTipoDocumento().getSigla() + " " + cliente.getNumeroDocumento());
        filaDato(detalle, "Correo", cliente.getEmail());
        documento.add(detalle);

        documento.add(subtitulo("Concepto"));
        if (piezas != null) {
            Tiquete tiquete = piezas.isEmpty() ? null : piezas.get(0).getTiquete();
            if (tiquete != null) {
                Viaje viaje = tiquete.getTarifa().getViaje();
                documento.add(parrafo("Equipaje de " + nombre(tiquete.getPasajero()) + ", tiquete "
                        + tiquete.getNumeroTiquete() + ", viaje " + viaje.getCodigoViaje() + "  " + trayecto(viaje.getRuta())
                        + " del " + FECHA_HORA.format(viaje.getFechaHoraSalida()) + ".",
                        fuente(9, Font.NORMAL, TEXTO), 0));
            }
            PdfPTable tablaPiezas = new PdfPTable(new float[] {25, 25, 25, 25});
            tablaPiezas.setWidthPercentage(100);
            tablaPiezas.setSpacingBefore(8);
            tablaPiezas.setHeaderRows(1);
            String[] titulos = {"Etiqueta", "Tipo", "Peso", "Valor"};
            for (int i = 0; i < titulos.length; i++) {
                tablaPiezas.addCell(celdaEncabezado(titulos[i], i >= 2 ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT));
            }
            int fila = 0;
            for (Equipaje pieza : piezas) {
                Color fondo = fila++ % 2 == 1 ? FONDO : Color.WHITE;
                tablaPiezas.addCell(celdaDato(pieza.getCodigoEtiqueta(), Element.ALIGN_LEFT, fondo));
                tablaPiezas.addCell(celdaDato(Formatos.legible(pieza.getTipo().name()), Element.ALIGN_LEFT, fondo));
                tablaPiezas.addCell(celdaDato(Formatos.peso(pieza.getPesoKg()), Element.ALIGN_RIGHT, fondo));
                tablaPiezas.addCell(celdaDato(dinero(pieza.getCostoAdicional()), Element.ALIGN_RIGHT, fondo));
            }
            documento.add(tablaPiezas);
        } else if (tiquetes.isEmpty()) {
            documento.add(parrafo("La reserva no tiene tiquetes.", fuente(9, Font.ITALIC, TEXTO_SUAVE)));
        } else {
            PdfPTable concepto = new PdfPTable(new float[] {16, 26, 34, 24});
            concepto.setWidthPercentage(100);
            concepto.setHeaderRows(1);
            for (String titulo : new String[] {"Tiquete", "Pasajero", "Viaje", "Salida"}) {
                concepto.addCell(celdaEncabezado(titulo, Element.ALIGN_LEFT));
            }
            int fila = 0;
            for (Tiquete tiquete : tiquetes) {
                Color fondo = fila++ % 2 == 1 ? FONDO : Color.WHITE;
                Viaje viaje = tiquete.getTarifa().getViaje();
                concepto.addCell(celdaDato(tiquete.getNumeroTiquete(), Element.ALIGN_LEFT, fondo));
                concepto.addCell(celdaDato(nombre(tiquete.getPasajero()), Element.ALIGN_LEFT, fondo));
                concepto.addCell(celdaDato(viaje.getCodigoViaje() + "  " + trayecto(viaje.getRuta()),
                        Element.ALIGN_LEFT, fondo));
                concepto.addCell(celdaDato(FECHA_HORA.format(viaje.getFechaHoraSalida()), Element.ALIGN_LEFT, fondo));
            }
            documento.add(concepto);
        }

        documento.add(parrafo(empresaNombre + "  ·  NIT " + empresaNit + "  ·  " + empresaDireccion,
                fuente(8.5f, Font.NORMAL, TEXTO_SUAVE), 22));
        documento.add(parrafo("Tel. " + empresaTelefono + "  ·  " + empresaEmail,
                fuente(8.5f, Font.NORMAL, TEXTO_SUAVE), 2));
        documento.close();
        return salida.toByteArray();
    }

    private PdfPCell celdaEmpresa() {
        PdfPCell celda = celdaVacia();
        celda.setPaddingRight(16);
        celda.addElement(parrafo(empresaNombre, fuente(15, Font.BOLD, AZUL_OSCURO)));
        celda.addElement(parrafo("NIT " + empresaNit, fuente(9, Font.NORMAL, TEXTO), 6));
        celda.addElement(parrafo(empresaDireccion, fuente(9, Font.NORMAL, TEXTO_SUAVE), 2));
        celda.addElement(parrafo("Tel. " + empresaTelefono, fuente(9, Font.NORMAL, TEXTO_SUAVE), 2));
        celda.addElement(parrafo(empresaEmail, fuente(9, Font.NORMAL, TEXTO_SUAVE), 2));
        return celda;
    }

    private PdfPCell recuadro(String titulo, List<String[]> lineas) {
        PdfPCell celda = celdaVacia();
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(LINEA);
        celda.setPadding(12);
        celda.setPaddingTop(8);
        celda.addElement(parrafo(titulo, fuente(8, Font.BOLD, AZUL)));
        for (String[] linea : lineas) {
            if (linea[0] == null) {
                celda.addElement(parrafo(linea[1], fuente(10.5f, Font.BOLD, TEXTO), 4));
            } else {
                Paragraph parrafo = new Paragraph();
                parrafo.setSpacingBefore(3);
                parrafo.add(new Chunk(linea[0] + ": ", fuente(8.5f, Font.NORMAL, TEXTO_SUAVE)));
                parrafo.add(new Chunk(linea[1], fuente(8.5f, Font.NORMAL, TEXTO)));
                celda.addElement(parrafo);
            }
        }
        return celda;
    }

    private void filaTotal(PdfPTable tabla, String etiqueta, String valor, boolean destacado) {
        Font fuenteEtiqueta = destacado ? fuente(11, Font.BOLD, Color.WHITE) : fuente(9, Font.NORMAL, TEXTO_SUAVE);
        Font fuenteValor = destacado ? fuente(11, Font.BOLD, Color.WHITE) : fuente(9, Font.NORMAL, TEXTO);
        PdfPCell izquierda = new PdfPCell(new Phrase(etiqueta, fuenteEtiqueta));
        PdfPCell derecha = new PdfPCell(new Phrase(valor, fuenteValor));
        derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        for (PdfPCell celda : new PdfPCell[] {izquierda, derecha}) {
            celda.setBorder(destacado ? Rectangle.NO_BORDER : Rectangle.BOTTOM);
            celda.setBorderColor(LINEA);
            celda.setBorderWidth(0.5f);
            celda.setPaddingTop(destacado ? 7 : 5);
            celda.setPaddingBottom(destacado ? 9 : 6);
            celda.setPaddingLeft(8);
            celda.setPaddingRight(8);
            if (destacado) {
                celda.setBackgroundColor(AZUL_OSCURO);
            }
            tabla.addCell(celda);
        }
    }

    private void filaDato(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell izquierda = new PdfPCell(new Phrase(etiqueta, fuente(9, Font.NORMAL, TEXTO_SUAVE)));
        PdfPCell derecha = new PdfPCell(new Phrase(valor, fuente(9, Font.BOLD, TEXTO)));
        for (PdfPCell celda : new PdfPCell[] {izquierda, derecha}) {
            celda.setBorder(Rectangle.BOTTOM);
            celda.setBorderColor(LINEA);
            celda.setBorderWidth(0.5f);
            celda.setPaddingTop(5);
            celda.setPaddingBottom(7);
            celda.setPaddingLeft(0);
            tabla.addCell(celda);
        }
    }

    private PdfPCell celdaEncabezado(String texto, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente(8.5f, Font.BOLD, Color.WHITE)));
        celda.setBackgroundColor(AZUL_OSCURO);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPaddingTop(6);
        celda.setPaddingBottom(7);
        celda.setPaddingLeft(5);
        celda.setPaddingRight(5);
        return celda;
    }

    private PdfPCell celdaDato(String texto, int alineacion, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente(8.5f, Font.NORMAL, TEXTO)));
        estiloDato(celda, fondo);
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }

    private PdfPCell celdaDosLineas(String principal, String secundaria, Color fondo) {
        Phrase frase = new Phrase();
        frase.add(new Chunk(principal, fuente(8.5f, Font.NORMAL, TEXTO)));
        frase.add(Chunk.NEWLINE);
        frase.add(new Chunk(secundaria, fuente(7.5f, Font.NORMAL, TEXTO_SUAVE)));
        PdfPCell celda = new PdfPCell(frase);
        estiloDato(celda, fondo);
        return celda;
    }

    private void estiloDato(PdfPCell celda, Color fondo) {
        celda.setBackgroundColor(fondo);
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(LINEA);
        celda.setBorderWidth(0.5f);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setUseAscender(true);
        celda.setPaddingTop(4.5f);
        celda.setPaddingBottom(5.5f);
        celda.setPaddingLeft(5);
        celda.setPaddingRight(5);
    }

    private PdfPCell celdaVacia() {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPadding(0);
        return celda;
    }

    private Paragraph subtitulo(String texto) {
        Paragraph parrafo = parrafo(texto, fuente(10.5f, Font.BOLD, TEXTO), 18);
        parrafo.setSpacingAfter(7);
        return parrafo;
    }

    private Paragraph parrafo(String texto, Font fuente) {
        return parrafo(texto, fuente, 0);
    }

    private Paragraph parrafo(String texto, Font fuente, float espacioAntes) {
        return parrafo(texto, fuente, espacioAntes, Element.ALIGN_LEFT);
    }

    private Paragraph parrafo(String texto, Font fuente, float espacioAntes, int alineacion) {
        Paragraph parrafo = new Paragraph(texto, fuente);
        parrafo.setSpacingBefore(espacioAntes);
        parrafo.setLeading(fuente.getSize() * 1.3f);
        parrafo.setAlignment(alineacion);
        return parrafo;
    }

    private Font fuente(float tamano, int estilo, Color color) {
        return new Font(Font.HELVETICA, tamano, estilo, color);
    }

    private String nombre(Usuario usuario) {
        return unir(usuario.getPrimerNombre(), usuario.getSegundoNombre(), usuario.getPrimerApellido(),
                usuario.getSegundoApellido());
    }

    private String nombre(Pasajero pasajero) {
        return unir(pasajero.getPrimerNombre(), pasajero.getSegundoNombre(), pasajero.getPrimerApellido(),
                pasajero.getSegundoApellido());
    }

    private String nombre(String codigo) {
        if (codigo == null) {
            return "-";
        }
        String conocido = NOMBRES.get(codigo);
        if (conocido != null) {
            return conocido;
        }
        String texto = codigo.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private String unir(String... partes) {
        StringBuilder texto = new StringBuilder();
        for (String parte : partes) {
            if (parte != null && !parte.isBlank()) {
                texto.append(texto.isEmpty() ? "" : " ").append(parte.trim());
            }
        }
        return texto.toString();
    }

    private String direccion(Usuario usuario) {
        String ciudad = usuario.getCiudad() == null ? null : usuario.getCiudad().getNombre();
        String calle = usuario.getDireccion();
        if (calle == null || calle.isBlank()) {
            return ciudad == null ? "-" : ciudad;
        }
        return ciudad == null ? calle : calle + ", " + ciudad;
    }

    private String trayecto(Ruta ruta) {
        return ruta.getTerminalOrigen().getCiudad().getNombre() + " - " + ruta.getTerminalDestino().getCiudad().getNombre();
    }

    private String silla(Asiento asiento) {
        return asiento == null ? "Sin silla asignada" : "Silla " + asiento.getNumeroAsiento();
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    private String dinero(BigDecimal valor) {
        return "$ " + formato("#,##0").format(valor == null ? BigDecimal.ZERO : valor);
    }

    private String porcentaje(BigDecimal valor) {
        return formato("#,##0.##").format(valor) + " %";
    }

    private static DecimalFormat formato(String patron) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return new DecimalFormat(patron, simbolos);
    }

    private String enLetras(BigDecimal valor) {
        long pesos = valor.setScale(0, RoundingMode.HALF_UP).longValue();
        if (pesos == 0) {
            return "CERO PESOS M/CTE";
        }
        String letras = apocope(numeroEnLetras(pesos));
        boolean millonesExactos = pesos % 1_000_000 == 0;
        return letras + (millonesExactos ? " DE PESOS" : pesos == 1 ? " PESO" : " PESOS") + " M/CTE";
    }

    private String numeroEnLetras(long numero) {
        long millones = numero / 1_000_000;
        int miles = (int) (numero / 1000 % 1000);
        int resto = (int) (numero % 1000);
        StringBuilder texto = new StringBuilder();
        if (millones > 0) {
            texto.append(millones == 1 ? "UN MILLÓN" : apocope(numeroEnLetras(millones)) + " MILLONES").append(' ');
        }
        if (miles > 0) {
            texto.append(miles == 1 ? "MIL" : apocope(menorQueMil(miles)) + " MIL").append(' ');
        }
        if (resto > 0) {
            texto.append(menorQueMil(resto));
        }
        return texto.toString().trim();
    }

    private String menorQueMil(int numero) {
        if (numero == 100) {
            return "CIEN";
        }
        int centenas = numero / 100;
        int resto = numero % 100;
        String decenas = resto < 30 ? UNIDADES[resto]
                : DECENAS[resto / 10] + (resto % 10 > 0 ? " Y " + UNIDADES[resto % 10] : "");
        return (CENTENAS[centenas] + " " + decenas).trim();
    }

    private String apocope(String texto) {
        if (texto.endsWith("VEINTIUNO")) {
            return texto.substring(0, texto.length() - 9) + "VEINTIÚN";
        }
        if (texto.endsWith("UNO")) {
            return texto.substring(0, texto.length() - 3) + "UN";
        }
        return texto;
    }

    private static class PiePagina extends PdfPageEventHelper {

        private final String texto;
        private final Font fuente = new Font(Font.HELVETICA, 8, Font.NORMAL, TEXTO_SUAVE);
        private PdfTemplate total;

        PiePagina(String texto) {
            this.texto = texto;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document documento) {
            total = writer.getDirectContent().createTemplate(24, 12);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            PdfContentByte contenido = writer.getDirectContent();
            float y = documento.bottom() - 24;
            contenido.saveState();
            contenido.setColorStroke(LINEA);
            contenido.setLineWidth(0.5f);
            contenido.moveTo(documento.left(), y + 12);
            contenido.lineTo(documento.right(), y + 12);
            contenido.stroke();
            contenido.restoreState();
            ColumnText.showTextAligned(contenido, Element.ALIGN_LEFT, new Phrase(texto, fuente),
                    documento.left(), y, 0);
            ColumnText.showTextAligned(contenido, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber() + " de", fuente), documento.right() - 13, y, 0);
            contenido.addTemplate(total, documento.right() - 10, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document documento) {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber() - 1), fuente), 0, 0, 0);
        }
    }
}
