package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Pago;
import com.reservasvuelos.reservas_backend.servicios.DocumentoServicio;
import com.reservasvuelos.reservas_backend.servicios.PagoServicio;

@RestController
@RequestMapping("/pagos")
public class PagoControlador extends CrudControlador<Pago, Long> {

    private final DocumentoServicio documentoServicio;

    public PagoControlador(PagoServicio pagoServicio, DocumentoServicio documentoServicio) {
        super(pagoServicio);
        this.documentoServicio = documentoServicio;
    }

    @GetMapping("/{id}/comprobante")
    public ResponseEntity<byte[]> comprobante(@PathVariable("id") Long id, Authentication autenticacion) {
        return RespuestaPdf.de(documentoServicio.comprobante(id, autenticacion));
    }
}
