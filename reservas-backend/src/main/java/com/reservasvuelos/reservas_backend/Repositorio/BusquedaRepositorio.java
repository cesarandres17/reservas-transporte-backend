package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Busqueda;
import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.model.Cliente;

@Repository
public class BusquedaRepositorio extends RepositorioSql<Busqueda, Long> {

    private static final String SELECCION = """
            SELECT b.cod_busqueda, b.fecha_hora_busqueda, b.fecha_ida, b.fecha_regreso, b.numero_pasajeros,
                   b.resultados, b.cod_cliente, b.cod_ciudad_origen, b.cod_ciudad_destino
            FROM busqueda b
            """;

    private final ClienteRepositorio clienteRepositorio;
    private final CiudadRepositorio ciudadRepositorio;

    public BusquedaRepositorio(JdbcTemplate jdbc, ClienteRepositorio clienteRepositorio,
            CiudadRepositorio ciudadRepositorio) {
        super(jdbc);
        this.clienteRepositorio = clienteRepositorio;
        this.ciudadRepositorio = ciudadRepositorio;
    }

    @Override
    protected Busqueda mapear(ResultSet fila) throws SQLException {
        Busqueda busqueda = new Busqueda();
        busqueda.setCodBusqueda(fila.getLong("cod_busqueda"));
        busqueda.setFechaHoraBusqueda(fechaHora(fila, "fecha_hora_busqueda"));
        busqueda.setFechaIda(fecha(fila, "fecha_ida"));
        busqueda.setFechaRegreso(fecha(fila, "fecha_regreso"));
        busqueda.setNumeroPasajeros(entero(fila, "numero_pasajeros"));
        busqueda.setResultados(entero(fila, "resultados"));
        busqueda.setCliente(referencia(Cliente::new, Cliente::setCodCliente, largo(fila, "cod_cliente")));
        busqueda.setCiudadOrigen(referencia(Ciudad::new, Ciudad::setCodCiudad, largo(fila, "cod_ciudad_origen")));
        busqueda.setCiudadDestino(referencia(Ciudad::new, Ciudad::setCodCiudad, largo(fila, "cod_ciudad_destino")));
        return busqueda;
    }

    @Override
    protected List<Busqueda> completar(List<Busqueda> busquedas) {
        cargar(busquedas, Busqueda::getCliente, Cliente::getCodCliente, clienteRepositorio::porCodigos,
                Busqueda::setCliente);
        cargar(busquedas, Busqueda::getCiudadOrigen, Ciudad::getCodCiudad, ciudadRepositorio::porCodigos,
                Busqueda::setCiudadOrigen);
        cargar(busquedas, Busqueda::getCiudadDestino, Ciudad::getCodCiudad, ciudadRepositorio::porCodigos,
                Busqueda::setCiudadDestino);
        return busquedas;
    }

    @Override
    public List<Busqueda> listar() {
        return consultar(SELECCION + "ORDER BY b.cod_busqueda");
    }

    @Override
    public Optional<Busqueda> buscarPorId(Long codBusqueda) {
        return consultarUno(SELECCION + "WHERE b.cod_busqueda = ?", codBusqueda);
    }

    public Map<Long, Busqueda> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE b.cod_busqueda = ANY(?)", arreglo(codigos)),
                Busqueda::getCodBusqueda);
    }

    @Override
    public boolean existePorId(Long codBusqueda) {
        return existe("SELECT EXISTS (SELECT 1 FROM busqueda WHERE cod_busqueda = ?)", codBusqueda);
    }

    @Override
    public Busqueda insertar(Busqueda busqueda) {
        busqueda.setCodBusqueda(insertarDevolviendoCodigo("""
                INSERT INTO busqueda (fecha_hora_busqueda, fecha_ida, fecha_regreso, numero_pasajeros, resultados,
                        cod_cliente, cod_ciudad_origen, cod_ciudad_destino)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING cod_busqueda
                """, busqueda.getFechaHoraBusqueda(), busqueda.getFechaIda(), busqueda.getFechaRegreso(),
                busqueda.getNumeroPasajeros(), busqueda.getResultados(),
                codigo(busqueda.getCliente(), Cliente::getCodCliente),
                codigo(busqueda.getCiudadOrigen(), Ciudad::getCodCiudad),
                codigo(busqueda.getCiudadDestino(), Ciudad::getCodCiudad)));
        return busqueda;
    }

    @Override
    public Busqueda actualizar(Busqueda busqueda) {
        jdbc.update("""
                UPDATE busqueda
                SET fecha_hora_busqueda = ?, fecha_ida = ?, fecha_regreso = ?, numero_pasajeros = ?, resultados = ?,
                    cod_cliente = ?, cod_ciudad_origen = ?, cod_ciudad_destino = ?
                WHERE cod_busqueda = ?
                """, busqueda.getFechaHoraBusqueda(), busqueda.getFechaIda(), busqueda.getFechaRegreso(),
                busqueda.getNumeroPasajeros(), busqueda.getResultados(),
                codigo(busqueda.getCliente(), Cliente::getCodCliente),
                codigo(busqueda.getCiudadOrigen(), Ciudad::getCodCiudad),
                codigo(busqueda.getCiudadDestino(), Ciudad::getCodCiudad), busqueda.getCodBusqueda());
        return busqueda;
    }

    @Override
    public void eliminar(Long codBusqueda) {
        jdbc.update("DELETE FROM busqueda WHERE cod_busqueda = ?", codBusqueda);
    }
}
