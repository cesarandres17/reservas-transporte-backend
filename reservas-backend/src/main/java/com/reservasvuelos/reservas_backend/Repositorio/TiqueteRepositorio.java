package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.model.ClaseServicio;
import com.reservasvuelos.reservas_backend.model.EstadoTiquete;
import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.Tarifa;
import com.reservasvuelos.reservas_backend.model.TipoTrayecto;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.Vehiculo;
import com.reservasvuelos.reservas_backend.model.Viaje;

@Repository
public class TiqueteRepositorio extends RepositorioSql<Tiquete, Long> {

    private static final String SELECCION = """
            SELECT t.cod_tiquete, t.numero_tiquete, t.precio_pagado, t.fecha_emision, t.cod_estado_tiquete,
                   t.cod_tipo_trayecto, t.cod_reserva, t.cod_pasajero, t.cod_viaje, t.cod_clase, t.cod_vehiculo,
                   t.numero_asiento
            FROM tiquete t
            """;

    private final EstadoTiqueteRepositorio estadoTiqueteRepositorio;
    private final TipoTrayectoRepositorio tipoTrayectoRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final PasajeroRepositorio pasajeroRepositorio;
    private final TarifaRepositorio tarifaRepositorio;
    private final AsientoRepositorio asientoRepositorio;

    public TiqueteRepositorio(JdbcTemplate jdbc, EstadoTiqueteRepositorio estadoTiqueteRepositorio,
            TipoTrayectoRepositorio tipoTrayectoRepositorio, ReservaRepositorio reservaRepositorio,
            PasajeroRepositorio pasajeroRepositorio, TarifaRepositorio tarifaRepositorio,
            AsientoRepositorio asientoRepositorio) {
        super(jdbc);
        this.estadoTiqueteRepositorio = estadoTiqueteRepositorio;
        this.tipoTrayectoRepositorio = tipoTrayectoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.pasajeroRepositorio = pasajeroRepositorio;
        this.tarifaRepositorio = tarifaRepositorio;
        this.asientoRepositorio = asientoRepositorio;
    }

    @Override
    protected Tiquete mapear(ResultSet fila) throws SQLException {
        Tiquete tiquete = new Tiquete();
        tiquete.setCodTiquete(fila.getLong("cod_tiquete"));
        tiquete.setNumeroTiquete(fila.getString("numero_tiquete"));
        tiquete.setPrecioPagado(fila.getBigDecimal("precio_pagado"));
        tiquete.setFechaEmision(fechaHora(fila, "fecha_emision"));
        tiquete.setEstado(referencia(EstadoTiquete::new, EstadoTiquete::setCodEstado,
                largo(fila, "cod_estado_tiquete")));
        tiquete.setTipoTrayecto(referencia(TipoTrayecto::new, TipoTrayecto::setCodTipo,
                largo(fila, "cod_tipo_trayecto")));
        tiquete.setReserva(referencia(Reserva::new, Reserva::setCodReserva, largo(fila, "cod_reserva")));
        tiquete.setPasajero(referencia(Pasajero::new, Pasajero::setCodPasajero, largo(fila, "cod_pasajero")));
        Tarifa tarifa = new Tarifa();
        tarifa.setViaje(referencia(Viaje::new, Viaje::setCodViaje, largo(fila, "cod_viaje")));
        tarifa.setClaseServicio(referencia(ClaseServicio::new, ClaseServicio::setCodClase, largo(fila, "cod_clase")));
        tiquete.setTarifa(tarifa);
        String numeroAsiento = fila.getString("numero_asiento");
        if (numeroAsiento != null) {
            Asiento asiento = new Asiento();
            asiento.setVehiculo(referencia(Vehiculo::new, Vehiculo::setCodVehiculo, largo(fila, "cod_vehiculo")));
            asiento.setNumeroAsiento(numeroAsiento);
            tiquete.setAsiento(asiento);
        }
        return tiquete;
    }

    @Override
    protected List<Tiquete> completar(List<Tiquete> tiquetes) {
        cargar(tiquetes, Tiquete::getEstado, EstadoTiquete::getCodEstado, estadoTiqueteRepositorio::porCodigos,
                Tiquete::setEstado);
        cargar(tiquetes, Tiquete::getTipoTrayecto, TipoTrayecto::getCodTipo, tipoTrayectoRepositorio::porCodigos,
                Tiquete::setTipoTrayecto);
        cargar(tiquetes, Tiquete::getReserva, Reserva::getCodReserva, reservaRepositorio::porCodigos,
                Tiquete::setReserva);
        cargar(tiquetes, Tiquete::getPasajero, Pasajero::getCodPasajero, pasajeroRepositorio::porCodigos,
                Tiquete::setPasajero);
        cargar(tiquetes, Tiquete::getTarifa, TarifaRepositorio::llave, tarifaRepositorio::porLlaves,
                Tiquete::setTarifa);
        cargar(tiquetes, Tiquete::getAsiento, AsientoRepositorio::llave, asientoRepositorio::porLlaves,
                Tiquete::setAsiento);
        return tiquetes;
    }

    @Override
    public List<Tiquete> listar() {
        return consultar(SELECCION + "ORDER BY t.cod_tiquete");
    }

    @Override
    public Optional<Tiquete> buscarPorId(Long codTiquete) {
        return consultarUno(SELECCION + "WHERE t.cod_tiquete = ?", codTiquete);
    }

    public Map<Long, Tiquete> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE t.cod_tiquete = ANY(?)", arreglo(codigos)),
                Tiquete::getCodTiquete);
    }

    public List<Tiquete> deReserva(Long codReserva) {
        return consultar(SELECCION + "WHERE t.cod_reserva = ? ORDER BY t.cod_tiquete", codReserva);
    }

    public boolean existeNumero(String numeroTiquete) {
        return existe("SELECT EXISTS (SELECT 1 FROM tiquete WHERE numero_tiquete = ?)", numeroTiquete);
    }

    public Set<String> asientosOcupados(Long codViaje) {
        return new LinkedHashSet<>(jdbc.queryForList("""
                SELECT t.numero_asiento
                FROM tiquete t
                WHERE t.cod_viaje = ? AND t.numero_asiento IS NOT NULL
                """, String.class, codViaje));
    }

    public boolean pasajeroEnViaje(Long codViaje, Long codPasajero) {
        return existe("""
                SELECT EXISTS (SELECT 1 FROM tiquete t
                               JOIN estado_tiquete e ON e.cod_estado = t.cod_estado_tiquete
                               WHERE t.cod_viaje = ? AND t.cod_pasajero = ?
                                 AND e.nombre IN ('EMITIDO', 'USADO'))
                """, codViaje, codPasajero);
    }

    public List<Tiquete> pagadosPorSalir(Collection<String> estadosViaje, LocalDateTime desde, LocalDateTime hasta) {
        return consultar(SELECCION + """
                JOIN estado_tiquete et ON et.cod_estado = t.cod_estado_tiquete
                JOIN reserva r ON r.cod_reserva = t.cod_reserva
                JOIN estado_reserva er ON er.cod_estado = r.cod_estado_reserva
                JOIN viaje v ON v.cod_viaje = t.cod_viaje
                JOIN estado_viaje ev ON ev.cod_estado = v.cod_estado_viaje
                WHERE et.nombre = 'EMITIDO' AND er.nombre = 'PAGADA' AND ev.nombre = ANY(?)
                  AND v.fecha_hora_salida > ? AND v.fecha_hora_salida <= ?
                ORDER BY v.fecha_hora_salida, r.cod_reserva, t.cod_tiquete
                """, textos(estadosViaje), desde, hasta);
    }

    public void liberar(Long codTiquete, Long codEstado) {
        jdbc.update("""
                UPDATE tiquete
                SET cod_estado_tiquete = ?, cod_vehiculo = NULL, numero_asiento = NULL
                WHERE cod_tiquete = ?
                """, codEstado, codTiquete);
    }

    public void asignarAsiento(Long codTiquete, Long codVehiculo, String numeroAsiento) {
        jdbc.update("""
                UPDATE tiquete
                SET cod_vehiculo = ?, numero_asiento = ?
                WHERE cod_tiquete = ?
                """, codVehiculo, numeroAsiento, codTiquete);
    }

    @Override
    public boolean existePorId(Long codTiquete) {
        return existe("SELECT EXISTS (SELECT 1 FROM tiquete WHERE cod_tiquete = ?)", codTiquete);
    }

    @Override
    public Tiquete insertar(Tiquete tiquete) {
        Asiento asiento = tiquete.getAsiento();
        tiquete.setCodTiquete(insertarDevolviendoCodigo("""
                INSERT INTO tiquete (numero_tiquete, precio_pagado, fecha_emision, cod_estado_tiquete,
                        cod_tipo_trayecto, cod_reserva, cod_pasajero, cod_viaje, cod_clase, cod_vehiculo,
                        numero_asiento)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING cod_tiquete
                """, tiquete.getNumeroTiquete(), tiquete.getPrecioPagado(), tiquete.getFechaEmision(),
                codigo(tiquete.getEstado(), EstadoTiquete::getCodEstado),
                codigo(tiquete.getTipoTrayecto(), TipoTrayecto::getCodTipo),
                codigo(tiquete.getReserva(), Reserva::getCodReserva),
                codigo(tiquete.getPasajero(), Pasajero::getCodPasajero), codViaje(tiquete), codClase(tiquete),
                asiento == null ? null : codigo(asiento.getVehiculo(), Vehiculo::getCodVehiculo),
                asiento == null ? null : asiento.getNumeroAsiento()));
        return tiquete;
    }

    @Override
    public Tiquete actualizar(Tiquete tiquete) {
        Asiento asiento = tiquete.getAsiento();
        jdbc.update("""
                UPDATE tiquete
                SET numero_tiquete = ?, precio_pagado = ?, fecha_emision = ?, cod_estado_tiquete = ?,
                    cod_tipo_trayecto = ?, cod_reserva = ?, cod_pasajero = ?, cod_viaje = ?, cod_clase = ?,
                    cod_vehiculo = ?, numero_asiento = ?
                WHERE cod_tiquete = ?
                """, tiquete.getNumeroTiquete(), tiquete.getPrecioPagado(), tiquete.getFechaEmision(),
                codigo(tiquete.getEstado(), EstadoTiquete::getCodEstado),
                codigo(tiquete.getTipoTrayecto(), TipoTrayecto::getCodTipo),
                codigo(tiquete.getReserva(), Reserva::getCodReserva),
                codigo(tiquete.getPasajero(), Pasajero::getCodPasajero), codViaje(tiquete), codClase(tiquete),
                asiento == null ? null : codigo(asiento.getVehiculo(), Vehiculo::getCodVehiculo),
                asiento == null ? null : asiento.getNumeroAsiento(), tiquete.getCodTiquete());
        return tiquete;
    }

    @Override
    public void eliminar(Long codTiquete) {
        jdbc.update("DELETE FROM tiquete WHERE cod_tiquete = ?", codTiquete);
    }

    private static Long codViaje(Tiquete tiquete) {
        return tiquete.getTarifa() == null ? null : codigo(tiquete.getTarifa().getViaje(), Viaje::getCodViaje);
    }

    private static Long codClase(Tiquete tiquete) {
        return tiquete.getTarifa() == null ? null
                : codigo(tiquete.getTarifa().getClaseServicio(), ClaseServicio::getCodClase);
    }
}
