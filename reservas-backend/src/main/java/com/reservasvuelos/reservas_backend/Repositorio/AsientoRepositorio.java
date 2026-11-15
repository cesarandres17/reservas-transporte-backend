package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.model.AsientoId;
import com.reservasvuelos.reservas_backend.model.ClaseServicio;
import com.reservasvuelos.reservas_backend.model.Vehiculo;
import com.reservasvuelos.reservas_backend.model.enums.UbicacionAsiento;

@Repository
public class AsientoRepositorio extends RepositorioSql<Asiento, AsientoId> {

    private static final String SELECCION = """
            SELECT a.cod_vehiculo, a.numero_asiento, a.fila, a.letra, a.ubicacion, a.cod_clase
            FROM asiento a
            """;

    private final VehiculoRepositorio vehiculoRepositorio;
    private final ClaseServicioRepositorio claseServicioRepositorio;

    public AsientoRepositorio(JdbcTemplate jdbc, VehiculoRepositorio vehiculoRepositorio,
            ClaseServicioRepositorio claseServicioRepositorio) {
        super(jdbc);
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.claseServicioRepositorio = claseServicioRepositorio;
    }

    public static AsientoId llave(Long codVehiculo, String numeroAsiento) {
        AsientoId id = new AsientoId();
        id.setVehiculo(codVehiculo);
        id.setNumeroAsiento(numeroAsiento);
        return id;
    }

    public static AsientoId llave(Asiento asiento) {
        return llave(codigo(asiento.getVehiculo(), Vehiculo::getCodVehiculo), asiento.getNumeroAsiento());
    }

    @Override
    protected Asiento mapear(ResultSet fila) throws SQLException {
        Asiento asiento = new Asiento();
        asiento.setVehiculo(referencia(Vehiculo::new, Vehiculo::setCodVehiculo, largo(fila, "cod_vehiculo")));
        asiento.setNumeroAsiento(fila.getString("numero_asiento"));
        asiento.setFila(entero(fila, "fila"));
        asiento.setLetra(fila.getString("letra"));
        asiento.setUbicacion(enumerado(fila, "ubicacion", UbicacionAsiento.class));
        asiento.setClaseServicio(referencia(ClaseServicio::new, ClaseServicio::setCodClase, largo(fila, "cod_clase")));
        return asiento;
    }

    @Override
    protected List<Asiento> completar(List<Asiento> asientos) {
        cargar(asientos, Asiento::getVehiculo, Vehiculo::getCodVehiculo, vehiculoRepositorio::porCodigos,
                Asiento::setVehiculo);
        cargar(asientos, Asiento::getClaseServicio, ClaseServicio::getCodClase, claseServicioRepositorio::porCodigos,
                Asiento::setClaseServicio);
        return asientos;
    }

    @Override
    public List<Asiento> listar() {
        return consultar(SELECCION + "ORDER BY a.cod_vehiculo, a.numero_asiento");
    }

    @Override
    public Optional<Asiento> buscarPorId(AsientoId id) {
        return consultarUno(SELECCION + "WHERE a.cod_vehiculo = ? AND a.numero_asiento = ?", id.getVehiculo(),
                id.getNumeroAsiento());
    }

    public Map<AsientoId, Asiento> porLlaves(Collection<AsientoId> llaves) {
        return porClave(consultar(SELECCION + """
                WHERE (a.cod_vehiculo, a.numero_asiento) IN (SELECT * FROM UNNEST(?::bigint[], ?::varchar[]))
                """, llaves.stream().map(AsientoId::getVehiculo).toArray(Long[]::new),
                llaves.stream().map(AsientoId::getNumeroAsiento).toArray(String[]::new)), AsientoRepositorio::llave);
    }

    public List<Asiento> delVehiculo(Long codVehiculo) {
        return consultar(SELECCION + """
                WHERE a.cod_vehiculo = ?
                ORDER BY a.fila, a.letra, a.numero_asiento
                """, codVehiculo);
    }

    @Override
    public boolean existePorId(AsientoId id) {
        return existe("SELECT EXISTS (SELECT 1 FROM asiento WHERE cod_vehiculo = ? AND numero_asiento = ?)",
                id.getVehiculo(), id.getNumeroAsiento());
    }

    @Override
    public Asiento insertar(Asiento asiento) {
        jdbc.update("""
                INSERT INTO asiento (cod_vehiculo, numero_asiento, fila, letra, ubicacion, cod_clase)
                VALUES (?, ?, ?, ?, ?, ?)
                """, codigo(asiento.getVehiculo(), Vehiculo::getCodVehiculo), asiento.getNumeroAsiento(),
                asiento.getFila(), asiento.getLetra(), nombre(asiento.getUbicacion()),
                codigo(asiento.getClaseServicio(), ClaseServicio::getCodClase));
        return asiento;
    }

    @Override
    public Asiento actualizar(Asiento asiento) {
        jdbc.update("""
                UPDATE asiento
                SET fila = ?, letra = ?, ubicacion = ?, cod_clase = ?
                WHERE cod_vehiculo = ? AND numero_asiento = ?
                """, asiento.getFila(), asiento.getLetra(), nombre(asiento.getUbicacion()),
                codigo(asiento.getClaseServicio(), ClaseServicio::getCodClase),
                codigo(asiento.getVehiculo(), Vehiculo::getCodVehiculo), asiento.getNumeroAsiento());
        return asiento;
    }

    @Override
    public void eliminar(AsientoId id) {
        jdbc.update("DELETE FROM asiento WHERE cod_vehiculo = ? AND numero_asiento = ?", id.getVehiculo(),
                id.getNumeroAsiento());
    }
}
