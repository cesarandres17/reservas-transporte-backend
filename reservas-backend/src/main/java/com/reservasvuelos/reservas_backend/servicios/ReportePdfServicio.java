package com.reservasvuelos.reservas_backend.servicios;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
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
import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.dto.ReporteDTO;
import com.reservasvuelos.reservas_backend.servicios.DefinicionReporte.Grafico;

@Service
public class ReportePdfServicio {

    private static final Color AZUL_OSCURO = new Color(0x1F, 0x3A, 0x5F);
    private static final Color AZUL = new Color(0x2A, 0x78, 0xD6);
    private static final Color AZUL_CLARO = new Color(0xB7, 0xD3, 0xF6);
    private static final Color TEXTO = new Color(0x1F, 0x29, 0x33);
    private static final Color TEXTO_SUAVE = new Color(0x5B, 0x65, 0x73);
    private static final Color LINEA = new Color(0xD9, 0xDE, 0xE5);
    private static final Color FILA_ALTERNA = new Color(0xF4, 0xF7, 0xFB);
    private static final Color FILA_TOTAL = new Color(0xE6, 0xEE, 0xF8);
    private static final Color FONDO_SQL = new Color(0xF6, 0xF7, 0xF9);
    private static final Color[] SERIES = {
        new Color(0x2A, 0x78, 0xD6), new Color(0xEB, 0x68, 0x34), new Color(0x1B, 0xAF, 0x7A),
        new Color(0xED, 0xA1, 0x00), new Color(0xE8, 0x7B, 0xA4), new Color(0x00, 0x83, 0x00),
        new Color(0x62, 0x50, 0xD6), new Color(0xE3, 0x49, 0x48)
    };
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final java.awt.Font FUENTE_GRAFICO = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8);

    public byte[] generar(ReporteDTO reporte, String usuario) {
        float[] anchos = anchos(reporte);
        float suma = 0;
        for (float ancho : anchos) {
            suma += ancho;
        }
        Rectangle tamano = suma > 105 ? PageSize.A4.rotate() : PageSize.A4;
        Document documento = new Document(tamano, 36, 36, 36, 54);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(documento, salida);
        writer.setPageEvent(new PiePagina("Reporte " + reporte.numero() + ": " + reporte.titulo()));
        documento.addTitle(reporte.titulo());
        documento.addAuthor("Reservas de Transporte");
        documento.open();
        encabezado(documento, reporte, usuario);
        if (reporte.grafico() != null && !reporte.filas().isEmpty()) {
            grafico(documento, writer, reporte);
        }
        tabla(documento, reporte, anchos);
        consulta(documento, reporte);
        documento.close();
        return salida.toByteArray();
    }

    public String nombreArchivo(ReporteDTO reporte) {
        String titulo = Normalizer.normalize(reporte.titulo(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return String.format("reporte-%02d-%s.pdf", reporte.numero(), titulo);
    }

    private void encabezado(Document documento, ReporteDTO reporte, String usuario) {
        PdfPTable banda = new PdfPTable(1);
        banda.setWidthPercentage(100);
        PdfPCell celda = new PdfPCell();
        celda.setBackgroundColor(AZUL_OSCURO);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPaddingTop(12);
        celda.setPaddingBottom(18);
        celda.setPaddingLeft(16);
        celda.setPaddingRight(16);
        celda.addElement(new Paragraph("RESERVAS DE TRANSPORTE", new Font(Font.HELVETICA, 8.5f, Font.BOLD, AZUL_CLARO)));
        Paragraph titulo = new Paragraph(reporte.titulo(), new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE));
        titulo.setSpacingBefore(4);
        titulo.setLeading(22);
        celda.addElement(titulo);
        banda.addCell(celda);
        documento.add(banda);

        String etiqueta = "REPORTE " + reporte.numero() + "  ·  " + reporte.tipo().toUpperCase(Locale.ROOT)
                + (reporte.grafico() != null ? "  ·  CON GRÁFICO" : "");
        Paragraph tipo = new Paragraph(etiqueta, new Font(Font.HELVETICA, 8.5f, Font.BOLD, AZUL));
        tipo.setSpacingBefore(12);
        documento.add(tipo);

        Paragraph descripcion = new Paragraph(reporte.descripcion(), new Font(Font.HELVETICA, 10, Font.NORMAL, TEXTO));
        descripcion.setSpacingBefore(4);
        descripcion.setLeading(14);
        documento.add(descripcion);

        int registros = reporte.filas().size();
        String meta = "Generado el " + FECHA_HORA.format(reporte.generado()) + " por " + usuario + "  ·  "
                + formato("#,##0").format(registros) + (registros == 1 ? " registro" : " registros");
        Paragraph datos = new Paragraph(meta, new Font(Font.HELVETICA, 8.5f, Font.NORMAL, TEXTO_SUAVE));
        datos.setSpacingBefore(4);
        datos.setSpacingAfter(14);
        documento.add(datos);
    }

    private void grafico(Document documento, PdfWriter writer, ReporteDTO reporte) {
        Grafico tipo = Grafico.valueOf(reporte.grafico());
        boolean millones = tipo != Grafico.TORTA && reporte.columnaValor().contains("($)") && maximo(reporte) >= 1_000_000;
        JFreeChart grafico = switch (tipo) {
            case BARRAS -> barras(reporte, PlotOrientation.VERTICAL, millones);
            case BARRAS_HORIZONTALES -> barras(reporte, PlotOrientation.HORIZONTAL, millones);
            case TORTA -> torta(reporte);
        };
        float ancho = documento.right() - documento.left();
        float alto = tipo == Grafico.BARRAS_HORIZONTALES
                ? Math.min(30 + reporte.filas().size() * 17, 400)
                : 240;

        String valor = reporte.columnaValor().replace("($)", "").replace("(%)", "").trim();
        String titulo = valor + " por " + reporte.columnaEtiqueta().toLowerCase(Locale.ROOT)
                + (tipo == Grafico.TORTA ? " (porcentaje del total)" : millones ? " (millones de pesos)" : "");
        Paragraph leyenda = new Paragraph(titulo, new Font(Font.HELVETICA, 10, Font.BOLD, TEXTO));
        leyenda.setSpacingAfter(6);
        documento.add(leyenda);

        PdfTemplate plantilla = writer.getDirectContent().createTemplate(ancho, alto);
        Graphics2D g2 = plantilla.createGraphicsShapes(ancho, alto);
        grafico.draw(g2, new Rectangle2D.Double(0, 0, ancho, alto));
        g2.dispose();
        Image imagen = Image.getInstance(plantilla);
        imagen.setSpacingAfter(14);
        documento.add(imagen);
    }

    private JFreeChart barras(ReporteDTO reporte, PlotOrientation orientacion, boolean millones) {
        DefaultCategoryDataset datos = new DefaultCategoryDataset();
        for (Map<String, Object> fila : reporte.filas()) {
            double valor = numero(fila.get(reporte.columnaValor()));
            datos.addValue(millones ? valor / 1_000_000 : valor, reporte.columnaValor(),
                    String.valueOf(fila.get(reporte.columnaEtiqueta())));
        }
        JFreeChart grafico = ChartFactory.createBarChart(null, null, null, datos, orientacion, false, false, false);
        estilo(grafico);
        DecimalFormat formatoValor = formato(millones ? "#,##0.0" : "#,##0");

        CategoryPlot plot = grafico.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(LINEA);
        plot.setRangeGridlineStroke(new BasicStroke(0.6f));
        plot.setAxisOffset(RectangleInsets.ZERO_INSETS);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, AZUL);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(orientacion == PlotOrientation.VERTICAL ? 0.05 : 0.6);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", formatoValor));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(FUENTE_GRAFICO);
        renderer.setDefaultItemLabelPaint(TEXTO);
        renderer.setItemLabelAnchorOffset(3);
        if (orientacion == PlotOrientation.VERTICAL) {
            renderer.setDefaultPositiveItemLabelPosition(
                    new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
        } else {
            renderer.setDefaultPositiveItemLabelPosition(
                    new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT));
        }

        CategoryAxis ejeCategorias = plot.getDomainAxis();
        ejeCategorias.setTickLabelFont(FUENTE_GRAFICO);
        ejeCategorias.setTickLabelPaint(TEXTO_SUAVE);
        ejeCategorias.setAxisLinePaint(LINEA);
        ejeCategorias.setTickMarksVisible(false);
        ejeCategorias.setCategoryMargin(orientacion == PlotOrientation.VERTICAL ? 0.35 : 0.3);
        ejeCategorias.setLowerMargin(0.01);
        ejeCategorias.setUpperMargin(0.01);
        ejeCategorias.setMaximumCategoryLabelWidthRatio(1.8f);

        NumberAxis ejeValores = (NumberAxis) plot.getRangeAxis();
        ejeValores.setNumberFormatOverride(formato("#,##0"));
        ejeValores.setTickLabelFont(FUENTE_GRAFICO);
        ejeValores.setTickLabelPaint(TEXTO_SUAVE);
        ejeValores.setAxisLineVisible(false);
        ejeValores.setTickMarksVisible(false);
        ejeValores.setUpperMargin(0.12);
        return grafico;
    }

    private JFreeChart torta(ReporteDTO reporte) {
        Map<String, Double> valores = new LinkedHashMap<>();
        List<Map<String, Object>> filas = reporte.filas();
        for (int i = 0; i < filas.size(); i++) {
            String etiqueta = i < SERIES.length - 1 || filas.size() == SERIES.length
                    ? String.valueOf(filas.get(i).get(reporte.columnaEtiqueta()))
                    : "Otros";
            valores.merge(etiqueta, numero(filas.get(i).get(reporte.columnaValor())), Double::sum);
        }
        DefaultPieDataset<String> datos = new DefaultPieDataset<>();
        valores.forEach(datos::setValue);

        JFreeChart grafico = ChartFactory.createPieChart(null, datos, true, false, false);
        estilo(grafico);
        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) grafico.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setInteriorGap(0.06);
        plot.setSectionOutlinesVisible(true);
        plot.setDefaultSectionOutlinePaint(Color.WHITE);
        plot.setDefaultSectionOutlineStroke(new BasicStroke(2f));
        int indice = 0;
        for (String clave : datos.getKeys()) {
            plot.setSectionPaint(clave, SERIES[indice++]);
        }
        DecimalFormat porcentaje = formato("#,##0.0 %");
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}", formato("#,##0"), porcentaje));
        plot.setLabelFont(FUENTE_GRAFICO);
        plot.setLabelPaint(TEXTO);
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelLinkPaint(TEXTO_SUAVE);
        plot.setMaximumLabelWidth(0.3);

        LegendTitle leyenda = grafico.getLegend();
        leyenda.setPosition(RectangleEdge.RIGHT);
        leyenda.setItemFont(FUENTE_GRAFICO);
        leyenda.setItemPaint(TEXTO);
        leyenda.setFrame(BlockBorder.NONE);
        leyenda.setBackgroundPaint(Color.WHITE);
        return grafico;
    }

    private void estilo(JFreeChart grafico) {
        grafico.setBackgroundPaint(Color.WHITE);
        grafico.setBorderVisible(false);
        grafico.setAntiAlias(true);
        grafico.setTextAntiAlias(true);
        grafico.setPadding(new RectangleInsets(2, 2, 2, 2));
    }

    private void tabla(Document documento, ReporteDTO reporte, float[] anchos) {
        if (reporte.filas().isEmpty()) {
            documento.add(new Paragraph("No hay registros para mostrar en este reporte.",
                    new Font(Font.HELVETICA, 10, Font.ITALIC, TEXTO_SUAVE)));
            return;
        }
        List<String> columnas = reporte.columnas();
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        Font fuenteEncabezado = new Font(Font.HELVETICA, 8.5f, Font.BOLD, Color.WHITE);
        Font fuenteDato = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, TEXTO);
        Font fuenteTotal = new Font(Font.HELVETICA, 8.5f, Font.BOLD, AZUL_OSCURO);

        int[] alineaciones = new int[columnas.size()];
        for (int c = 0; c < columnas.size(); c++) {
            alineaciones[c] = alineacion(primerValor(reporte, columnas.get(c)));
            PdfPCell celda = celda(columnas.get(c), fuenteEncabezado, alineaciones[c]);
            celda.setBackgroundColor(AZUL_OSCURO);
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setPaddingTop(6);
            celda.setPaddingBottom(7);
            tabla.addCell(celda);
        }

        int numeroFila = 0;
        for (Map<String, Object> fila : reporte.filas()) {
            Color fondo = numeroFila++ % 2 == 1 ? FILA_ALTERNA : Color.WHITE;
            for (int c = 0; c < columnas.size(); c++) {
                PdfPCell celda = celda(formatear(columnas.get(c), fila.get(columnas.get(c))), fuenteDato, alineaciones[c]);
                celda.setBackgroundColor(fondo);
                celda.setBorder(Rectangle.BOTTOM);
                celda.setBorderColor(LINEA);
                celda.setBorderWidth(0.5f);
                tabla.addCell(celda);
            }
        }

        if (!reporte.columnasTotal().isEmpty()) {
            for (int c = 0; c < columnas.size(); c++) {
                String columna = columnas.get(c);
                String texto = c == 0 ? "Total"
                        : reporte.columnasTotal().contains(columna) ? formatear(columna, suma(reporte, columna)) : "";
                PdfPCell celda = celda(texto, fuenteTotal, c == 0 ? Element.ALIGN_LEFT : alineaciones[c]);
                celda.setBackgroundColor(FILA_TOTAL);
                celda.setBorder(Rectangle.TOP);
                celda.setBorderColor(AZUL_OSCURO);
                celda.setBorderWidth(1f);
                tabla.addCell(celda);
            }
        }
        documento.add(tabla);
    }

    private void consulta(Document documento, ReporteDTO reporte) {
        PdfPTable marco = new PdfPTable(1);
        marco.setWidthPercentage(100);
        marco.setSpacingBefore(18);
        marco.setKeepTogether(true);
        PdfPCell encabezado = new PdfPCell(new Phrase("Consulta SQL del reporte", new Font(Font.HELVETICA, 10, Font.BOLD, TEXTO)));
        encabezado.setBorder(Rectangle.NO_BORDER);
        encabezado.setPaddingLeft(0);
        encabezado.setPaddingBottom(8);
        marco.addCell(encabezado);
        Paragraph codigo = new Paragraph(reporte.sql(), new Font(Font.COURIER, 7.5f, Font.NORMAL, TEXTO));
        codigo.setLeading(10);
        PdfPCell celda = new PdfPCell();
        celda.addElement(codigo);
        celda.setBackgroundColor(FONDO_SQL);
        celda.setBorder(Rectangle.LEFT);
        celda.setBorderColor(AZUL);
        celda.setBorderWidth(2f);
        celda.setPaddingTop(4);
        celda.setPaddingBottom(10);
        celda.setPaddingLeft(10);
        celda.setPaddingRight(10);
        marco.addCell(celda);
        documento.add(marco);
    }

    private PdfPCell celda(String texto, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setUseAscender(true);
        celda.setPaddingTop(4.5f);
        celda.setPaddingBottom(5.5f);
        celda.setPaddingLeft(5);
        celda.setPaddingRight(5);
        return celda;
    }

    private float[] anchos(ReporteDTO reporte) {
        List<String> columnas = reporte.columnas();
        float[] anchos = new float[columnas.size()];
        for (int c = 0; c < columnas.size(); c++) {
            String columna = columnas.get(c);
            int largo = Math.max(6, Math.round(columna.length() * 0.85f));
            for (Map<String, Object> fila : reporte.filas()) {
                largo = Math.max(largo, formatear(columna, fila.get(columna)).length());
            }
            anchos[c] = Math.min(largo, 42) + 2;
        }
        return anchos;
    }

    private String formatear(String columna, Object valor) {
        if (valor == null) {
            return "-";
        }
        if (valor instanceof LocalDateTime fechaHora) {
            return FECHA_HORA.format(fechaHora);
        }
        if (valor instanceof LocalDate fecha) {
            return FECHA.format(fecha);
        }
        if (valor instanceof Boolean logico) {
            return logico ? "Sí" : "No";
        }
        if (valor instanceof Number numero) {
            if (columna.equals("Año")) {
                return String.valueOf(numero.intValue());
            }
            if (columna.contains("($)")) {
                return "$ " + formato("#,##0").format(numero);
            }
            if (columna.contains("(%)")) {
                return formato("#,##0.0").format(numero);
            }
            if (numero instanceof BigDecimal decimal && decimal.stripTrailingZeros().scale() > 0) {
                return formato("#,##0.0").format(numero);
            }
            return formato("#,##0").format(numero);
        }
        return String.valueOf(valor);
    }

    private int alineacion(Object valor) {
        if (valor instanceof Number) {
            return Element.ALIGN_RIGHT;
        }
        if (valor instanceof LocalDate || valor instanceof LocalDateTime) {
            return Element.ALIGN_CENTER;
        }
        return Element.ALIGN_LEFT;
    }

    private Object primerValor(ReporteDTO reporte, String columna) {
        return reporte.filas().stream()
                .map(fila -> fila.get(columna))
                .filter(valor -> valor != null)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal suma(ReporteDTO reporte, String columna) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> fila : reporte.filas()) {
            if (fila.get(columna) instanceof Number numero) {
                total = total.add(new BigDecimal(numero.toString()));
            }
        }
        return total;
    }

    private double maximo(ReporteDTO reporte) {
        return reporte.filas().stream()
                .mapToDouble(fila -> numero(fila.get(reporte.columnaValor())))
                .max()
                .orElse(0);
    }

    private double numero(Object valor) {
        return valor instanceof Number numero ? numero.doubleValue() : 0;
    }

    private static DecimalFormat formato(String patron) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return new DecimalFormat(patron, simbolos);
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
            ColumnText.showTextAligned(contenido, Element.ALIGN_LEFT,
                    new Phrase("Reservas de Transporte  ·  " + texto, fuente), documento.left(), y, 0);
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
