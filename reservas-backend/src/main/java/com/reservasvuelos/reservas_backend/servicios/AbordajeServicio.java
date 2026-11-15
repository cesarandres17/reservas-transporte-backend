package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.AsientoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.CheckinRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EmpleadoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EquipajeRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ReservaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TiqueteRepositorio;
import com.reservasvuelos.reservas_backend.dto.CheckinReservaDTO;
import com.reservasvuelos.reservas_backend.dto.CheckinReservaDTO.EquipajeRegistrado;
import com.reservasvuelos.reservas_backend.dto.CheckinReservaDTO.PasajeroCheckin;
import com.reservasvuelos.reservas_backend.dto.CheckinSolicitudDTO;
import com.reservasvuelos.reservas_backend.dto.DocumentoPdf;
import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.model.Checkin;
import com.reservasvuelos.reservas_backend.model.ClaseServicio;
import com.reservasvuelos.reservas_backend.model.Empleado;
import com.reservasvuelos.reservas_backend.model.Equipaje;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.Viaje;
import com.reservasvuelos.reservas_backend.model.enums.CanalCheckin;
import com.reservasvuelos.reservas_backend.model.enums.TipoPasajero;

@Service
public class AbordajeServicio {

    private final ReservaRepositorio reservaRepositorio;
    private final TiqueteRepositorio tiqueteRepositorio;
    private final CheckinRepositorio checkinRepositorio;
    private final EquipajeRepositorio equipajeRepositorio;
    private final AsientoRepositorio asientoRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;
    private final UsuarioActual usuarioActual;
    private final NotificadorServicio notificadorServicio;
    private final PaseAbordarPdf paseAbordarPdf;

    public AbordajeServicio(ReservaRepositorio reservaRepositorio, TiqueteRepositorio tiqueteRepositorio,
            CheckinRepositorio checkinRepositorio, EquipajeRepositorio equipajeRepositorio,
            AsientoRepositorio asientoRepositorio, EmpleadoRepositorio empleadoRepositorio,
            UsuarioActual usuarioActual, NotificadorServicio notificadorServicio, PaseAbordarPdf paseAbordarPdf) {
        this.reservaRepositorio = reservaRepositorio;
        this.tiqueteRepositorio = tiqueteRepositorio;
        this.checkinRepositorio = checkinRepositorio;
        this.equipajeRepositorio = equipajeRepositorio;
        this.asientoRepositorio = asientoRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
        this.usuarioActual = usuarioActual;
        this.notificadorServicio = notificadorServicio;
        this.paseAbordarPdf = paseAbordarPdf;
    }

    @Transactional(readOnly = true)
    public CheckinReservaDTO consultar(Long codReserva, Authentication autenticacion) {
        return resumen(reservaConAcceso(codReserva, autenticacion));
    }

    @Transactional
    public CheckinReservaDTO hacerCheckin(Long codReserva, CheckinSolicitudDTO datos, Authentication autenticacion) {
        Reserva reserva = reservaConAcceso(codReserva, autenticacion);
        boolean personal = usuarioActual.esPersonal(autenticacion);
        CanalCheckin canal = canal(datos == null ? null : datos.canal(), personal);
        List<Tiquete> tiquetes = tiquetes(reserva);
        Viaje viaje = tiquetes.get(0).getTarifa().getViaje();
        String motivo = motivoCerrado(reserva, viaje, LocalDateTime.now());
        if (motivo != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, motivo);
        }

        Map<Long, Checkin> hechos = checkins(reserva);
        Map<Tiquete, String> seleccion = seleccion(reserva, tiquetes, hechos,
                datos == null ? null : datos.pasajeros());

        LocalDate fechaViaje = viaje.getFechaHoraSalida().toLocalDate();
        boolean conAdulto = tiquetes.stream()
                .filter(tiquete -> hechos.containsKey(tiquete.getCodTiquete()) || seleccion.containsKey(tiquete))
                .anyMatch(tiquete -> PasajeroCompraServicio.segunEdad(tiquete.getPasajero().getFechaNacimiento(),
                        fechaViaje) == TipoPasajero.ADULTO);
        if (!conAdulto) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los niños e infantes hacen el check-in junto con un adulto de la reserva");
        }

        asignarSillas(viaje, seleccion);

        Empleado empleado = personal
                ? empleadoRepositorio.buscarPorId(usuarioActual.obtener(autenticacion).getCodUsuario()).orElse(null)
                : null;
        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        boolean aereo = ReglasAbordaje.esAereo(viaje);
        List<Tiquete> registrados = new ArrayList<>(seleccion.keySet());
        for (Tiquete tiquete : registrados) {
            Asiento asiento = tiquete.getAsiento();
            tiqueteRepositorio.asignarAsiento(tiquete.getCodTiquete(), asiento.getVehiculo().getCodVehiculo(),
                    asiento.getNumeroAsiento());
            Checkin checkin = new Checkin();
            checkin.setTiquete(tiquete);
            checkin.setFechaHora(ahora);
            checkin.setCanal(canal);
            checkin.setCodigoPaseAbordar(String.format("PB%010d", tiquete.getCodTiquete()));
            checkin.setGrupoAbordaje(aereo ? grupo(tiquete) : null);
            checkin.setEmpleado(empleado);
            checkinRepositorio.insertar(checkin);
        }
        notificadorServicio.checkinRealizado(reserva, viaje, registrados);
        return resumen(reserva);
    }

    @Transactional(readOnly = true)
    public DocumentoPdf pasesDeReserva(Long codReserva, Authentication autenticacion) {
        Reserva reserva = reservaConAcceso(codReserva, autenticacion);
        byte[] contenido = paseAbordarPdf.deReserva(reserva.getCodReserva());
        if (contenido == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nadie de la reserva " + reserva.getCodigoReserva() + " tiene check-in todavía");
        }
        return new DocumentoPdf("pases-abordar-" + reserva.getCodigoReserva() + ".pdf", contenido);
    }

    @Transactional(readOnly = true)
    public DocumentoPdf paseDeTiquete(Long codTiquete, Authentication autenticacion) {
        Tiquete tiquete = tiqueteRepositorio.buscarPorId(codTiquete)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el tiquete " + codTiquete));
        validarAcceso(tiquete.getReserva(), autenticacion);
        Checkin checkin = checkinRepositorio.buscarPorId(codTiquete)
                .filter(encontrado -> PaseAbordarPdf.ESTADOS_CON_PASE.contains(tiquete.getEstado().getNombre()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "El pasajero "
                        + PasajeroCompraServicio.nombre(tiquete.getPasajero()) + " no tiene check-in en este viaje"));
        return new DocumentoPdf("pase-abordar-" + tiquete.getNumeroTiquete() + ".pdf", paseAbordarPdf.deTiquete(checkin));
    }

    static String motivoCerrado(Reserva reserva, Viaje viaje, LocalDateTime ahora) {
        String estadoReserva = reserva.getEstado().getNombre();
        if (!estadoReserva.equals("PAGADA")) {
            return "La reserva " + reserva.getCodigoReserva() + " está " + Formatos.legible(estadoReserva).toLowerCase(Locale.ROOT)
                    + " y solo las reservas pagadas pueden hacer check-in";
        }
        String estadoViaje = viaje.getEstado().getNombre();
        if (!CompraServicio.ESTADOS_VENTA.contains(estadoViaje)) {
            return "El viaje " + viaje.getCodigoViaje() + " está " + Formatos.legible(estadoViaje).toLowerCase(Locale.ROOT)
                    + " y no tiene check-in";
        }
        if (ahora.isBefore(ReglasAbordaje.abreCheckin(viaje))) {
            return "El check-in del viaje " + viaje.getCodigoViaje() + " abre el "
                    + Formatos.cuando(ReglasAbordaje.abreCheckin(viaje)) + ", "
                    + ReglasAbordaje.duracion(ReglasAbordaje.APERTURA_CHECKIN) + " antes de la salida";
        }
        if (ahora.isAfter(ReglasAbordaje.cierraCheckin(viaje))) {
            return "El check-in del viaje " + viaje.getCodigoViaje() + " cerró el "
                    + Formatos.cuando(ReglasAbordaje.cierraCheckin(viaje)) + ", "
                    + ReglasAbordaje.duracion(ReglasAbordaje.anticipacionCierre(viaje)) + " antes de la salida";
        }
        return null;
    }

    private Map<Tiquete, String> seleccion(Reserva reserva, List<Tiquete> tiquetes, Map<Long, Checkin> hechos,
            List<CheckinSolicitudDTO.PasajeroCheckin> pedidos) {
        Map<Tiquete, String> seleccion = new LinkedHashMap<>();
        if (pedidos == null || pedidos.isEmpty()) {
            tiquetes.stream()
                    .filter(tiquete -> tiquete.getEstado().getNombre().equals("EMITIDO"))
                    .filter(tiquete -> !hechos.containsKey(tiquete.getCodTiquete()))
                    .forEach(tiquete -> seleccion.put(tiquete, null));
            if (seleccion.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Todos los pasajeros de la reserva " + reserva.getCodigoReserva() + " ya tienen check-in");
            }
            return seleccion;
        }
        Map<Long, Tiquete> porCodigo = tiquetes.stream()
                .collect(Collectors.toMap(Tiquete::getCodTiquete, Function.identity()));
        for (CheckinSolicitudDTO.PasajeroCheckin pedido : pedidos) {
            Tiquete tiquete = porCodigo.get(pedido.codTiquete());
            if (tiquete == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tiquete " + pedido.codTiquete()
                        + " no es de la reserva " + reserva.getCodigoReserva());
            }
            String nombre = PasajeroCompraServicio.nombre(tiquete.getPasajero());
            if (seleccion.containsKey(tiquete)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pasajero " + nombre + " está repetido");
            }
            if (!tiquete.getEstado().getNombre().equals("EMITIDO")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El tiquete de " + nombre + " está "
                        + Formatos.legible(tiquete.getEstado().getNombre()).toLowerCase(Locale.ROOT));
            }
            if (hechos.containsKey(tiquete.getCodTiquete())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El pasajero " + nombre + " ya tiene check-in");
            }
            seleccion.put(tiquete, pedido.numeroAsiento());
        }
        return seleccion;
    }

    private void asignarSillas(Viaje viaje, Map<Tiquete, String> seleccion) {
        Map<String, Asiento> delVehiculo = new LinkedHashMap<>();
        asientoRepositorio.delVehiculo(viaje.getVehiculo().getCodVehiculo())
                .forEach(asiento -> delVehiculo.put(asiento.getNumeroAsiento().toUpperCase(Locale.ROOT), asiento));
        Set<String> tomados = new HashSet<>();
        tiqueteRepositorio.asientosOcupados(viaje.getCodViaje())
                .forEach(numero -> tomados.add(numero.toUpperCase(Locale.ROOT)));
        Set<String> ocupados = Set.copyOf(tomados);

        for (Map.Entry<Tiquete, String> entrada : seleccion.entrySet()) {
            Tiquete tiquete = entrada.getKey();
            String numero = entrada.getValue();
            if (numero == null || numero.isBlank()) {
                continue;
            }
            String clave = numero.trim().toUpperCase(Locale.ROOT);
            Asiento actual = tiquete.getAsiento();
            if (actual != null && actual.getNumeroAsiento().equalsIgnoreCase(clave)) {
                continue;
            }
            Asiento asiento = delVehiculo.get(clave);
            if (asiento == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El asiento " + clave + " no existe en el vehículo del viaje " + viaje.getCodigoViaje());
            }
            ClaseServicio clase = tiquete.getTarifa().getClaseServicio();
            if (!asiento.getClaseServicio().getCodClase().equals(clase.getCodClase())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El asiento " + clave + " es de clase "
                        + asiento.getClaseServicio().getNombre() + " y el tiquete es de clase " + clase.getNombre());
            }
            if (!tomados.add(clave)) {
                throw new ResponseStatusException(ocupados.contains(clave) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST,
                        ocupados.contains(clave) ? "El asiento " + clave + " ya está ocupado"
                                : "El asiento " + clave + " está repetido en la solicitud");
            }
            tiquete.setAsiento(asiento);
        }

        for (Tiquete tiquete : seleccion.keySet()) {
            if (tiquete.getAsiento() != null) {
                continue;
            }
            Long codClase = tiquete.getTarifa().getClaseServicio().getCodClase();
            Asiento libre = delVehiculo.entrySet().stream()
                    .filter(entrada -> !tomados.contains(entrada.getKey()))
                    .map(Map.Entry::getValue)
                    .filter(asiento -> asiento.getClaseServicio().getCodClase().equals(codClase))
                    .min(Comparator.comparingInt(Asiento::getFila))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                            "No quedan sillas libres en la clase " + tiquete.getTarifa().getClaseServicio().getNombre()));
            tomados.add(libre.getNumeroAsiento().toUpperCase(Locale.ROOT));
            tiquete.setAsiento(libre);
        }
    }

    private static String grupo(Tiquete tiquete) {
        if (!"ECONOMICA".equals(tiquete.getTarifa().getClaseServicio().getNombre())) {
            return "A";
        }
        Asiento asiento = tiquete.getAsiento();
        if (asiento == null) {
            return "D";
        }
        return switch (asiento.getUbicacion()) {
            case VENTANA -> "B";
            case CENTRO -> "C";
            case PASILLO -> "D";
        };
    }

    private CanalCheckin canal(CanalCheckin canal, boolean personal) {
        if (canal == null) {
            return personal ? CanalCheckin.MOSTRADOR : CanalCheckin.WEB;
        }
        if (!personal && canal == CanalCheckin.MOSTRADOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El check-in en mostrador lo hace un empleado; en línea usa WEB, APP_MOVIL o KIOSCO");
        }
        return canal;
    }

    private CheckinReservaDTO resumen(Reserva reserva) {
        List<Tiquete> tiquetes = tiquetes(reserva);
        Viaje viaje = tiquetes.get(0).getTarifa().getViaje();
        Map<Long, Checkin> hechos = checkins(reserva);
        Map<Long, List<Equipaje>> equipajes = equipajeRepositorio.deReserva(reserva.getCodReserva()).stream()
                .collect(Collectors.groupingBy(equipaje -> equipaje.getTiquete().getCodTiquete()));

        List<PasajeroCheckin> pasajeros = tiquetes.stream().map(tiquete -> {
            Checkin checkin = hechos.get(tiquete.getCodTiquete());
            ClaseServicio clase = tiquete.getTarifa().getClaseServicio();
            return new PasajeroCheckin(tiquete.getCodTiquete(), tiquete.getNumeroTiquete(),
                    PasajeroCompraServicio.nombre(tiquete.getPasajero()), tiquete.getPasajero().getTipoPasajero().name(),
                    tiquete.getEstado().getNombre(), clase.getNombre(), clase.getEquipajeIncluidoKg(),
                    tiquete.getAsiento() == null ? null : tiquete.getAsiento().getNumeroAsiento(),
                    checkin != null, checkin == null ? null : checkin.getFechaHora(),
                    checkin == null ? null : checkin.getCanal().name(),
                    checkin == null ? null : checkin.getCodigoPaseAbordar(),
                    checkin == null ? null : checkin.getGrupoAbordaje(),
                    equipajes.getOrDefault(tiquete.getCodTiquete(), List.of()).stream()
                            .map(EquipajeRegistrado::desde)
                            .toList());
        }).toList();

        String motivo = motivoCerrado(reserva, viaje, LocalDateTime.now());
        boolean pendientes = tiquetes.stream().anyMatch(tiquete -> tiquete.getEstado().getNombre().equals("EMITIDO")
                && !hechos.containsKey(tiquete.getCodTiquete()));
        if (motivo == null && !pendientes) {
            motivo = "Todos los pasajeros ya tienen check-in";
        }
        return new CheckinReservaDTO(reserva.getCodReserva(), reserva.getCodigoReserva(),
                reserva.getEstado().getNombre(), viaje.getCodViaje(), viaje.getCodigoViaje(),
                viaje.getRuta().getTerminalOrigen().getCiudad().getNombre(),
                viaje.getRuta().getTerminalDestino().getCiudad().getNombre(), viaje.getFechaHoraSalida(),
                ReglasAbordaje.inicioAbordaje(viaje), viaje.getPuertaAnden(), viaje.getEstado().getNombre(),
                ReglasAbordaje.abreCheckin(viaje), ReglasAbordaje.cierraCheckin(viaje), motivo == null, motivo,
                pasajeros);
    }

    private Map<Long, Checkin> checkins(Reserva reserva) {
        return checkinRepositorio.deReserva(reserva.getCodReserva()).stream()
                .collect(Collectors.toMap(checkin -> checkin.getTiquete().getCodTiquete(), Function.identity()));
    }

    private List<Tiquete> tiquetes(Reserva reserva) {
        return tiqueteRepositorio.deReserva(reserva.getCodReserva());
    }

    private Reserva reservaConAcceso(Long codReserva, Authentication autenticacion) {
        Reserva reserva = reservaRepositorio.buscarPorId(codReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la reserva " + codReserva));
        validarAcceso(reserva, autenticacion);
        return reserva;
    }

    private void validarAcceso(Reserva reserva, Authentication autenticacion) {
        if (!usuarioActual.puedeGestionar(reserva, autenticacion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes gestionar tus propias reservas");
        }
    }
}
