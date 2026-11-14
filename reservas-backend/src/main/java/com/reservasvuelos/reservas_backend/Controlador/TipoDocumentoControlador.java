package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.TipoDocumento;
import com.reservasvuelos.reservas_backend.servicios.TipoDocumentoServicio;

@RestController
@RequestMapping("/tipos-documento")
public class TipoDocumentoControlador extends CrudControlador<TipoDocumento, Long> {

    public TipoDocumentoControlador(TipoDocumentoServicio tipoDocumentoServicio) {
        super(tipoDocumentoServicio);
    }
}
