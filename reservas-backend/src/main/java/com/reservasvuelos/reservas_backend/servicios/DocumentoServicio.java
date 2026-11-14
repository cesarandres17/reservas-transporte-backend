package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.FacturaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PagoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TiqueteRepositorio;
import com.reservasvuelos.reservas_backend.dto.DocumentoPdf;
import com.reservasvuelos.reservas_backend.model.Factura;
import com.reservasvuelos.reservas_backend.model.Pago;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.Tiquete;

@Service
public class DocumentoServicio {

    private final FacturaRepositorio facturaRepositorio;
    private final PagoRepositorio pagoRepositorio;
    private final TiqueteRepositorio tiqueteRepositorio;
    private final DocumentoPdfServicio documentoPdfServicio;

    public DocumentoServicio(FacturaRepositorio facturaRepositorio, PagoRepositorio pagoRepositorio,
            TiqueteRepositorio tiqueteRepositorio, DocumentoPdfServicio documentoPdfServicio) {
        this.facturaRepositorio = facturaRepositorio;
        this.pagoRepositorio = pagoRepositorio;
        this.tiqueteRepositorio = tiqueteRepositorio;
        this.documentoPdfServicio = documentoPdfServicio;
    }

    @Transactional(readOnly = true)
    public DocumentoPdf factura(Long codReserva, Authentication autenticacion) {
        Factura factura = facturaRepositorio.findById(codReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe la factura de la reserva " + codReserva));
        validarAcceso(factura.getReserva(), autenticacion);
        List<Tiquete> tiquetes = tiqueteRepositorio.findByReserva_CodReservaOrderByCodTiquete(codReserva);
        List<Pago> pagos = pagoRepositorio.findByReserva_CodReservaOrderByFechaPago(codReserva);
        byte[] contenido = documentoPdfServicio.factura(factura, tiquetes, pagos);
        return new DocumentoPdf("factura-" + factura.getNumeroFactura() + ".pdf", contenido);
    }

    @Transactional(readOnly = true)
    public DocumentoPdf comprobante(Long codPago, Authentication autenticacion) {
        Pago pago = pagoRepositorio.findById(codPago)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el pago " + codPago));
        Reserva reserva = pago.getReserva();
        validarAcceso(reserva, autenticacion);
        List<Tiquete> tiquetes = tiqueteRepositorio.findByReserva_CodReservaOrderByCodTiquete(reserva.getCodReserva());
        Factura factura = facturaRepositorio.findById(reserva.getCodReserva()).orElse(null);
        byte[] contenido = documentoPdfServicio.comprobante(pago, tiquetes, factura);
        return new DocumentoPdf(String.format("comprobante-pago-%06d.pdf", codPago), contenido);
    }

    private void validarAcceso(Reserva reserva, Authentication autenticacion) {
        boolean personal = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> rol.equals("ROLE_" + AuthServicio.ROL_ADMINISTRADOR)
                        || rol.equals("ROLE_" + AuthServicio.ROL_EMPLEADO));
        String propietario = reserva.getCliente().getUsuario().getEmail();
        if (!personal && !propietario.equalsIgnoreCase(autenticacion.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo puedes ver los documentos de tus propias reservas");
        }
    }
}
