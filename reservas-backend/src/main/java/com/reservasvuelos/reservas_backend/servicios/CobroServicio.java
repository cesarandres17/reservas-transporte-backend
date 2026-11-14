package com.reservasvuelos.reservas_backend.servicios;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

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
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.enums.TipoMetodoPago;

@Service
public class CobroServicio {

    public static final String PREFIJO_EQUIPAJE = "EQP-";

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
        MetodoPago metodo = metodo(datos.codMetodo(), datos.cuotas(), personal);

        EstadoPago rechazado = catalogos.estadoPago("RECHAZADO");
        pagoRepositorio.findByReserva_CodReservaOrderByFechaPago(reserva.getCodReserva()).stream()
                .filter(anterior -> anterior.getEstado().getNombre().equals("PENDIENTE"))
                .forEach(anterior -> anterior.setEstado(rechazado));

        if (reserva.getTotal().signum() > 0) {
            guardar(reserva, reserva.getTotal(), metodo, datos.cuotas(),
                    referencia(metodo, datos.referenciaTransaccion()));
        }

        reserva.setEstado(catalogos.estadoReserva("PAGADA"));
        notificadorServicio.pagoConfirmado(reserva, facturar(reserva));
    }

    public Pago cobrarEquipaje(Tiquete tiquete, int desde, int hasta, BigDecimal monto, Long codMetodo, Integer cuotas,
            boolean personal) {
        MetodoPago metodo = metodo(codMetodo, cuotas, personal);
        return guardar(tiquete.getReserva(), monto, metodo, cuotas,
                generada(PREFIJO_EQUIPAJE + tiquete.getCodTiquete() + "-" + desde + "-" + hasta + "-", 6));
    }

    public static Optional<PiezasPagadas> piezasPagadas(Pago pago) {
        String referencia = pago.getReferenciaTransaccion();
        if (referencia == null || !referencia.startsWith(PREFIJO_EQUIPAJE)) {
            return Optional.empty();
        }
        String[] partes = referencia.substring(PREFIJO_EQUIPAJE.length()).split("-");
        if (partes.length != 4) {
            return Optional.empty();
        }
        try {
            return Optional.of(new PiezasPagadas(Long.valueOf(partes[0]), Integer.parseInt(partes[1]),
                    Integer.parseInt(partes[2])));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public record PiezasPagadas(Long codTiquete, int desde, int hasta) {

        public boolean incluye(int numeroPieza) {
            return numeroPieza >= desde && numeroPieza <= hasta;
        }
    }

    private MetodoPago metodo(Long codMetodo, Integer cuotas, boolean personal) {
        MetodoPago metodo = metodoPagoRepositorio.findById(codMetodo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe el método de pago " + codMetodo));
        if (metodo.getTipo() == TipoMetodoPago.EFECTIVO && !personal) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago en efectivo solo se recibe en taquilla");
        }
        if (metodo.getTipo() != TipoMetodoPago.TARJETA_CREDITO && cuotas != null && cuotas > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las cuotas solo aplican a pagos con tarjeta de crédito");
        }
        return metodo;
    }

    private Pago guardar(Reserva reserva, BigDecimal monto, MetodoPago metodo, Integer cuotas, String referencia) {
        boolean credito = metodo.getTipo() == TipoMetodoPago.TARJETA_CREDITO;
        Pago pago = new Pago();
        pago.setFechaPago(LocalDateTime.now());
        pago.setMonto(monto);
        pago.setCuotas(credito ? (cuotas == null ? 1 : cuotas) : null);
        pago.setReferenciaTransaccion(referencia);
        pago.setEstado(catalogos.estadoPago("APROBADO"));
        pago.setReserva(reserva);
        pago.setMetodoPago(metodo);
        return pagoRepositorio.save(pago);
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
            if (referencia.toUpperCase(Locale.ROOT).startsWith(PREFIJO_EQUIPAJE)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Las referencias que empiezan por " + PREFIJO_EQUIPAJE + " son de pagos de equipaje");
            }
            if (pagoRepositorio.existsByReferenciaTransaccion(referencia)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La referencia de transacción " + referencia + " ya fue usada en otro pago");
            }
            return referencia;
        }
        return generada("TX", 12);
    }

    private String generada(String prefijo, int largo) {
        String referencia;
        do {
            StringBuilder texto = new StringBuilder(prefijo);
            for (int i = 0; i < largo; i++) {
                texto.append(CARACTERES_REFERENCIA.charAt(aleatorio.nextInt(CARACTERES_REFERENCIA.length())));
            }
            referencia = texto.toString();
        } while (pagoRepositorio.existsByReferenciaTransaccion(referencia));
        return referencia;
    }
}
