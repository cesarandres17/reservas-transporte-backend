package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Ruta;
import com.reservasvuelos.reservas_backend.model.Terminal;

@Repository
public class RutaRepositorio extends RepositorioSql<Ruta, Long> {

    private static final String SELECCION = """
            SELECT r.cod_ruta, r.codigo, r.distancia_km, r.duracion_min, r.cod_terminal_origen, r.cod_terminal_destino
            FROM ruta r
            """;

    private final TerminalRepositorio terminalRepositorio;

    public RutaRepositorio(JdbcTemplate jdbc, TerminalRepositorio terminalRepositorio) {
        super(jdbc);
        this.terminalRepositorio = terminalRepositorio;
    }

    @Override
    protected Ruta mapear(ResultSet fila) throws SQLException {
        Ruta ruta = new Ruta();
        ruta.setCodRuta(fila.getLong("cod_ruta"));
        ruta.setCodigo(fila.getString("codigo"));
        ruta.setDistanciaKm(fila.getBigDecimal("distancia_km"));
        ruta.setDuracionMin(entero(fila, "duracion_min"));
        ruta.setTerminalOrigen(referencia(Terminal::new, Terminal::setCodTerminal, largo(fila, "cod_terminal_origen")));
        ruta.setTerminalDestino(referencia(Terminal::new, Terminal::setCodTerminal,
                largo(fila, "cod_terminal_destino")));
        return ruta;
    }

    @Override
    protected List<Ruta> completar(List<Ruta> rutas) {
        cargar(rutas, Ruta::getTerminalOrigen, Terminal::getCodTerminal, terminalRepositorio::porCodigos,
                Ruta::setTerminalOrigen);
        cargar(rutas, Ruta::getTerminalDestino, Terminal::getCodTerminal, terminalRepositorio::porCodigos,
                Ruta::setTerminalDestino);
        return rutas;
    }

    @Override
    public List<Ruta> listar() {
        return consultar(SELECCION + "ORDER BY r.cod_ruta");
    }

    @Override
    public Optional<Ruta> buscarPorId(Long codRuta) {
        return consultarUno(SELECCION + "WHERE r.cod_ruta = ?", codRuta);
    }

    public Map<Long, Ruta> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE r.cod_ruta = ANY(?)", arreglo(codigos)), Ruta::getCodRuta);
    }

    @Override
    public boolean existePorId(Long codRuta) {
        return existe("SELECT EXISTS (SELECT 1 FROM ruta WHERE cod_ruta = ?)", codRuta);
    }

    @Override
    public Ruta insertar(Ruta ruta) {
        ruta.setCodRuta(insertarDevolviendoCodigo("""
                INSERT INTO ruta (codigo, distancia_km, duracion_min, cod_terminal_origen, cod_terminal_destino)
                VALUES (?, ?, ?, ?, ?)
                RETURNING cod_ruta
                """, ruta.getCodigo(), ruta.getDistanciaKm(), ruta.getDuracionMin(),
                codigo(ruta.getTerminalOrigen(), Terminal::getCodTerminal),
                codigo(ruta.getTerminalDestino(), Terminal::getCodTerminal)));
        return ruta;
    }

    @Override
    public Ruta actualizar(Ruta ruta) {
        jdbc.update("""
                UPDATE ruta
                SET codigo = ?, distancia_km = ?, duracion_min = ?, cod_terminal_origen = ?, cod_terminal_destino = ?
                WHERE cod_ruta = ?
                """, ruta.getCodigo(), ruta.getDistanciaKm(), ruta.getDuracionMin(),
                codigo(ruta.getTerminalOrigen(), Terminal::getCodTerminal),
                codigo(ruta.getTerminalDestino(), Terminal::getCodTerminal), ruta.getCodRuta());
        return ruta;
    }

    @Override
    public void eliminar(Long codRuta) {
        jdbc.update("DELETE FROM ruta WHERE cod_ruta = ?", codRuta);
    }
}
