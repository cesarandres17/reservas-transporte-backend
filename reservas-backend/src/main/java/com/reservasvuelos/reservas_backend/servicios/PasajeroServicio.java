package com.reservasvuelos.reservas_backend.servicios;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.PaisRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PasajeroRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoDocumentoRepositorio;
import com.reservasvuelos.reservas_backend.model.Pais;
import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.TipoDocumento;

@Service
public class PasajeroServicio extends CrudServicio<Pasajero, Long> {

    private final TipoDocumentoRepositorio tipoDocumentoRepositorio;
    private final PaisRepositorio paisRepositorio;

    public PasajeroServicio(PasajeroRepositorio repositorio,
            TipoDocumentoRepositorio tipoDocumentoRepositorio,
            PaisRepositorio paisRepositorio) {
        super(repositorio, "el pasajero");
        this.tipoDocumentoRepositorio = tipoDocumentoRepositorio;
        this.paisRepositorio = paisRepositorio;
    }

    @Override
    protected void asignarId(Pasajero pasajero, Long id) {
        pasajero.setCodPasajero(id);
    }

    @Override
    protected void completar(Pasajero pasajero, Pasajero actual) {
        pasajero.setTipoDocumento(referencia(pasajero.getTipoDocumento(), TipoDocumento::getCodTipo,
                tipoDocumentoRepositorio, "el tipo de documento"));
        pasajero.setPais(referencia(pasajero.getPais(), Pais::getCodPais, paisRepositorio, "el país"));
    }
}
