package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import com.reservasvuelos.reservas_backend.Repositorio.ClienteNotificacionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.NotificacionRepositorio;
import com.reservasvuelos.reservas_backend.model.ClienteNotificacion;
import com.reservasvuelos.reservas_backend.model.EstadoNotificacion;
import com.reservasvuelos.reservas_backend.model.Notificacion;
import com.reservasvuelos.reservas_backend.servicios.PlantillaCorreo.ContenidoCorreo;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class CorreoServicio {

    private static final int DESTINATARIOS_POR_CORREO = 50;
    private static final String REMITENTE_POR_DEFECTO = "no-responder@reservas.com";
    private static final Logger LOG = LoggerFactory.getLogger(CorreoServicio.class);

    private final JavaMailSender enviador;
    private final TransactionTemplate transaccion;
    private final NotificacionRepositorio notificacionRepositorio;
    private final ClienteNotificacionRepositorio clienteNotificacionRepositorio;
    private final PlantillaCorreo plantilla;
    private final Catalogos catalogos;
    private final boolean habilitado;
    private final String remitente;
    private final String redirigirA;
    private final String empresa;
    private final AtomicBoolean avisoApagado = new AtomicBoolean();
    private final Set<Long> enEnvio = ConcurrentHashMap.newKeySet();

    public CorreoServicio(JavaMailSender enviador, TransactionTemplate transaccion,
            NotificacionRepositorio notificacionRepositorio,
            ClienteNotificacionRepositorio clienteNotificacionRepositorio, PlantillaCorreo plantilla,
            Catalogos catalogos,
            @Value("${app.correo.habilitado:false}") boolean habilitado,
            @Value("${app.correo.remitente:}") String remitente,
            @Value("${spring.mail.username:}") String usuarioCorreo,
            @Value("${app.correo.redirigir-a:}") String redirigirA,
            @Value("${app.empresa.nombre:Reservas de Transporte S.A.S.}") String empresa) {
        this.enviador = enviador;
        this.transaccion = transaccion;
        this.notificacionRepositorio = notificacionRepositorio;
        this.clienteNotificacionRepositorio = clienteNotificacionRepositorio;
        this.plantilla = plantilla;
        this.catalogos = catalogos;
        this.habilitado = habilitado;
        this.remitente = !remitente.isBlank() ? remitente.trim()
                : !usuarioCorreo.isBlank() ? usuarioCorreo.trim() : REMITENTE_POR_DEFECTO;
        this.redirigirA = redirigirA.trim();
        this.empresa = empresa;
    }

    private record Destinatario(Long codCliente, String nombre, String email) {
    }

    private record Envio(ContenidoCorreo contenido, List<Destinatario> destinatarios) {
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void enviar(AvisoCreado aviso) {
        if (!habilitado) {
            if (avisoApagado.compareAndSet(false, true)) {
                LOG.info("El envio de correos esta apagado (app.correo.habilitado=false): "
                        + "las notificaciones quedan solo en la bandeja de cada cliente");
            }
            return;
        }
        if (!enEnvio.add(aviso.codNotificacion())) {
            return;
        }
        try {
            Envio envio = transaccion.execute(estado -> preparar(aviso));
            if (envio == null) {
                return;
            }
            Map<Long, Boolean> resultado = new HashMap<>();
            for (List<Destinatario> lote : lotes(envio.destinatarios())) {
                boolean enviado = mandar(aviso.codNotificacion(), envio.contenido(), lote);
                lote.forEach(destinatario -> resultado.put(destinatario.codCliente(), enviado));
            }
            transaccion.executeWithoutResult(estado -> registrar(aviso.codNotificacion(), resultado));
        } finally {
            enEnvio.remove(aviso.codNotificacion());
        }
    }

    private Envio preparar(AvisoCreado aviso) {
        Notificacion notificacion = notificacionRepositorio.findById(aviso.codNotificacion()).orElse(null);
        if (notificacion == null) {
            return null;
        }
        List<Destinatario> destinatarios = clienteNotificacionRepositorio.destinatarios(aviso.codNotificacion())
                .stream()
                .filter(fila -> fila.getFechaEnvio() == null
                        && fila.getEstadoNotificacion().getNombre().equals("PENDIENTE"))
                .map(fila -> new Destinatario(fila.getCliente().getCodCliente(),
                        fila.getCliente().getUsuario().getPrimerNombre(), fila.getCliente().getUsuario().getEmail()))
                .filter(destinatario -> destinatario.email() != null && !destinatario.email().isBlank())
                .toList();
        if (destinatarios.isEmpty()) {
            return null;
        }
        String nombre = destinatarios.size() == 1 ? destinatarios.get(0).nombre() : null;
        String avisoPrueba = redirigirA.isEmpty() ? null : "Correo de prueba: en uso real iría a "
                + resumen(destinatarios) + ".";
        ContenidoCorreo contenido = plantilla.contenido(notificacion, aviso.codReserva(), aviso.adjunto(),
                nombre, avisoPrueba);
        return new Envio(contenido, destinatarios);
    }

    private List<List<Destinatario>> lotes(List<Destinatario> destinatarios) {
        if (!redirigirA.isEmpty() || destinatarios.size() == 1) {
            return List.of(destinatarios);
        }
        List<List<Destinatario>> lotes = new ArrayList<>();
        for (int i = 0; i < destinatarios.size(); i += DESTINATARIOS_POR_CORREO) {
            lotes.add(destinatarios.subList(i, Math.min(i + DESTINATARIOS_POR_CORREO, destinatarios.size())));
        }
        return lotes;
    }

    private boolean mandar(Long codNotificacion, ContenidoCorreo contenido, List<Destinatario> lote) {
        try {
            MimeMessage mensaje = enviador.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, true, "UTF-8");
            ayudante.setFrom(new InternetAddress(remitente, empresa, "UTF-8"));
            if (!redirigirA.isEmpty()) {
                ayudante.setTo(redirigirA);
                ayudante.setSubject("[Prueba] " + contenido.asunto());
            } else if (lote.size() == 1) {
                ayudante.setTo(lote.get(0).email());
                ayudante.setSubject(contenido.asunto());
            } else {
                ayudante.setTo(remitente);
                ayudante.setBcc(lote.stream().map(Destinatario::email).toArray(String[]::new));
                ayudante.setSubject(contenido.asunto());
            }
            ayudante.setText(contenido.texto(), contenido.html());
            if (contenido.adjunto() != null) {
                ayudante.addAttachment(contenido.nombreAdjunto(), new ByteArrayResource(contenido.adjunto()),
                        "application/pdf");
            }
            enviador.send(mensaje);
            return true;
        } catch (Exception error) {
            LOG.warn("No se pudo enviar el correo de la notificacion {}: {}", codNotificacion, error.getMessage());
            return false;
        }
    }

    private void registrar(Long codNotificacion, Map<Long, Boolean> resultado) {
        EstadoNotificacion enviada = catalogos.estadoNotificacion("ENVIADA");
        EstadoNotificacion fallida = catalogos.estadoNotificacion("FALLIDA");
        LocalDateTime ahora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        for (ClienteNotificacion fila : clienteNotificacionRepositorio.destinatarios(codNotificacion)) {
            Boolean enviado = resultado.get(fila.getCliente().getCodCliente());
            if (enviado == null) {
                continue;
            }
            if (fila.getFechaLectura() == null) {
                fila.setEstadoNotificacion(enviado ? enviada : fallida);
            }
            if (enviado) {
                fila.setFechaEnvio(ahora);
            }
        }
    }

    private static String resumen(List<Destinatario> destinatarios) {
        List<String> correos = destinatarios.stream().limit(3).map(Destinatario::email).toList();
        int resto = destinatarios.size() - correos.size();
        return String.join(", ", correos) + (resto > 0 ? " y " + resto + " más" : "");
    }
}
