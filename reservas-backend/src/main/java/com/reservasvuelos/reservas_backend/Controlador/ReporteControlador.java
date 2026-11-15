package com.reservasvuelos.reservas_backend.Controlador;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.dto.ReporteDTO;
import com.reservasvuelos.reservas_backend.dto.ReporteInfoDTO;
import com.reservasvuelos.reservas_backend.servicios.ReportePdfServicio;
import com.reservasvuelos.reservas_backend.servicios.ReporteServicio;

@RestController
@RequestMapping("/reportes")
public class ReporteControlador {

    private final ReporteServicio reporteServicio;
    private final ReportePdfServicio reportePdfServicio;

    public ReporteControlador(ReporteServicio reporteServicio, ReportePdfServicio reportePdfServicio) {
        this.reporteServicio = reporteServicio;
        this.reportePdfServicio = reportePdfServicio;
    }

    @GetMapping
    public List<ReporteInfoDTO> listar() {
        return reporteServicio.listar();
    }

    @GetMapping("/{numero}")
    public ReporteDTO ver(@PathVariable("numero") int numero) {
        return reporteServicio.ejecutar(numero);
    }

    @GetMapping("/{numero}/pdf")
    public ResponseEntity<byte[]> descargar(@PathVariable("numero") int numero, Authentication autenticacion) {
        ReporteDTO reporte = reporteServicio.ejecutar(numero);
        byte[] pdf = reportePdfServicio.generar(reporte, autenticacion.getName());
        ContentDisposition adjunto = ContentDisposition.attachment()
                .filename(reportePdfServicio.nombreArchivo(reporte))
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, adjunto.toString())
                .body(pdf);
    }
}
