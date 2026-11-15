package com.reservasvuelos.reservas_backend.servicios;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.BarcodePDF417;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.reservasvuelos.reservas_backend.Repositorio.CheckinRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EquipajeRepositorio;
import com.reservasvuelos.reservas_backend.model.Checkin;
import com.reservasvuelos.reservas_backend.model.Equipaje;
import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.Terminal;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.Viaje;

@Component
public class PaseAbordarPdf {

    public static final List<String> ESTADOS_CON_PASE = List.of("EMITIDO", "USADO");

    private static final Color AZUL_OSCURO = new Color(0x1F, 0x3A, 0x5F);
    private static final Color AZUL = new Color(0x2A, 0x78, 0xD6);
    private static final Color AZUL_CLARO = new Color(0xB7, 0xD3, 0xF6);
    private static final Color TEXTO = new Color(0x1F, 0x29, 0x33);
    private static final Color TEXTO_SUAVE = new Color(0x5B, 0x65, 0x73);
    private static final Color LINEA = new Color(0xD9, 0xDE, 0xE5);
    private static final Color FONDO = new Color(0xF4, 0xF7, 0xFB);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_CODIGO = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CheckinRepositorio checkinRepositorio;
    private final EquipajeRepositorio equipajeRepositorio;
    private final String empresa;

    public PaseAbordarPdf(CheckinRepositorio checkinRepositorio, EquipajeRepositorio equipajeRepositorio,
            @Value("${app.empresa.nombre:Reservas de Transporte S.A.S.}") String empresa) {
        this.checkinRepositorio = checkinRepositorio;
        this.equipajeRepositorio = equipajeRepositorio;
        this.empresa = empresa;
    }

    public List<Checkin> vigentes(Long codReserva) {
        return checkinRepositorio.deReserva(codReserva).stream()
                .filter(checkin -> ESTADOS_CON_PASE.contains(checkin.getTiquete().getEstado().getNombre()))
                .toList();
    }

    public byte[] deReserva(Long codReserva) {
        List<Checkin> checkins = vigentes(codReserva);
        if (checkins.isEmpty()) {
            return null;
        }
        Map<Long, List<Equipaje>> equipajes = equipajeRepositorio.deReserva(codReserva).stream()
                .collect(Collectors.groupingBy(equipaje -> equipaje.getTiquete().getCodTiquete()));
        return generar(checkins, equipajes);
    }

    public byte[] deTiquete(Checkin checkin) {
        Long codTiquete = checkin.getTiquete().getCodTiquete();
        return generar(List.of(checkin),
                Map.of(codTiquete, equipajeRepositorio.deTiquete(codTiquete)));
    }

    private byte[] generar(List<Checkin> checkins, Map<Long, List<Equipaje>> equipajes) {
        Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter.getInstance(documento, salida);
        documento.addTitle(checkins.size() == 1 ? "Pase de abordar" : "Pases de abordar");
        documento.addAuthor(empresa);
        documento.open();
        for (int i = 0; i < checkins.size(); i++) {
            Checkin checkin = checkins.get(i);
            PdfPTable pase = pase(checkin, equipajes.getOrDefault(checkin.getTiquete().getCodTiquete(), List.of()));
            if (i % 2 == 1) {
                pase.setSpacingBefore(28);
            } else if (i > 0) {
                documento.newPage();
            }
            documento.add(pase);
        }
        documento.close();
        return salida.toByteArray();
    }

    private PdfPTable pase(Checkin checkin, List<Equipaje> equipajes) {
        Tiquete tiquete = checkin.getTiquete();
        Viaje viaje = tiquete.getTarifa().getViaje();
        Pasajero pasajero = tiquete.getPasajero();
        Terminal origen = viaje.getRuta().getTerminalOrigen();
        Terminal destino = viaje.getRuta().getTerminalDestino();
        boolean aereo = ReglasAbordaje.esAereo(viaje);

        PdfPTable tarjeta = new PdfPTable(new float[] {70, 30});
        tarjeta.setWidthPercentage(100);
        tarjeta.setKeepTogether(true);

        PdfPCell banda = celda(AZUL_OSCURO, 14);
        banda.addElement(parrafo(empresa.toUpperCase(Locale.ROOT), fuente(8, Font.BOLD, AZUL_CLARO), 0));
        banda.addElement(parrafo(aereo ? "Pase de abordar" : "Pase de abordaje", fuente(19, Font.BOLD, Color.WHITE), 3));
        tarjeta.addCell(banda);
        PdfPCell bandaDerecha = celda(AZUL_OSCURO, 14);
        bandaDerecha.addElement(parrafo((aereo ? "Vuelo " : "Bus ") + viaje.getCodigoViaje(),
                fuente(13, Font.BOLD, Color.WHITE), 0, Element.ALIGN_RIGHT));
        bandaDerecha.addElement(parrafo(Formatos.legible(tiquete.getTarifa().getClaseServicio().getNombre()),
                fuente(9, Font.NORMAL, AZUL_CLARO), 4, Element.ALIGN_RIGHT));
        tarjeta.addCell(bandaDerecha);

        PdfPCell cuerpo = celda(Color.WHITE, 16);
        cuerpo.setBorder(Rectangle.LEFT | Rectangle.BOTTOM);
        cuerpo.setBorderColor(LINEA);
        cuerpo.addElement(parrafo("PASAJERO", fuente(7.5f, Font.BOLD, AZUL), 0));
        cuerpo.addElement(parrafo(PasajeroCompraServicio.nombre(pasajero), fuente(15, Font.BOLD, TEXTO), 3));
        cuerpo.addElement(parrafo(pasajero.getTipoDocumento().getSigla() + " " + pasajero.getNumeroDocumento() + "  ·  "
                + Formatos.legible(pasajero.getTipoPasajero().name()), fuente(8.5f, Font.NORMAL, TEXTO_SUAVE), 2));

        PdfPTable ruta = new PdfPTable(new float[] {44, 12, 44});
        ruta.setWidthPercentage(100);
        ruta.setSpacingBefore(14);
        ruta.addCell(lugar(origen, Element.ALIGN_LEFT));
        PdfPCell flecha = celda(null, 0);
        flecha.setVerticalAlignment(Element.ALIGN_MIDDLE);
        flecha.addElement(parrafo("»", fuente(22, Font.BOLD, AZUL), 0, Element.ALIGN_CENTER));
        ruta.addCell(flecha);
        ruta.addCell(lugar(destino, Element.ALIGN_RIGHT));
        cuerpo.addElement(ruta);

        PdfPTable datos = new PdfPTable(4);
        datos.setWidthPercentage(100);
        datos.setSpacingBefore(14);
        dato(datos, "FECHA", FECHA.format(viaje.getFechaHoraSalida()));
        dato(datos, "SALIDA", HORA.format(viaje.getFechaHoraSalida()));
        dato(datos, "ABORDAJE", HORA.format(ReglasAbordaje.inicioAbordaje(viaje)));
        dato(datos, aereo ? "PUERTA" : "ANDÉN", viaje.getPuertaAnden() == null ? "Por definir" : sinPrefijo(viaje.getPuertaAnden()));
        dato(datos, "SILLA", tiquete.getAsiento() == null ? "-" : tiquete.getAsiento().getNumeroAsiento());
        dato(datos, "GRUPO", checkin.getGrupoAbordaje() == null ? "-" : checkin.getGrupoAbordaje());
        dato(datos, "CIERRE DE PUERTA", HORA.format(ReglasAbordaje.cierrePuerta(viaje)));
        dato(datos, "ESTADO DEL VIAJE", Formatos.legible(viaje.getEstado().getNombre()));
        cuerpo.addElement(datos);
        tarjeta.addCell(cuerpo);

        PdfPCell talon = celda(FONDO, 14);
        talon.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        talon.setBorderColor(LINEA);
        talon.setBorderWidthLeft(1.2f);
        talon.addElement(codigoBarras(checkin, tiquete, viaje, pasajero));
        talon.addElement(parrafo("PASE", fuente(7.5f, Font.BOLD, AZUL), 10));
        talon.addElement(parrafo(checkin.getCodigoPaseAbordar(), fuente(9.5f, Font.BOLD, TEXTO), 1));
        talon.addElement(parrafo("TIQUETE", fuente(7.5f, Font.BOLD, AZUL), 6));
        talon.addElement(parrafo(tiquete.getNumeroTiquete(), fuente(9.5f, Font.BOLD, TEXTO), 1));
        talon.addElement(parrafo("RESERVA", fuente(7.5f, Font.BOLD, AZUL), 6));
        talon.addElement(parrafo(tiquete.getReserva().getCodigoReserva(), fuente(9.5f, Font.BOLD, TEXTO), 1));
        talon.addElement(parrafo("CHECK-IN", fuente(7.5f, Font.BOLD, AZUL), 6));
        talon.addElement(parrafo(FECHA_HORA.format(checkin.getFechaHora()) + "  ·  "
                + Formatos.legible(checkin.getCanal().name()), fuente(8.5f, Font.NORMAL, TEXTO), 1));
        tarjeta.addCell(talon);

        PdfPCell pie = celda(Color.WHITE, 12);
        pie.setColspan(2);
        pie.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        pie.setBorderColor(LINEA);
        Paragraph equipaje = new Paragraph();
        equipaje.setLeading(11.5f);
        equipaje.add(new Chunk("Equipaje: ", fuente(8.5f, Font.BOLD, TEXTO)));
        equipaje.add(new Chunk(equipaje(equipajes, tiquete.getTarifa().getClaseServicio().getEquipajeIncluidoKg()),
                fuente(8.5f, Font.NORMAL, TEXTO)));
        pie.addElement(equipaje);
        pie.addElement(parrafo("Preséntate en " + (aereo ? "la puerta" : "el andén") + " a las "
                + HORA.format(ReglasAbordaje.inicioAbordaje(viaje)) + " con tu documento de identidad. "
                + (aereo ? "La puerta" : "El abordaje") + " se cierra a las " + HORA.format(ReglasAbordaje.cierrePuerta(viaje))
                + " y después no se permite el ingreso.", fuente(8.5f, Font.NORMAL, TEXTO_SUAVE), 5));
        tarjeta.addCell(pie);
        return tarjeta;
    }

    private PdfPCell lugar(Terminal terminal, int alineacion) {
        PdfPCell celda = celda(null, 0);
        celda.addElement(parrafo(terminal.getCiudad().getNombre(), fuente(20, Font.BOLD, AZUL_OSCURO), 0, alineacion));
        celda.addElement(parrafo(terminal.getNombre(), fuente(8, Font.NORMAL, TEXTO_SUAVE), 2, alineacion));
        return celda;
    }

    private void dato(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celda = celda(null, 0);
        celda.setPaddingBottom(10);
        celda.addElement(parrafo(etiqueta, fuente(7, Font.BOLD, AZUL), 0));
        celda.addElement(parrafo(valor, fuente(12.5f, Font.BOLD, TEXTO), 2));
        tabla.addCell(celda);
    }

    private Image codigoBarras(Checkin checkin, Tiquete tiquete, Viaje viaje, Pasajero pasajero) {
        String texto = String.join("|", checkin.getCodigoPaseAbordar(), tiquete.getReserva().getCodigoReserva(),
                viaje.getCodigoViaje(), FECHA_CODIGO.format(viaje.getFechaHoraSalida()),
                tiquete.getAsiento() == null ? "" : tiquete.getAsiento().getNumeroAsiento(),
                ascii(pasajero.getPrimerApellido() + "/" + pasajero.getPrimerNombre()));
        BarcodePDF417 codigo = new BarcodePDF417();
        codigo.setText(texto);
        codigo.setErrorLevel(2);
        Image imagen = codigo.getImage();
        imagen.scaleAbsolute(130, 52);
        imagen.setAlignment(Image.ALIGN_LEFT);
        return imagen;
    }

    private String equipaje(List<Equipaje> equipajes, BigDecimal incluidoKg) {
        if (equipajes.isEmpty()) {
            return incluidoKg.signum() > 0
                    ? "sin piezas registradas. Tu clase incluye una pieza de bodega de hasta " + Formatos.peso(incluidoKg) + "."
                    : "sin piezas registradas.";
        }
        return equipajes.stream()
                .map(pieza -> Formatos.legible(pieza.getTipo().name()).toLowerCase(Locale.ROOT) + " "
                        + pieza.getCodigoEtiqueta() + " (" + Formatos.peso(pieza.getPesoKg()) + ")")
                .collect(Collectors.joining(", ")) + ".";
    }

    private static String sinPrefijo(String puerta) {
        return puerta.replaceFirst("(?i)^(puerta|andén|anden)\\s+", "");
    }

    private static String ascii(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
    }

    private PdfPCell celda(Color fondo, float relleno) {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPadding(relleno);
        if (fondo != null) {
            celda.setBackgroundColor(fondo);
        }
        return celda;
    }

    private Paragraph parrafo(String texto, Font fuente, float espacioAntes) {
        return parrafo(texto, fuente, espacioAntes, Element.ALIGN_LEFT);
    }

    private Paragraph parrafo(String texto, Font fuente, float espacioAntes, int alineacion) {
        Paragraph parrafo = new Paragraph(new Phrase(texto, fuente));
        parrafo.setSpacingBefore(espacioAntes);
        parrafo.setLeading(fuente.getSize() * 1.25f);
        parrafo.setAlignment(alineacion);
        return parrafo;
    }

    private Font fuente(float tamano, int estilo, Color color) {
        return new Font(Font.HELVETICA, tamano, estilo, color);
    }
}
