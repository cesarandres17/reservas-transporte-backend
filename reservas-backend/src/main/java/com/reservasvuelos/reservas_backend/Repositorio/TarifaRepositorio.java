package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.ClaseServicio;
import com.reservasvuelos.reservas_backend.model.Tarifa;
import com.reservasvuelos.reservas_backend.model.TarifaId;
import com.reservasvuelos.reservas_backend.model.Viaje;

@Repository
public class TarifaRepositorio extends RepositorioSql<Tarifa, TarifaId> {

    private static final String SELECCION = """
            SELECT t.cod_viaje, t.cod_clase, t.precio_base, t.impuestos, t.cupos_disponibles
            FROM tarifa t
            """;

    private final ViajeRepositorio viajeRepositorio;
    private final ClaseServicioRepositorio claseServicioRepositorio;

    public TarifaRepositorio(JdbcTemplate jdbc, ViajeRepositorio viajeRepositorio,
            ClaseServicioRepositorio claseServicioRepositorio) {
        super(jdbc);
        this.viajeRepositorio = viajeRepositorio;
        this.claseServicioRepositorio = claseServicioRepositorio;
    }

    public static TarifaId llave(Long codViaje, Long codClase) {
        TarifaId id = new TarifaId();
        id.setViaje(codViaje);
        id.setClaseServicio(codClase);
        return id;
    }

    public static TarifaId llave(Tarifa tarifa) {
        return llave(codigo(tarifa.getViaje(), Viaje::getCodViaje),
                codigo(tarifa.getClaseServicio(), ClaseServicio::getCodClase));
    }

    @Override
    protected Tarifa mapear(ResultSet fila) throws SQLException {
        Tarifa tarifa = new Tarifa();
        tarifa.setViaje(referencia(Viaje::new, Viaje::setCodViaje, largo(fila, "cod_viaje")));
        tarifa.setClaseServicio(referencia(ClaseServicio::new, ClaseServicio::setCodClase, largo(fila, "cod_clase")));
        tarifa.setPrecioBase(fila.getBigDecimal("precio_base"));
        tarifa.setImpuestos(fila.getBigDecimal("impuestos"));
        tarifa.setCuposDisponibles(entero(fila, "cupos_disponibles"));
        return tarifa;
    }

    @Override
    protected List<Tarifa> completar(List<Tarifa> tarifas) {
        cargar(tarifas, Tarifa::getViaje, Viaje::getCodViaje, viajeRepositorio::porCodigos, Tarifa::setViaje);
        cargar(tarifas, Tarifa::getClaseServicio, ClaseServicio::getCodClase, claseServicioRepositorio::porCodigos,
                Tarifa::setClaseServicio);
        return tarifas;
    }

    @Override
    public List<Tarifa> listar() {
        return consultar(SELECCION + "ORDER BY t.cod_viaje, t.cod_clase");
    }

    @Override
    public Optional<Tarifa> buscarPorId(TarifaId id) {
        return consultarUno(SELECCION + "WHERE t.cod_viaje = ? AND t.cod_clase = ?", id.getViaje(),
                id.getClaseServicio());
    }

    public Map<TarifaId, Tarifa> porLlaves(Collection<TarifaId> llaves) {
        return porClave(consultar(SELECCION + """
                WHERE (t.cod_viaje, t.cod_clase) IN (SELECT * FROM UNNEST(?::bigint[], ?::bigint[]))
                """, llaves.stream().map(TarifaId::getViaje).toArray(Long[]::new),
                llaves.stream().map(TarifaId::getClaseServicio).toArray(Long[]::new)), TarifaRepositorio::llave);
    }

    public List<Tarifa> deLosViajes(Collection<Long> viajes) {
        return consultar(SELECCION + """
                WHERE t.cod_viaje = ANY(?)
                ORDER BY t.precio_base, t.cod_viaje, t.cod_clase
                """, arreglo(viajes));
    }

    public Optional<Tarifa> bloquear(Long codViaje, Long codClase) {
        return consultarUno(SELECCION + """
                WHERE t.cod_viaje = ? AND t.cod_clase = ?
                FOR UPDATE
                """, codViaje, codClase);
    }

    public int descontarCupos(Long codViaje, Long codClase, int cantidad) {
        return jdbc.update("""
                UPDATE tarifa
                SET cupos_disponibles = cupos_disponibles - ?
                WHERE cod_viaje = ? AND cod_clase = ?
                """, cantidad, codViaje, codClase);
    }

    public int devolverCupos(Long codViaje, Long codClase, int cantidad) {
        return jdbc.update("""
                UPDATE tarifa
                SET cupos_disponibles = cupos_disponibles + ?
                WHERE cod_viaje = ? AND cod_clase = ?
                """, cantidad, codViaje, codClase);
    }

    @Override
    public boolean existePorId(TarifaId id) {
        return existe("SELECT EXISTS (SELECT 1 FROM tarifa WHERE cod_viaje = ? AND cod_clase = ?)", id.getViaje(),
                id.getClaseServicio());
    }

    @Override
    public Tarifa insertar(Tarifa tarifa) {
        jdbc.update("""
                INSERT INTO tarifa (cod_viaje, cod_clase, precio_base, impuestos, cupos_disponibles)
                VALUES (?, ?, ?, ?, ?)
                """, codigo(tarifa.getViaje(), Viaje::getCodViaje),
                codigo(tarifa.getClaseServicio(), ClaseServicio::getCodClase), tarifa.getPrecioBase(),
                tarifa.getImpuestos(), tarifa.getCuposDisponibles());
        return tarifa;
    }

    @Override
    public Tarifa actualizar(Tarifa tarifa) {
        jdbc.update("""
                UPDATE tarifa
                SET precio_base = ?, impuestos = ?, cupos_disponibles = ?
                WHERE cod_viaje = ? AND cod_clase = ?
                """, tarifa.getPrecioBase(), tarifa.getImpuestos(), tarifa.getCuposDisponibles(),
                codigo(tarifa.getViaje(), Viaje::getCodViaje),
                codigo(tarifa.getClaseServicio(), ClaseServicio::getCodClase));
        return tarifa;
    }

    @Override
    public void eliminar(TarifaId id) {
        jdbc.update("DELETE FROM tarifa WHERE cod_viaje = ? AND cod_clase = ?", id.getViaje(),
                id.getClaseServicio());
    }
}
