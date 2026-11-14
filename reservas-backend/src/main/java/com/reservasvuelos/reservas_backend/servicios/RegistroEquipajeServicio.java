package com.reservasvuelos.reservas_backend.servicios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.CheckinRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.EquipajeRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TiqueteRepositorio;
import com.reservasvuelos.reservas_backend.dto.CheckinReservaDTO.EquipajeRegistrado;
import com.reservasvuelos.reservas_backend.dto.EquipajeRegistradoDTO;
import com.reservasvuelos.reservas_backend.dto.EquipajeSolicitudDTO;
import com.reservasvuelos.reservas_backend.model.Equipaje;
import com.reservasvuelos.reservas_backend.model.Pago;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.Viaje;
import com.reservasvuelos.reservas_backend.model.enums.EstadoEquipaje;
import com.reservasvuelos.reservas_backend.model.enums.TipoEquipaje;

@Service
public class RegistroEquipajeServicio {

    public static final BigDecimal MAXIMO_MANO_KG = BigDecimal.valueOf(10);
    public static final BigDecimal LIMITE_PIEZA_KG = BigDecimal.valueOf(23);
    public static final BigDecimal MAXIMO_PIEZA_KG = BigDecimal.valueOf(32);
    public static final BigDecimal COSTO_PIEZA_ADICIONAL = BigDecimal.valueOf(95000);
    public static final BigDecimal COSTO_PIEZA_ESPECIAL = BigDecimal.valueOf(120000);
    public static final BigDecimal COSTO_KG_EXCESO = BigDecimal.valueOf(8000);
    public static final int MAXIMO_PIEZAS_DESPACHADAS = 3;

    private final TiqueteRepositorio tiqueteRepositorio;
    private final CheckinRepositorio checkinRepositorio;
    private final EquipajeRepositorio equipajeRepositorio;
    private final CobroServicio cobroServicio;
    private final UsuarioActual usuarioActual;

    public RegistroEquipajeServicio(TiqueteRepositorio tiqueteRepositorio, CheckinRepositorio checkinRepositorio,
            EquipajeRepositorio equipajeRepositorio, CobroServicio cobroServicio, UsuarioActual usuarioActual) {
        this.tiqueteRepositorio = tiqueteRepositorio;
        this.checkinRepositorio = checkinRepositorio;
        this.equipajeRepositorio = equipajeRepositorio;
        this.cobroServicio = cobroServicio;
        this.usuarioActual = usuarioActual;
    }

    @Transactional
    public EquipajeRegistradoDTO registrar(Long codTiquete, EquipajeSolicitudDTO datos, Authentication autenticacion) {
        Tiquete tiquete = tiqueteRepositorio.findById(codTiquete)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el tiquete " + codTiquete));
        if (!usuarioActual.puedeGestionar(tiquete.getReserva(), autenticacion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes gestionar tus propias reservas");
        }
        String pasajero = PasajeroCompraServicio.nombre(tiquete.getPasajero());
        if (!tiquete.getEstado().getNombre().equals("EMITIDO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El tiquete de " + pasajero + " está "
                    + Formatos.legible(tiquete.getEstado().getNombre()).toLowerCase(Locale.ROOT));
        }
        if (!checkinRepositorio.existsById(codTiquete)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Primero haz el check-in de " + pasajero + " y después registra el equipaje");
        }
        Viaje viaje = tiquete.getTarifa().getViaje();
        if (!CompraServicio.ESTADOS_VENTA.contains(viaje.getEstado().getNombre())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El viaje " + viaje.getCodigoViaje() + " está "
                    + Formatos.legible(viaje.getEstado().getNombre()).toLowerCase(Locale.ROOT));
        }
        if (LocalDateTime.now().isAfter(ReglasAbordaje.cierraCheckin(viaje))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La entrega de equipaje del viaje "
                    + viaje.getCodigoViaje() + " cerró el " + Formatos.cuando(ReglasAbordaje.cierraCheckin(viaje)));
        }

        List<Equipaje> actuales = equipajeRepositorio.findByTiquete_CodTiqueteOrderByNumeroPieza(codTiquete);
        int siguiente = actuales.stream().mapToInt(Equipaje::getNumeroPieza).max().orElse(0) + 1;
        long deMano = actuales.stream().filter(pieza -> pieza.getTipo() == TipoEquipaje.MANO).count();
        long despachadas = actuales.size() - deMano;
        BigDecimal incluidoKg = tiquete.getTarifa().getClaseServicio().getEquipajeIncluidoKg();
        boolean incluidaUsada = incluidoKg.signum() == 0
                || actuales.stream().anyMatch(pieza -> pieza.getTipo() == TipoEquipaje.BODEGA);

        List<Equipaje> nuevas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (EquipajeSolicitudDTO.Pieza pieza : datos.piezas()) {
            BigDecimal peso = pieza.pesoKg().setScale(2, RoundingMode.HALF_UP);
            BigDecimal costo;
            if (pieza.tipo() == TipoEquipaje.MANO) {
                if (peso.compareTo(MAXIMO_MANO_KG) > 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipaje de mano puede pesar hasta "
                            + Formatos.peso(MAXIMO_MANO_KG) + "; la pieza de " + Formatos.peso(peso) + " debe ir en bodega");
                }
                if (++deMano > 1) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Cada pasajero puede llevar una sola pieza de equipaje de mano");
                }
                costo = BigDecimal.ZERO;
            } else {
                if (peso.compareTo(MAXIMO_PIEZA_KG) > 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ninguna pieza puede pesar más de "
                            + Formatos.peso(MAXIMO_PIEZA_KG) + "; la de " + Formatos.peso(peso) + " se debe enviar como carga");
                }
                if (++despachadas > MAXIMO_PIEZAS_DESPACHADAS) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cada pasajero puede despachar hasta "
                            + MAXIMO_PIEZAS_DESPACHADAS + " piezas de bodega o especiales");
                }
                if (pieza.tipo() == TipoEquipaje.BODEGA && !incluidaUsada) {
                    costo = exceso(peso, incluidoKg);
                    incluidaUsada = true;
                } else {
                    BigDecimal base = pieza.tipo() == TipoEquipaje.ESPECIAL ? COSTO_PIEZA_ESPECIAL : COSTO_PIEZA_ADICIONAL;
                    costo = base.add(exceso(peso, LIMITE_PIEZA_KG));
                }
            }
            Equipaje equipaje = new Equipaje();
            equipaje.setTiquete(tiquete);
            equipaje.setNumeroPieza(siguiente);
            equipaje.setCodigoEtiqueta(String.format("ET%08d%02d", codTiquete, siguiente));
            equipaje.setTipo(pieza.tipo());
            equipaje.setPesoKg(peso);
            equipaje.setDimensionesCm(pieza.dimensionesCm());
            equipaje.setCostoAdicional(costo.setScale(2));
            equipaje.setEstado(EstadoEquipaje.REGISTRADO);
            nuevas.add(equipaje);
            total = total.add(costo);
            siguiente++;
        }

        Pago pago = null;
        if (total.signum() > 0) {
            if (datos.codMetodo() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipaje tiene un costo de "
                        + Formatos.dinero(total) + "; indica codMetodo para pagarlo");
            }
            pago = cobroServicio.cobrarEquipaje(tiquete, nuevas.get(0).getNumeroPieza(),
                    nuevas.get(nuevas.size() - 1).getNumeroPieza(), total.setScale(2), datos.codMetodo(), datos.cuotas(),
                    usuarioActual.esPersonal(autenticacion));
        }
        equipajeRepositorio.saveAll(nuevas);
        equipajeRepositorio.flush();

        List<EquipajeRegistrado> todas = equipajeRepositorio.findByTiquete_CodTiqueteOrderByNumeroPieza(codTiquete)
                .stream().map(EquipajeRegistrado::desde).toList();
        return new EquipajeRegistradoDTO(codTiquete, PasajeroCompraServicio.nombre(tiquete.getPasajero()), incluidoKg,
                nuevas.stream().map(EquipajeRegistrado::desde).toList(), todas, total.setScale(2),
                pago == null ? null : pago.getCodPago(), pago == null ? null : pago.getReferenciaTransaccion());
    }

    static BigDecimal exceso(BigDecimal peso, BigDecimal limite) {
        BigDecimal sobra = peso.subtract(limite);
        if (sobra.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return sobra.setScale(0, RoundingMode.CEILING).multiply(COSTO_KG_EXCESO);
    }
}
