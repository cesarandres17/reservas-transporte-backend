package com.reservasvuelos.reservas_backend.servicios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.AsientoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EmpleadoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.FacturaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PagoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.PromocionRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ReservaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TarifaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TiqueteRepositorio;
import com.reservasvuelos.reservas_backend.dto.CompraDTO;
import com.reservasvuelos.reservas_backend.dto.CompraDTO.PasajeroCompra;
import com.reservasvuelos.reservas_backend.dto.PagoCompraDTO;
import com.reservasvuelos.reservas_backend.dto.ReservaDetalleDTO;
import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.model.ClaseServicio;
import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.model.EstadoPago;
import com.reservasvuelos.reservas_backend.model.EstadoReserva;
import com.reservasvuelos.reservas_backend.model.EstadoTiquete;
import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.Promocion;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.Tarifa;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.TipoTrayecto;
import com.reservasvuelos.reservas_backend.model.Usuario;
import com.reservasvuelos.reservas_backend.model.Viaje;
import com.reservasvuelos.reservas_backend.model.enums.CanalReserva;
import com.reservasvuelos.reservas_backend.model.enums.TipoPasajero;

@Service
public class CompraServicio {

    public static final List<String> ESTADOS_VENTA = List.of("PROGRAMADO", "RETRASADO");
    public static final Duration CIERRE_VENTA = Duration.ofHours(1);

    private static final Duration PLAZO_PAGO = Duration.ofHours(24);
    private static final Duration PAGO_ANTES_DE_SALIDA = Duration.ofHours(2);
    private static final Duration MINIMO_PARA_RESERVAR_SIN_PAGO = Duration.ofHours(3);
    private static final List<String> ESTADOS_CANCELABLES = List.of("PENDIENTE", "CONFIRMADA", "PAGADA");
    private static final List<String> ESTADOS_POR_PAGAR = List.of("PENDIENTE", "CONFIRMADA");
    private static final String LETRAS_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Logger LOG = LoggerFactory.getLogger(CompraServicio.class);

    private final TarifaRepositorio tarifaRepositorio;
    private final AsientoRepositorio asientoRepositorio;
    private final TiqueteRepositorio tiqueteRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final PagoRepositorio pagoRepositorio;
    private final FacturaRepositorio facturaRepositorio;
    private final PromocionRepositorio promocionRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;
    private final PasajeroCompraServicio pasajeroCompraServicio;
    private final CobroServicio cobroServicio;
    private final Catalogos catalogos;
    private final UsuarioActual usuarioActual;
    private final SecureRandom aleatorio = new SecureRandom();

    public CompraServicio(TarifaRepositorio tarifaRepositorio, AsientoRepositorio asientoRepositorio,
            TiqueteRepositorio tiqueteRepositorio, ReservaRepositorio reservaRepositorio,
            PagoRepositorio pagoRepositorio, FacturaRepositorio facturaRepositorio,
            PromocionRepositorio promocionRepositorio, ClienteRepositorio clienteRepositorio,
            EmpleadoRepositorio empleadoRepositorio, PasajeroCompraServicio pasajeroCompraServicio,
            CobroServicio cobroServicio, Catalogos catalogos, UsuarioActual usuarioActual) {
        this.tarifaRepositorio = tarifaRepositorio;
        this.asientoRepositorio = asientoRepositorio;
        this.tiqueteRepositorio = tiqueteRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.pagoRepositorio = pagoRepositorio;
        this.facturaRepositorio = facturaRepositorio;
        this.promocionRepositorio = promocionRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
        this.pasajeroCompraServicio = pasajeroCompraServicio;
        this.cobroServicio = cobroServicio;
        this.catalogos = catalogos;
        this.usuarioActual = usuarioActual;
    }

    @Transactional
    public ReservaDetalleDTO comprar(CompraDTO datos, Authentication autenticacion) {
        Usuario usuario = usuarioActual.obtener(autenticacion);
        boolean personal = usuarioActual.esPersonal(autenticacion);
        Cliente cliente = clienteDeLaCompra(datos.codCliente(), usuario, personal);
        CanalReserva canal = canal(datos.canal(), personal);
        Promocion promocion = promocionVigente(datos.codigoPromocion());

        Tarifa tarifa = tarifaRepositorio.bloquear(datos.codViaje(), datos.codClase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El viaje " + datos.codViaje() + " no existe o no vende la clase " + datos.codClase()));
        Viaje viaje = tarifa.getViaje();
        ClaseServicio clase = tarifa.getClaseServicio();
        validarVenta(viaje);

        int cantidad = datos.pasajeros().size();
        if (tarifa.getCuposDisponibles() < cantidad) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo quedan " + tarifa.getCuposDisponibles()
                    + " cupos en clase " + clase.getNombre() + " para el viaje " + viaje.getCodigoViaje());
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime salida = viaje.getFechaHoraSalida();
        if (datos.pago() == null && salida.isBefore(ahora.plus(MINIMO_PARA_RESERVAR_SIN_PAGO))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El viaje sale en menos de "
                    + MINIMO_PARA_RESERVAR_SIN_PAGO.toHours() + " horas, así que la reserva se debe pagar de una vez");
        }

        List<Pasajero> pasajeros = pasajeros(datos.pasajeros(), viaje);
        List<Asiento> asientos = asignarAsientos(viaje, clase, datos.pasajeros());

        BigDecimal porcentaje = promocion == null ? BigDecimal.ZERO : promocion.getPorcentajeDescuento();
        BigDecimal precio = tarifa.getPrecioBase().add(tarifa.getImpuestos());
        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
        BigDecimal descuento = descuento(subtotal, porcentaje);
        LocalDateTime limitePago = ahora.plus(PLAZO_PAGO);
        if (limitePago.isAfter(salida.minus(PAGO_ANTES_DE_SALIDA))) {
            limitePago = salida.minus(PAGO_ANTES_DE_SALIDA);
        }

        Reserva reserva = new Reserva();
        reserva.setCodigoReserva(codigoReserva());
        reserva.setFechaReserva(ahora);
        reserva.setFechaLimitePago(limitePago);
        reserva.setCanal(canal);
        reserva.setEstado(catalogos.estadoReserva("PENDIENTE"));
        reserva.setSubtotal(subtotal);
        reserva.setDescuento(descuento);
        reserva.setTotal(subtotal.subtract(descuento));
        reserva.setCliente(cliente);
        reserva.setEmpleado(personal ? empleadoRepositorio.findById(usuario.getCodUsuario()).orElse(null) : null);
        reserva.setPromocion(promocion);
        reservaRepositorio.save(reserva);

        EstadoTiquete emitido = catalogos.estadoTiquete("EMITIDO");
        TipoTrayecto ida = catalogos.tipoTrayecto("IDA");
        BigDecimal precioPagado = precio.subtract(descuento(precio, porcentaje));
        boolean terrestre = "TERRESTRE".equalsIgnoreCase(
                viaje.getRuta().getTerminalOrigen().getTipoTransporte().getNombre());
        for (int i = 0; i < cantidad; i++) {
            Tiquete tiquete = new Tiquete();
            tiquete.setNumeroTiquete(numeroTiquete(terrestre));
            tiquete.setPrecioPagado(precioPagado);
            tiquete.setFechaEmision(ahora);
            tiquete.setEstado(emitido);
            tiquete.setTipoTrayecto(ida);
            tiquete.setReserva(reserva);
            tiquete.setPasajero(pasajeros.get(i));
            tiquete.setTarifa(tarifa);
            tiquete.setAsiento(asientos.get(i));
            tiqueteRepositorio.save(tiquete);
        }
        tarifa.setCuposDisponibles(tarifa.getCuposDisponibles() - cantidad);

        if (datos.pago() != null) {
            cobroServicio.cobrar(reserva, datos.pago(), personal);
        }
        return detalle(reserva);
    }

    @Transactional
    public ReservaDetalleDTO pagar(Long codReserva, PagoCompraDTO datos, Authentication autenticacion) {
        Reserva reserva = reservaConAcceso(codReserva, autenticacion);
        String estado = reserva.getEstado().getNombre();
        if (!ESTADOS_POR_PAGAR.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La reserva " + reserva.getCodigoReserva() + " está " + estado + " y no se puede pagar");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (estado.equals("PENDIENTE") && reserva.getFechaLimitePago() != null
                && reserva.getFechaLimitePago().isBefore(ahora)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El plazo para pagar la reserva "
                    + reserva.getCodigoReserva() + " venció el " + reserva.getFechaLimitePago().format(FORMATO_FECHA_HORA));
        }
        for (Tiquete tiquete : tiquetes(reserva)) {
            Viaje viaje = tiquete.getTarifa().getViaje();
            if (!ESTADOS_VENTA.contains(viaje.getEstado().getNombre()) || viaje.getFechaHoraSalida().isBefore(ahora)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El viaje " + viaje.getCodigoViaje()
                        + " está " + viaje.getEstado().getNombre() + " y ya no se puede pagar");
            }
        }
        cobroServicio.cobrar(reserva, datos, usuarioActual.esPersonal(autenticacion));
        return detalle(reserva);
    }

    @Transactional
    public ReservaDetalleDTO cancelar(Long codReserva, Authentication autenticacion) {
        Reserva reserva = reservaConAcceso(codReserva, autenticacion);
        String estado = reserva.getEstado().getNombre();
        if (!ESTADOS_CANCELABLES.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La reserva " + reserva.getCodigoReserva() + " ya está " + estado);
        }
        List<Tiquete> tiquetes = tiquetes(reserva);
        LocalDateTime ahora = LocalDateTime.now();
        boolean viajeCancelado = tiquetes.stream()
                .anyMatch(tiquete -> tiquete.getTarifa().getViaje().getEstado().getNombre().equals("CANCELADO"));
        if (!viajeCancelado && tiquetes.stream()
                .anyMatch(tiquete -> !tiquete.getTarifa().getViaje().getFechaHoraSalida().isAfter(ahora))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El viaje ya salió, así que la reserva " + reserva.getCodigoReserva() + " no se puede cancelar");
        }

        boolean pagada = estado.equals("PAGADA");
        if (pagada) {
            Optional<ClaseServicio> sinReembolso = tiquetes.stream()
                    .map(tiquete -> tiquete.getTarifa().getClaseServicio())
                    .filter(clase -> !Boolean.TRUE.equals(clase.getPermiteReembolso()))
                    .findFirst();
            if (!viajeCancelado && sinReembolso.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La clase " + sinReembolso.get().getNombre()
                        + " no permite reembolso, así que la reserva pagada no se puede cancelar");
            }
            EstadoPago reembolsado = catalogos.estadoPago("REEMBOLSADO");
            pagoRepositorio.findByReserva_CodReservaOrderByFechaPago(reserva.getCodReserva()).stream()
                    .filter(pago -> pago.getEstado().getNombre().equals("APROBADO"))
                    .forEach(pago -> pago.setEstado(reembolsado));
        }

        liberar(tiquetes, pagada ? "REEMBOLSADO" : "ANULADO");
        reserva.setEstado(catalogos.estadoReserva("CANCELADA"));
        return detalle(reserva);
    }

    @Transactional(readOnly = true)
    public ReservaDetalleDTO consultar(Long codReserva, Authentication autenticacion) {
        return detalle(reservaConAcceso(codReserva, autenticacion));
    }

    @Transactional(readOnly = true)
    public List<ReservaDetalleDTO> misReservas(Long codCliente, Authentication autenticacion) {
        Long cliente;
        if (usuarioActual.esPersonal(autenticacion)) {
            if (codCliente == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Indica el parámetro cliente con el código del cliente");
            }
            cliente = codCliente;
        } else {
            cliente = usuarioActual.obtener(autenticacion).getCodUsuario();
        }
        return reservaRepositorio.findByCliente_CodClienteOrderByFechaReservaDesc(cliente).stream()
                .map(this::detalle)
                .toList();
    }

    @Scheduled(initialDelay = 30000, fixedDelay = 60000)
    @Transactional
    public void vencerReservasSinPago() {
        List<Reserva> vencidas = reservaRepositorio.findByEstado_NombreAndFechaLimitePagoBefore("PENDIENTE",
                LocalDateTime.now());
        if (vencidas.isEmpty()) {
            return;
        }
        EstadoReserva vencida = catalogos.estadoReserva("VENCIDA");
        for (Reserva reserva : vencidas) {
            liberar(tiquetes(reserva), "ANULADO");
            reserva.setEstado(vencida);
        }
        LOG.info("Se vencieron {} reservas que no se pagaron a tiempo", vencidas.size());
    }

    private Cliente clienteDeLaCompra(Long codCliente, Usuario usuario, boolean personal) {
        if (personal) {
            if (codCliente == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Indica codCliente, el cliente para el que se hace la compra");
            }
            return clienteRepositorio.findById(codCliente)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No existe el cliente " + codCliente));
        }
        Cliente propio = clienteRepositorio.findById(usuario.getCodUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Tu usuario no tiene perfil de cliente"));
        if (codCliente != null && !codCliente.equals(propio.getCodCliente())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes comprar a tu nombre");
        }
        return propio;
    }

    private CanalReserva canal(CanalReserva canal, boolean personal) {
        if (canal == null) {
            return personal ? CanalReserva.TAQUILLA : CanalReserva.WEB;
        }
        if (!personal && (canal == CanalReserva.AGENCIA || canal == CanalReserva.TAQUILLA)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Como cliente solo puedes comprar por WEB o APP_MOVIL");
        }
        return canal;
    }

    private Promocion promocionVigente(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        Promocion promocion = promocionRepositorio.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe la promoción " + codigo.trim()));
        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(promocion.getFechaInicio()) || hoy.isAfter(promocion.getFechaFin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La promoción " + promocion.getCodigo()
                    + " solo es válida del " + promocion.getFechaInicio().format(FORMATO_FECHA)
                    + " al " + promocion.getFechaFin().format(FORMATO_FECHA));
        }
        return promocion;
    }

    private void validarVenta(Viaje viaje) {
        String estado = viaje.getEstado().getNombre();
        if (!ESTADOS_VENTA.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El viaje " + viaje.getCodigoViaje() + " está " + estado + " y no tiene venta");
        }
        if (viaje.getFechaHoraSalida().isBefore(LocalDateTime.now().plus(CIERRE_VENTA))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La venta del viaje " + viaje.getCodigoViaje()
                    + " cierra " + CIERRE_VENTA.toHours() + " hora antes de la salida");
        }
    }

    private List<Pasajero> pasajeros(List<PasajeroCompra> solicitados, Viaje viaje) {
        LocalDate fechaViaje = viaje.getFechaHoraSalida().toLocalDate();
        List<Pasajero> pasajeros = new ArrayList<>();
        Set<Long> incluidos = new HashSet<>();
        for (int i = 0; i < solicitados.size(); i++) {
            Pasajero pasajero = pasajeroCompraServicio.resolver(solicitados.get(i), i + 1, fechaViaje);
            String nombre = PasajeroCompraServicio.nombre(pasajero);
            if (!incluidos.add(pasajero.getCodPasajero())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El pasajero " + nombre + " está repetido en la compra");
            }
            if (tiqueteRepositorio.pasajeroEnViaje(viaje.getCodViaje(), pasajero.getCodPasajero())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El pasajero " + nombre + " ya tiene un tiquete en el viaje " + viaje.getCodigoViaje());
            }
            pasajeros.add(pasajero);
        }
        boolean hayAdulto = pasajeros.stream().anyMatch(pasajero -> PasajeroCompraServicio
                .segunEdad(pasajero.getFechaNacimiento(), fechaViaje) == TipoPasajero.ADULTO);
        if (!hayAdulto) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los niños e infantes deben viajar con al menos un adulto en la misma reserva");
        }
        return pasajeros;
    }

    private List<Asiento> asignarAsientos(Viaje viaje, ClaseServicio clase, List<PasajeroCompra> solicitados) {
        Map<String, Asiento> delVehiculo = new LinkedHashMap<>();
        asientoRepositorio.delVehiculo(viaje.getVehiculo().getCodVehiculo())
                .forEach(asiento -> delVehiculo.put(asiento.getNumeroAsiento().toUpperCase(), asiento));
        Set<String> tomados = new HashSet<>();
        tiqueteRepositorio.asientosOcupados(viaje.getCodViaje())
                .forEach(numero -> tomados.add(numero.toUpperCase()));
        Set<String> ocupados = Set.copyOf(tomados);

        List<Asiento> elegidos = new ArrayList<>(Collections.nCopies(solicitados.size(), null));
        for (int i = 0; i < solicitados.size(); i++) {
            String numero = solicitados.get(i).numeroAsiento();
            if (numero == null || numero.isBlank()) {
                continue;
            }
            String clave = numero.trim().toUpperCase();
            Asiento asiento = delVehiculo.get(clave);
            if (asiento == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El asiento " + clave + " no existe en el vehículo del viaje " + viaje.getCodigoViaje());
            }
            if (!asiento.getClaseServicio().getCodClase().equals(clase.getCodClase())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El asiento " + clave + " es de clase "
                        + asiento.getClaseServicio().getNombre() + " y la compra es en " + clase.getNombre());
            }
            if (!tomados.add(clave)) {
                throw new ResponseStatusException(ocupados.contains(clave) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST,
                        ocupados.contains(clave) ? "El asiento " + clave + " ya está ocupado"
                                : "El asiento " + clave + " está repetido en la compra");
            }
            elegidos.set(i, asiento);
        }

        int filaReferencia = elegidos.stream().filter(Objects::nonNull).map(Asiento::getFila).findFirst().orElse(0);
        List<Asiento> libres = delVehiculo.entrySet().stream()
                .filter(entrada -> !tomados.contains(entrada.getKey()))
                .map(Map.Entry::getValue)
                .filter(asiento -> asiento.getClaseServicio().getCodClase().equals(clase.getCodClase()))
                .sorted(Comparator.comparingInt(asiento -> Math.abs(asiento.getFila() - filaReferencia)))
                .toList();
        int siguiente = 0;
        for (int i = 0; i < elegidos.size() && siguiente < libres.size(); i++) {
            if (elegidos.get(i) == null) {
                elegidos.set(i, libres.get(siguiente++));
            }
        }
        return elegidos;
    }

    private void liberar(List<Tiquete> tiquetes, String estadoFinal) {
        EstadoTiquete nuevoEstado = catalogos.estadoTiquete(estadoFinal);
        Map<ClaveTarifa, Integer> cupos = new LinkedHashMap<>();
        for (Tiquete tiquete : tiquetes) {
            if (!tiquete.getEstado().getNombre().equals("EMITIDO")) {
                continue;
            }
            tiquete.setEstado(nuevoEstado);
            tiquete.setAsiento(null);
            ClaveTarifa clave = new ClaveTarifa(tiquete.getTarifa().getViaje().getCodViaje(),
                    tiquete.getTarifa().getClaseServicio().getCodClase());
            cupos.merge(clave, 1, Integer::sum);
        }
        cupos.forEach((clave, cantidad) -> tarifaRepositorio.devolverCupos(clave.codViaje(), clave.codClase(), cantidad));
    }

    private Reserva reservaConAcceso(Long codReserva, Authentication autenticacion) {
        Reserva reserva = reservaRepositorio.findById(codReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe la reserva " + codReserva));
        String propietario = reserva.getCliente().getUsuario().getEmail();
        if (!usuarioActual.esPersonal(autenticacion) && !propietario.equalsIgnoreCase(autenticacion.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes ver y gestionar tus propias reservas");
        }
        return reserva;
    }

    private List<Tiquete> tiquetes(Reserva reserva) {
        return tiqueteRepositorio.findByReserva_CodReservaOrderByCodTiquete(reserva.getCodReserva());
    }

    private ReservaDetalleDTO detalle(Reserva reserva) {
        return ReservaDetalleDTO.desde(reserva, tiquetes(reserva),
                pagoRepositorio.findByReserva_CodReservaOrderByFechaPago(reserva.getCodReserva()),
                facturaRepositorio.findById(reserva.getCodReserva()).orElse(null));
    }

    private static BigDecimal descuento(BigDecimal valor, BigDecimal porcentaje) {
        return valor.multiply(porcentaje).divide(CIEN, 0, RoundingMode.HALF_UP).setScale(2);
    }

    private String codigoReserva() {
        String codigo;
        do {
            StringBuilder texto = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                texto.append(LETRAS_CODIGO.charAt(aleatorio.nextInt(LETRAS_CODIGO.length())));
            }
            codigo = texto.toString();
        } while (reservaRepositorio.existsByCodigoReserva(codigo));
        return codigo;
    }

    private String numeroTiquete(boolean terrestre) {
        String numero;
        do {
            numero = terrestre ? "B" + String.format("%09d", aleatorio.nextLong(1_000_000_000L))
                    : "134" + String.format("%010d", aleatorio.nextLong(10_000_000_000L));
        } while (tiqueteRepositorio.existsByNumeroTiquete(numero));
        return numero;
    }

    private record ClaveTarifa(Long codViaje, Long codClase) {
    }
}
