package com.reservasvuelos.reservas_backend.servicios;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.FacturaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.MetodoPagoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PagoRepositorio;
import com.reservasvuelos.reservas_backend.dto.PagoCompraDTO;
import com.reservasvuelos.reservas_backend.model.EstadoPago;
import com.reservasvuelos.reservas_backend.model.Factura;
import com.reservasvuelos.reservas_backend.model.MetodoPago;
import com.reservasvuelos.reservas_backend.model.Pago;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.enums.TipoMetodoPago;

@Service
public class CobroServicio {

    private static final String CARACTERES_REFERENCIA = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private final PagoRepositorio pagoRepositorio;
    private final MetodoPagoRepositorio metodoPagoRepositorio;
    private final FacturaRepositorio facturaRepositorio;
    private final Catalogos catalogos;
    private final NotificadorServicio notificadorServicio;
    private final SecureRandom aleatorio = new SecureRandom();

    public CobroServicio(PagoRepositorio pagoRepositorio, MetodoPagoRepositorio metodoPagoRepositorio,
            FacturaRepositorio facturaRepositorio, Catalogos catalogos, NotificadorServicio notificadorServicio) {
        this.pagoRepositorio = pagoRepositorio;
        this.metodoPagoRepositorio = metodoPagoRepositorio;
        this.facturaRepositorio = facturaRepositorio;
        this.catalogos = catalogos;
        this.notificadorServicio = notificadorServicio;
    }

    public void cobrar(Reserva reserva, PagoCompraDTO datos, boolean personal) {
        MetodoPago metodo = metodoPagoRepositorio.findById(datos.codMetodo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe el método de pago " + datos.codMetodo()));
        boolean credito = metodo.getTipo() == TipoMetodoPago.TARJETA_CREDITO;
        if (metodo.getTipo() == TipoMetodoPago.EFECTIVO && !personal) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago en efectivo solo se recibe en taquilla");
        }
        if (!credito && datos.cuotas() != null && datos.cuotas() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las cuotas solo aplican a pagos con tarjeta de crédito");
        }

        EstadoPago rechazado = catalogos.estadoPago("RECHAZADO");
        pagoRepositorio.findByReserva_CodReservaOrderByFechaPago(reserva.getCodReserva()).stream()
                .filter(anterior -> anterior.getEstado().getNombre().equals("PENDIENTE"))
                .forEach(anterior -> anterior.setEstado(rechazado));

        if (reserva.getTotal().signum() > 0) {
            Pago pago = new Pago();
            pago.setFechaPago(LocalDateTime.now());
            pago.setMonto(reserva.getTotal());
            pago.setCuotas(credito ? (datos.cuotas() == null ? 1 : datos.cuotas()) : null);
            pago.setReferenciaTransaccion(referencia(metodo, datos.referenciaTransaccion()));
            pago.setEstado(catalogos.estadoPago("APROBADO"));
            pago.setReserva(reserva);
            pago.setMetodoPago(metodo);
            pagoRepositorio.save(pago);
        }

        reserva.setEstado(catalogos.estadoReserva("PAGADA"));
        notificadorServicio.pagoConfirmado(reserva, facturar(reserva));
    }

    private Factura facturar(Reserva reserva) {
        return facturaRepositorio.findById(reserva.getCodReserva()).orElseGet(() -> {
            Factura factura = new Factura();
            factura.setReserva(reserva);
            factura.setNumeroFactura(String.format("FE-%06d", facturaRepositorio.ultimoConsecutivo() + 1));
            factura.setFechaEmision(LocalDateTime.now());
            return facturaRepositorio.save(factura);
        });
    }

    private String referencia(MetodoPago metodo, String enviada) {
        if (metodo.getTipo() == TipoMetodoPago.EFECTIVO) {
            return null;
        }
        if (enviada != null && !enviada.isBlank()) {
            String referencia = enviada.trim();
            if (pagoRepositorio.existsByReferenciaTransaccion(referencia)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La referencia de transacción " + referencia + " ya fue usada en otro pago");
            }
            return referencia;
        }
        String referencia;
        do {
            StringBuilder texto = new StringBuilder("TX");
            for (int i = 0; i < 12; i++) {
                texto.append(CARACTERES_REFERENCIA.charAt(aleatorio.nextInt(CARACTERES_REFERENCIA.length())));
            }
            referencia = texto.toString();
        } while (pagoRepositorio.existsByReferenciaTransaccion(referencia));
        return referencia;
    }
}
