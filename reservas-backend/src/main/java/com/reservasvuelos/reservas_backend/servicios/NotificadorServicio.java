package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.ClienteNotificacionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.NotificacionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PromocionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TiqueteRepositorio;
import com.reservasvuelos.reservas_backend.dto.EnvioPromocionDTO;
import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.model.ClienteNotificacion;
import com.reservasvuelos.reservas_backend.model.EstadoNotificacion;
import com.reservasvuelos.reservas_backend.model.Factura;
import com.reservasvuelos.reservas_backend.model.Notificacion;
import com.reservasvuelos.reservas_backend.model.Promocion;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.Viaje;
import com.reservasvuelos.reservas_backend.servicios.AvisoCreado.Adjunto;

@Service
public class NotificadorServicio {

    public static final String RECORDATORIO_VIAJE = "RECORDATORIO_VIAJE";
    public static final String PROMOCION = "PROMOCION";
    public static final String CAMBIO_VIAJE = "CAMBIO_VIAJE";
    public static final String CONFIRMACION_PAGO = "CONFIRMACION_PAGO";
    public static final String RESERVA_PENDIENTE = "RESERVA_PENDIENTE";
    public static final String RESERVA_CANCELADA = "RESERVA_CANCELADA";
    public static final String RESERVA_VENCIDA = "RESERVA_VENCIDA";
    public static final String CHECKIN = "CHECKIN";
    public static final List<String> TIPOS = List.of(RECORDATORIO_VIAJE, PROMOCION, CAMBIO_VIAJE, CONFIRMACION_PAGO,
            RESERVA_PENDIENTE, RESERVA_CANCELADA, RESERVA_VENCIDA, CHECKIN);
    public static final List<String> ESTADOS = List.of("PENDIENTE", "ENVIADA", "LEIDA", "FALLIDA");

    private static final int HORAS_RECORDATORIO_POR_DEFECTO = 24;
    private static final int MAXIMO_HORAS_RECORDATORIO = 72;
    private static final int MINUTOS_DE_ESPERA = 5;
    private static final Logger LOG = LoggerFactory.getLogger(NotificadorServicio.class);

    private final NotificacionRepositorio notificacionRepositorio;
    private final ClienteNotificacionRepositorio clienteNotificacionRepositorio;
    private final TiqueteRepositorio tiqueteRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final PromocionRepositorio promocionRepositorio;
    private final Catalogos catalogos;
    private final ApplicationEventPublisher eventos;

    public NotificadorServicio(NotificacionRepositorio notificacionRepositorio,
            ClienteNotificacionRepositorio clienteNotificacionRepositorio, TiqueteRepositorio tiqueteRepositorio,
            ClienteRepositorio clienteRepositorio, PromocionRepositorio promocionRepositorio, Catalogos catalogos,
            ApplicationEventPublisher eventos) {
        this.notificacionRepositorio = notificacionRepositorio;
        this.clienteNotificacionRepositorio = clienteNotificacionRepositorio;
        this.tiqueteRepositorio = tiqueteRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.promocionRepositorio = promocionRepositorio;
        this.catalogos = catalogos;
        this.eventos = eventos;
    }

    public void pagoConfirmado(Reserva reserva, Factura factura) {
        Viaje viaje = viaje(reserva);
        String mensaje = "Recibimos el pago de " + Formatos.dinero(reserva.getTotal()) + " de tu reserva "
                + reserva.getCodigoReserva() + ". Tu viaje " + descripcion(viaje) + " sale el "
                + Formatos.cuando(viaje.getFechaHoraSalida()) + "."
                + (factura == null ? "" : " Te adjuntamos la factura " + factura.getNumeroFactura() + ".");
        crear(CONFIRMACION_PAGO, "Pago confirmado de la reserva " + reserva.getCodigoReserva(), mensaje, viaje, null,
                List.of(reserva.getCliente()), reserva.getCodReserva(),
                factura == null ? Adjunto.NINGUNO : Adjunto.FACTURA);
    }

    public void reservaPendiente(Reserva reserva) {
        Viaje viaje = viaje(reserva);
        int sillas = tiqueteRepositorio.deReserva(reserva.getCodReserva()).size();
        String mensaje = "Apartamos " + sillas + (sillas == 1 ? " cupo" : " cupos") + " en el viaje "
                + descripcion(viaje) + " del " + Formatos.cuando(viaje.getFechaHoraSalida()) + ". El total es "
                + Formatos.dinero(reserva.getTotal()) + ". Págala antes del "
                + Formatos.cuando(reserva.getFechaLimitePago()) + " o la reserva se libera.";
        crear(RESERVA_PENDIENTE, "Reserva " + reserva.getCodigoReserva() + " pendiente de pago", mensaje, viaje, null,
                List.of(reserva.getCliente()), reserva.getCodReserva(), Adjunto.NINGUNO);
    }

    public void reservaCancelada(Reserva reserva, boolean reembolso) {
        Viaje viaje = viaje(reserva);
        String mensaje = "Cancelamos tu reserva " + reserva.getCodigoReserva() + " del viaje " + descripcion(viaje)
                + " del " + Formatos.cuando(viaje.getFechaHoraSalida()) + "."
                + (reembolso ? " Te devolvemos " + Formatos.dinero(reserva.getTotal()) + " al mismo medio de pago."
                        : " Como la reserva no estaba pagada, no se hizo ningún cobro.");
        crear(RESERVA_CANCELADA, "Reserva " + reserva.getCodigoReserva() + " cancelada", mensaje, viaje, null,
                List.of(reserva.getCliente()), reserva.getCodReserva(), Adjunto.NINGUNO);
    }

    public void reservaVencida(Reserva reserva) {
        Viaje viaje = viaje(reserva);
        String mensaje = "Tu reserva " + reserva.getCodigoReserva() + " del viaje " + descripcion(viaje)
                + " venció porque no se pagó antes del " + Formatos.cuando(reserva.getFechaLimitePago())
                + ". Los cupos quedaron libres. Si todavía quieres viajar, haz una nueva compra.";
        crear(RESERVA_VENCIDA, "Reserva " + reserva.getCodigoReserva() + " vencida", mensaje, viaje, null,
                List.of(reserva.getCliente()), reserva.getCodReserva(), Adjunto.NINGUNO);
    }

    public void checkinRealizado(Reserva reserva, Viaje viaje, List<Tiquete> tiquetes) {
        String pasajeros = tiquetes.stream()
                .map(tiquete -> tiquete.getPasajero().getPrimerNombre() + " " + tiquete.getPasajero().getPrimerApellido()
                        + (tiquete.getAsiento() == null ? "" : " (silla " + tiquete.getAsiento().getNumeroAsiento() + ")"))
                .collect(Collectors.joining(", "));
        String lugar = viaje.getPuertaAnden() == null ? "" : " por " + lugar(viaje.getPuertaAnden());
        String mensaje = "Hiciste el check-in de " + pasajeros + " en el viaje " + descripcion(viaje) + " del "
                + Formatos.cuando(viaje.getFechaHoraSalida()) + ". El abordaje empieza a las "
                + Formatos.hora(ReglasAbordaje.inicioAbordaje(viaje)) + lugar + " y se cierra a las "
                + Formatos.hora(ReglasAbordaje.cierrePuerta(viaje)) + ". "
                + (tiquetes.size() == 1 ? "Te adjuntamos el pase de abordar." : "Te adjuntamos los pases de abordar.");
        crear(CHECKIN, "Check-in listo para el viaje " + viaje.getCodigoViaje(), mensaje, viaje, null,
                List.of(reserva.getCliente()), reserva.getCodReserva(), Adjunto.PASES_ABORDAR);
    }

    public void cambioViaje(Viaje viaje, String estadoAnterior, LocalDateTime salidaAnterior, String puertaAnterior) {
        String estado = viaje.getEstado().getNombre();
        boolean cancelado = estado.equals("CANCELADO") && !estado.equals(estadoAnterior);
        if (!cancelado && !CompraServicio.ESTADOS_VENTA.contains(estado)) {
            return;
        }
        List<String> cambios = new ArrayList<>();
        if (estado.equals("RETRASADO") && !estado.equals(estadoAnterior)) {
            cambios.add("El viaje quedó retrasado.");
        }
        if (!viaje.getFechaHoraSalida().equals(salidaAnterior)) {
            cambios.add("La nueva salida es el " + Formatos.cuando(viaje.getFechaHoraSalida()) + " (antes era el "
                    + Formatos.cuando(salidaAnterior) + ").");
        }
        if (viaje.getPuertaAnden() != null && !Objects.equals(viaje.getPuertaAnden(), puertaAnterior)) {
            cambios.add("Ahora sale por " + lugar(viaje.getPuertaAnden()) + ".");
        }
        if (!cancelado && cambios.isEmpty()) {
            return;
        }
        List<Cliente> clientes = clienteRepositorio.conTiquetesEnViaje(viaje.getCodViaje());
        if (clientes.isEmpty()) {
            return;
        }
        if (cancelado) {
            crear(CAMBIO_VIAJE, "Viaje " + viaje.getCodigoViaje() + " cancelado", "La empresa canceló el viaje "
                    + descripcion(viaje) + " del " + Formatos.cuando(viaje.getFechaHoraSalida())
                    + ". Puedes cancelar tu reserva desde Mis reservas y te devolvemos el valor pagado.", viaje, null,
                    clientes, null, Adjunto.NINGUNO);
        } else {
            crear(CAMBIO_VIAJE, "Cambio en tu viaje " + viaje.getCodigoViaje(), "Hay cambios en tu viaje "
                    + descripcion(viaje) + ". " + String.join(" ", cambios), viaje, null, clientes, null, Adjunto.NINGUNO);
        }
    }

    @Scheduled(initialDelayString = "${app.recordatorios.retraso-inicial-ms:60000}",
            fixedDelayString = "${app.recordatorios.intervalo-ms:600000}")
    @Transactional
    public void revisarAvisos() {
        LocalDateTime ahora = LocalDateTime.now();
        notificacionRepositorio.programadasPorEnviar(ahora.minusDays(1), ahora.minusMinutes(MINUTOS_DE_ESPERA))
                .forEach(notificacion -> eventos.publishEvent(
                        new AvisoCreado(notificacion.getCodNotificacion(), null, Adjunto.NINGUNO)));
        Map<String, List<Tiquete>> grupos = tiqueteRepositorio
                .pagadosPorSalir(CompraServicio.ESTADOS_VENTA, ahora, ahora.plusHours(MAXIMO_HORAS_RECORDATORIO))
                .stream()
                .collect(Collectors.groupingBy(tiquete -> tiquete.getReserva().getCliente().getCodCliente() + "-"
                        + tiquete.getTarifa().getViaje().getCodViaje(), LinkedHashMap::new, Collectors.toList()));
        int creados = 0;
        for (List<Tiquete> tiquetes : grupos.values()) {
            Tiquete primero = tiquetes.get(0);
            Cliente cliente = primero.getReserva().getCliente();
            Viaje viaje = primero.getTarifa().getViaje();
            int horas = cliente.getHorasRecordatorio() == null ? HORAS_RECORDATORIO_POR_DEFECTO
                    : Math.min(cliente.getHorasRecordatorio(), MAXIMO_HORAS_RECORDATORIO);
            if (!Boolean.TRUE.equals(cliente.getRecibeNotificaciones())
                    || viaje.getFechaHoraSalida().isAfter(ahora.plusHours(horas))
                    || clienteNotificacionRepositorio.yaNotificado(cliente.getCodCliente(), viaje.getCodViaje(),
                            RECORDATORIO_VIAJE)) {
                continue;
            }
            boolean terrestre = "TERRESTRE"
                    .equalsIgnoreCase(viaje.getRuta().getTerminalOrigen().getTipoTransporte().getNombre());
            String pasajeros = tiquetes.stream()
                    .map(tiquete -> tiquete.getPasajero().getPrimerNombre() + " "
                            + tiquete.getPasajero().getPrimerApellido()
                            + (tiquete.getAsiento() == null ? "" : " (silla " + tiquete.getAsiento().getNumeroAsiento() + ")"))
                    .collect(Collectors.joining(", "));
            String mensaje = "Tu viaje " + descripcion(viaje) + " sale el " + Formatos.cuando(viaje.getFechaHoraSalida())
                    + " desde " + viaje.getRuta().getTerminalOrigen().getNombre()
                    + (viaje.getPuertaAnden() == null ? "" : ", " + viaje.getPuertaAnden()) + ". Pasajeros: " + pasajeros
                    + ". Llega con " + (terrestre ? "30 minutos" : "2 horas")
                    + " de anticipación y lleva tu documento de identidad.";
            crear(RECORDATORIO_VIAJE, "Tu viaje " + viaje.getCodigoViaje() + " sale pronto", mensaje, viaje, null,
                    List.of(cliente), primero.getReserva().getCodReserva(), Adjunto.NINGUNO);
            creados++;
        }
        if (creados > 0) {
            LOG.info("Se crearon {} recordatorios de viaje", creados);
        }
    }

    @Transactional
    public EnvioPromocionDTO promocion(Long codPromocion) {
        Promocion promocion = promocionRepositorio.buscarPorId(codPromocion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe la promoción " + codPromocion));
        if (promocion.getFechaFin().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La promoción " + promocion.getCodigo()
                    + " terminó el " + Formatos.fecha(promocion.getFechaFin()));
        }
        List<Cliente> clientes = clienteRepositorio.conNotificaciones();
        if (clientes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ningún cliente acepta recibir promociones");
        }
        String porcentaje = Formatos.porcentaje(promocion.getPorcentajeDescuento());
        String descripcion = promocion.getDescripcion() == null || promocion.getDescripcion().isBlank() ? ""
                : promocion.getDescripcion().trim() + ". ";
        String mensaje = descripcion + "Usa el código " + promocion.getCodigo() + " al comprar tus tiquetes y recibe "
                + porcentaje + " de descuento. Válida del " + Formatos.fecha(promocion.getFechaInicio()) + " al "
                + Formatos.fecha(promocion.getFechaFin()) + ".";
        Notificacion notificacion = crear(PROMOCION, "Promoción " + promocion.getCodigo() + ": " + porcentaje
                + " de descuento", mensaje, null, promocion, clientes, null, Adjunto.NINGUNO);
        return new EnvioPromocionDTO(notificacion.getCodNotificacion(), promocion.getCodigo(), clientes.size());
    }

    private Notificacion crear(String tipo, String titulo, String mensaje, Viaje viaje, Promocion promocion,
            Collection<Cliente> clientes, Long codReserva, Adjunto adjunto) {
        Notificacion notificacion = new Notificacion();
        notificacion.setTitulo(Formatos.recortar(titulo, 80));
        notificacion.setMensaje(Formatos.recortar(mensaje, 500));
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setTipoNotificacion(catalogos.tipoNotificacion(tipo));
        notificacion.setViaje(viaje);
        notificacion.setPromocion(promocion);
        notificacionRepositorio.insertar(notificacion);

        EstadoNotificacion pendiente = catalogos.estadoNotificacion("PENDIENTE");
        List<ClienteNotificacion> destinatarios = new ArrayList<>();
        for (Cliente cliente : clientes) {
            ClienteNotificacion destinatario = new ClienteNotificacion();
            destinatario.setCliente(cliente);
            destinatario.setNotificacion(notificacion);
            destinatario.setEstadoNotificacion(pendiente);
            destinatarios.add(destinatario);
        }
        clienteNotificacionRepositorio.insertarTodos(destinatarios);
        eventos.publishEvent(new AvisoCreado(notificacion.getCodNotificacion(), codReserva, adjunto));
        return notificacion;
    }

    private Viaje viaje(Reserva reserva) {
        return tiqueteRepositorio.deReserva(reserva.getCodReserva()).get(0)
                .getTarifa().getViaje();
    }

    private static String lugar(String puertaAnden) {
        String texto = puertaAnden.trim();
        String minuscula = texto.toLowerCase(Locale.ROOT);
        if (minuscula.startsWith("puerta")) {
            return "la " + minuscula;
        }
        if (minuscula.startsWith("andén") || minuscula.startsWith("anden")) {
            return "el " + minuscula;
        }
        return texto;
    }

    private static String descripcion(Viaje viaje) {
        return viaje.getCodigoViaje() + " de " + viaje.getRuta().getTerminalOrigen().getCiudad().getNombre() + " a "
                + viaje.getRuta().getTerminalDestino().getCiudad().getNombre();
    }
}
