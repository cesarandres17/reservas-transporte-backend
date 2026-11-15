package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.EstadoViaje;
import com.reservasvuelos.reservas_backend.model.Ruta;
import com.reservasvuelos.reservas_backend.model.Vehiculo;
import com.reservasvuelos.reservas_backend.model.Viaje;

@Repository
public class ViajeRepositorio extends RepositorioSql<Viaje, Long> {

    private static final String SELECCION = """
            SELECT v.cod_viaje, v.codigo_viaje, v.fecha_hora_salida, v.fecha_hora_llegada, v.puerta_anden,
                   v.cod_estado_viaje, v.cod_ruta, v.cod_vehiculo
            FROM viaje v
            """;

    private final EstadoViajeRepositorio estadoViajeRepositorio;
    private final RutaRepositorio rutaRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;

    public ViajeRepositorio(JdbcTemplate jdbc, EstadoViajeRepositorio estadoViajeRepositorio,
            RutaRepositorio rutaRepositorio, VehiculoRepositorio vehiculoRepositorio) {
        super(jdbc);
        this.estadoViajeRepositorio = estadoViajeRepositorio;
        this.rutaRepositorio = rutaRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
    }

    @Override
    protected Viaje mapear(ResultSet fila) throws SQLException {
        Viaje viaje = new Viaje();
        viaje.setCodViaje(fila.getLong("cod_viaje"));
        viaje.setCodigoViaje(fila.getString("codigo_viaje"));
        viaje.setFechaHoraSalida(fechaHora(fila, "fecha_hora_salida"));
        viaje.setFechaHoraLlegada(fechaHora(fila, "fecha_hora_llegada"));
        viaje.setPuertaAnden(fila.getString("puerta_anden"));
        viaje.setEstado(referencia(EstadoViaje::new, EstadoViaje::setCodEstado, largo(fila, "cod_estado_viaje")));
        viaje.setRuta(referencia(Ruta::new, Ruta::setCodRuta, largo(fila, "cod_ruta")));
        viaje.setVehiculo(referencia(Vehiculo::new, Vehiculo::setCodVehiculo, largo(fila, "cod_vehiculo")));
        return viaje;
    }

    @Override
    protected List<Viaje> completar(List<Viaje> viajes) {
        cargar(viajes, Viaje::getEstado, EstadoViaje::getCodEstado, estadoViajeRepositorio::porCodigos,
                Viaje::setEstado);
        cargar(viajes, Viaje::getRuta, Ruta::getCodRuta, rutaRepositorio::porCodigos, Viaje::setRuta);
        cargar(viajes, Viaje::getVehiculo, Vehiculo::getCodVehiculo, vehiculoRepositorio::porCodigos,
                Viaje::setVehiculo);
        return viajes;
    }

    @Override
    public List<Viaje> listar() {
        return consultar(SELECCION + "ORDER BY v.cod_viaje");
    }

    @Override
    public Optional<Viaje> buscarPorId(Long codViaje) {
        return consultarUno(SELECCION + "WHERE v.cod_viaje = ?", codViaje);
    }

    public Map<Long, Viaje> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE v.cod_viaje = ANY(?)", arreglo(codigos)), Viaje::getCodViaje);
    }

    public List<Viaje> buscarDisponibles(Long origen, Long destino, LocalDateTime desde, LocalDateTime hasta,
            Collection<String> estados) {
        return consultar(SELECCION + """
                JOIN estado_viaje e ON e.cod_estado = v.cod_estado_viaje
                JOIN ruta r ON r.cod_ruta = v.cod_ruta
                JOIN terminal o ON o.cod_terminal = r.cod_terminal_origen
                JOIN terminal d ON d.cod_terminal = r.cod_terminal_destino
                WHERE o.cod_ciudad = ? AND d.cod_ciudad = ?
                  AND v.fecha_hora_salida >= ? AND v.fecha_hora_salida < ?
                  AND e.nombre = ANY(?)
                ORDER BY v.fecha_hora_salida, v.cod_viaje
                """, origen, destino, desde, hasta, textos(estados));
    }

    @Override
    public boolean existePorId(Long codViaje) {
        return existe("SELECT EXISTS (SELECT 1 FROM viaje WHERE cod_viaje = ?)", codViaje);
    }

    @Override
    public Viaje insertar(Viaje viaje) {
        viaje.setCodViaje(insertarDevolviendoCodigo("""
                INSERT INTO viaje (codigo_viaje, fecha_hora_salida, fecha_hora_llegada, puerta_anden, cod_estado_viaje,
                        cod_ruta, cod_vehiculo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING cod_viaje
                """, viaje.getCodigoViaje(), viaje.getFechaHoraSalida(), viaje.getFechaHoraLlegada(),
                viaje.getPuertaAnden(), codigo(viaje.getEstado(), EstadoViaje::getCodEstado),
                codigo(viaje.getRuta(), Ruta::getCodRuta), codigo(viaje.getVehiculo(), Vehiculo::getCodVehiculo)));
        return viaje;
    }

    @Override
    public Viaje actualizar(Viaje viaje) {
        jdbc.update("""
                UPDATE viaje
                SET codigo_viaje = ?, fecha_hora_salida = ?, fecha_hora_llegada = ?, puerta_anden = ?,
                    cod_estado_viaje = ?, cod_ruta = ?, cod_vehiculo = ?
                WHERE cod_viaje = ?
                """, viaje.getCodigoViaje(), viaje.getFechaHoraSalida(), viaje.getFechaHoraLlegada(),
                viaje.getPuertaAnden(), codigo(viaje.getEstado(), EstadoViaje::getCodEstado),
                codigo(viaje.getRuta(), Ruta::getCodRuta), codigo(viaje.getVehiculo(), Vehiculo::getCodVehiculo),
                viaje.getCodViaje());
        return viaje;
    }

    @Override
    public void eliminar(Long codViaje) {
        jdbc.update("DELETE FROM viaje WHERE cod_viaje = ?", codViaje);
    }
}
