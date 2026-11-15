package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.PaisRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PasajeroRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TipoDocumentoRepositorio;
import com.reservasvuelos.reservas_backend.dto.CompraDTO.PasajeroCompra;
import com.reservasvuelos.reservas_backend.model.Pais;
import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.TipoDocumento;
import com.reservasvuelos.reservas_backend.model.enums.TipoPasajero;

@Service
public class PasajeroCompraServicio {

    private static final String PAIS_POR_DEFECTO = "COL";

    private final PasajeroRepositorio pasajeroRepositorio;
    private final TipoDocumentoRepositorio tipoDocumentoRepositorio;
    private final PaisRepositorio paisRepositorio;

    public PasajeroCompraServicio(PasajeroRepositorio pasajeroRepositorio,
            TipoDocumentoRepositorio tipoDocumentoRepositorio, PaisRepositorio paisRepositorio) {
        this.pasajeroRepositorio = pasajeroRepositorio;
        this.tipoDocumentoRepositorio = tipoDocumentoRepositorio;
        this.paisRepositorio = paisRepositorio;
    }

    public Pasajero resolver(PasajeroCompra datos, int posicion, LocalDate fechaViaje) {
        if (datos.codPasajero() != null) {
            return pasajeroRepositorio.buscarPorId(datos.codPasajero())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "No existe el pasajero " + datos.codPasajero()));
        }
        if (vacio(datos.siglaTipoDocumento()) || vacio(datos.numeroDocumento())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pasajero " + posicion
                    + ": envía codPasajero, o siglaTipoDocumento y numeroDocumento");
        }
        TipoDocumento tipoDocumento = tipoDocumentoRepositorio.buscarPorSigla(datos.siglaTipoDocumento().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe el tipo de documento " + datos.siglaTipoDocumento()));
        String numeroDocumento = datos.numeroDocumento().trim();
        Optional<Pasajero> registrado = pasajeroRepositorio.buscarPorDocumento(tipoDocumento.getCodTipo(),
                numeroDocumento);
        if (registrado.isPresent()) {
            return registrado.get();
        }

        List<String> faltantes = new ArrayList<>();
        if (vacio(datos.primerNombre())) {
            faltantes.add("primerNombre");
        }
        if (vacio(datos.primerApellido())) {
            faltantes.add("primerApellido");
        }
        if (datos.fechaNacimiento() == null) {
            faltantes.add("fechaNacimiento");
        }
        if (!faltantes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Pasajero " + posicion + ": falta " + String.join(", ", faltantes));
        }

        String codigoPais = vacio(datos.codigoPais()) ? PAIS_POR_DEFECTO : datos.codigoPais().trim();
        Pais pais = paisRepositorio.buscarPorCodigoIso(codigoPais)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe el país con código " + codigoPais));

        Pasajero pasajero = new Pasajero();
        pasajero.setTipoDocumento(tipoDocumento);
        pasajero.setNumeroDocumento(numeroDocumento);
        pasajero.setPrimerNombre(datos.primerNombre().trim());
        pasajero.setSegundoNombre(limpio(datos.segundoNombre()));
        pasajero.setPrimerApellido(datos.primerApellido().trim());
        pasajero.setSegundoApellido(limpio(datos.segundoApellido()));
        pasajero.setFechaNacimiento(datos.fechaNacimiento());
        pasajero.setGenero(datos.genero());
        pasajero.setTipoPasajero(datos.tipoPasajero() != null ? datos.tipoPasajero()
                : segunEdad(datos.fechaNacimiento(), fechaViaje));
        pasajero.setPais(pais);
        return pasajeroRepositorio.insertar(pasajero);
    }

    public static TipoPasajero segunEdad(LocalDate fechaNacimiento, LocalDate fechaViaje) {
        int edad = Period.between(fechaNacimiento, fechaViaje).getYears();
        if (edad < 2) {
            return TipoPasajero.INFANTE;
        }
        return edad < 12 ? TipoPasajero.NINO : TipoPasajero.ADULTO;
    }

    public static String nombre(Pasajero pasajero) {
        return pasajero.getPrimerNombre() + " " + pasajero.getPrimerApellido();
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private static String limpio(String texto) {
        return vacio(texto) ? null : texto.trim();
    }
}
