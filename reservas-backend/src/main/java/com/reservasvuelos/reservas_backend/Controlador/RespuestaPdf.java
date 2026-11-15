package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.reservasvuelos.reservas_backend.dto.DocumentoPdf;

public final class RespuestaPdf {

    private RespuestaPdf() {
    }

    public static ResponseEntity<byte[]> de(DocumentoPdf documento) {
        ContentDisposition adjunto = ContentDisposition.attachment()
                .filename(documento.nombreArchivo())
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, adjunto.toString())
                .body(documento.contenido());
    }
}
