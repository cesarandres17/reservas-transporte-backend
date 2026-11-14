package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Factura;
import com.reservasvuelos.reservas_backend.servicios.DocumentoServicio;
import com.reservasvuelos.reservas_backend.servicios.FacturaServicio;

@RestController
@RequestMapping("/facturas")
public class FacturaControlador extends CrudControlador<Factura, Long> {

    private final DocumentoServicio documentoServicio;

    public FacturaControlador(FacturaServicio facturaServicio, DocumentoServicio documentoServicio) {
        super(facturaServicio);
        this.documentoServicio = documentoServicio;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable("id") Long id, Authentication autenticacion) {
        return RespuestaPdf.de(documentoServicio.factura(id, autenticacion));
    }
}
