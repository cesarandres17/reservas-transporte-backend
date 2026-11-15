package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.AsientoRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.BusquedaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.CiudadRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TarifaRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.TiqueteRepositorio;
import com.reservasvuelos.reservas_backend.Repositorio.ViajeRepositorio;
import com.reservasvuelos.reservas_backend.dto.MapaAsientosDTO;
import com.reservasvuelos.reservas_backend.dto.MapaAsientosDTO.AsientoMapa;
import com.reservasvuelos.reservas_backend.dto.ViajeDisponibleDTO;
import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.model.Busqueda;
import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.model.Viaje;

@Service
public class DisponibilidadServicio {

    private static final int DIAS_SIN_FECHA = 30;
    private static final int MAXIMO_PASAJEROS = 9;

    private final ViajeRepositorio viajeRepositorio;
    private final TarifaRepositorio tarifaRepositorio;
    private final AsientoRepositorio asientoRepositorio;
    private final TiqueteRepositorio tiqueteRepositorio;
    private final CiudadRepositorio ciudadRepositorio;
    private final BusquedaRepositorio busquedaRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final UsuarioActual usuarioActual;

    public DisponibilidadServicio(ViajeRepositorio viajeRepositorio, TarifaRepositorio tarifaRepositorio,
            AsientoRepositorio asientoRepositorio, TiqueteRepositorio tiqueteRepositorio,
            CiudadRepositorio ciudadRepositorio, BusquedaRepositorio busquedaRepositorio,
            ClienteRepositorio clienteRepositorio, UsuarioActual usuarioActual) {
        this.viajeRepositorio = viajeRepositorio;
        this.tarifaRepositorio = tarifaRepositorio;
        this.asientoRepositorio = asientoRepositorio;
        this.tiqueteRepositorio = tiqueteRepositorio;
        this.ciudadRepositorio = ciudadRepositorio;
        this.busquedaRepositorio = busquedaRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.usuarioActual = usuarioActual;
    }

    @Transactional
    public List<ViajeDisponibleDTO> buscar(Long origen, Long destino, LocalDate fecha, int pasajeros,
            Authentication autenticacion) {
        if (origen.equals(destino)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El origen y el destino deben ser distintos");
        }
        if (pasajeros < 1 || pasajeros > MAXIMO_PASAJEROS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Puedes buscar entre 1 y " + MAXIMO_PASAJEROS + " pasajeros");
        }
        LocalDate hoy = LocalDate.now();
        if (fecha != null && fecha.isBefore(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha del viaje no puede ser anterior a hoy");
        }
        Ciudad ciudadOrigen = ciudad(origen);
        Ciudad ciudadDestino = ciudad(destino);

        LocalDateTime primeraSalida = LocalDateTime.now().plus(CompraServicio.CIERRE_VENTA);
        LocalDateTime desde = fecha == null || fecha.atStartOfDay().isBefore(primeraSalida)
                ? primeraSalida : fecha.atStartOfDay();
        LocalDateTime hasta = (fecha == null ? hoy.plusDays(DIAS_SIN_FECHA) : fecha).plusDays(1).atStartOfDay();

        List<Viaje> viajes = viajeRepositorio.buscarDisponibles(origen, destino, desde, hasta,
                CompraServicio.ESTADOS_VENTA);
        Map<Long, List<ViajeDisponibleDTO.Clase>> clases = viajes.isEmpty() ? Map.of()
                : tarifaRepositorio.deLosViajes(viajes.stream().map(Viaje::getCodViaje).toList()).stream()
                        .filter(tarifa -> tarifa.getCuposDisponibles() >= pasajeros)
                        .collect(Collectors.groupingBy(tarifa -> tarifa.getViaje().getCodViaje(),
                                Collectors.mapping(ViajeDisponibleDTO.Clase::desde, Collectors.toList())));
        List<ViajeDisponibleDTO> resultado = viajes.stream()
                .filter(viaje -> clases.containsKey(viaje.getCodViaje()))
                .map(viaje -> ViajeDisponibleDTO.desde(viaje, clases.get(viaje.getCodViaje())))
                .toList();

        Busqueda busqueda = new Busqueda();
        busqueda.setFechaHoraBusqueda(LocalDateTime.now());
        busqueda.setFechaIda(fecha == null ? hoy : fecha);
        busqueda.setNumeroPasajeros(pasajeros);
        busqueda.setResultados(resultado.size());
        busqueda.setCliente(usuarioActual.buscar(autenticacion)
                .flatMap(usuario -> clienteRepositorio.buscarPorId(usuario.getCodUsuario()))
                .orElse(null));
        busqueda.setCiudadOrigen(ciudadOrigen);
        busqueda.setCiudadDestino(ciudadDestino);
        busquedaRepositorio.insertar(busqueda);

        return resultado;
    }

    @Transactional(readOnly = true)
    public MapaAsientosDTO asientos(Long codViaje) {
        Viaje viaje = viajeRepositorio.buscarPorId(codViaje)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el viaje " + codViaje));
        List<Asiento> asientos = asientoRepositorio.delVehiculo(viaje.getVehiculo().getCodVehiculo());
        Set<String> ocupados = tiqueteRepositorio.asientosOcupados(codViaje);
        List<AsientoMapa> mapa = asientos.stream()
                .map(asiento -> new AsientoMapa(
                        asiento.getNumeroAsiento(),
                        asiento.getFila(),
                        asiento.getLetra(),
                        asiento.getUbicacion().name(),
                        asiento.getClaseServicio().getCodClase(),
                        asiento.getClaseServicio().getNombre(),
                        ocupados.contains(asiento.getNumeroAsiento())))
                .toList();
        int libres = (int) mapa.stream().filter(asiento -> !asiento.ocupado()).count();
        return new MapaAsientosDTO(viaje.getCodViaje(), viaje.getCodigoViaje(), viaje.getVehiculo().getModelo(),
                mapa.size(), libres, mapa);
    }

    private Ciudad ciudad(Long codCiudad) {
        return ciudadRepositorio.buscarPorId(codCiudad)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la ciudad " + codCiudad));
    }
}
