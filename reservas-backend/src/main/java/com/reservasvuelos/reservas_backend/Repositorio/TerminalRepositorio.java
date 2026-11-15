package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.model.Terminal;
import com.reservasvuelos.reservas_backend.model.TipoTransporte;

@Repository
public class TerminalRepositorio extends RepositorioSql<Terminal, Long> {

    private static final String SELECCION = """
            SELECT t.cod_terminal, t.codigo, t.nombre, t.direccion, t.cod_ciudad, t.cod_tipo
            FROM terminal t
            """;

    private final CiudadRepositorio ciudadRepositorio;
    private final TipoTransporteRepositorio tipoTransporteRepositorio;

    public TerminalRepositorio(JdbcTemplate jdbc, CiudadRepositorio ciudadRepositorio,
            TipoTransporteRepositorio tipoTransporteRepositorio) {
        super(jdbc);
        this.ciudadRepositorio = ciudadRepositorio;
        this.tipoTransporteRepositorio = tipoTransporteRepositorio;
    }

    @Override
    protected Terminal mapear(ResultSet fila) throws SQLException {
        Terminal terminal = new Terminal();
        terminal.setCodTerminal(fila.getLong("cod_terminal"));
        terminal.setCodigo(fila.getString("codigo"));
        terminal.setNombre(fila.getString("nombre"));
        terminal.setDireccion(fila.getString("direccion"));
        terminal.setCiudad(referencia(Ciudad::new, Ciudad::setCodCiudad, largo(fila, "cod_ciudad")));
        terminal.setTipoTransporte(referencia(TipoTransporte::new, TipoTransporte::setCodTipo,
                largo(fila, "cod_tipo")));
        return terminal;
    }

    @Override
    protected List<Terminal> completar(List<Terminal> terminales) {
        cargar(terminales, Terminal::getCiudad, Ciudad::getCodCiudad, ciudadRepositorio::porCodigos,
                Terminal::setCiudad);
        cargar(terminales, Terminal::getTipoTransporte, TipoTransporte::getCodTipo,
                tipoTransporteRepositorio::porCodigos, Terminal::setTipoTransporte);
        return terminales;
    }

    @Override
    public List<Terminal> listar() {
        return consultar(SELECCION + "ORDER BY t.cod_terminal");
    }

    @Override
    public Optional<Terminal> buscarPorId(Long codTerminal) {
        return consultarUno(SELECCION + "WHERE t.cod_terminal = ?", codTerminal);
    }

    public Map<Long, Terminal> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE t.cod_terminal = ANY(?)", arreglo(codigos)),
                Terminal::getCodTerminal);
    }

    @Override
    public boolean existePorId(Long codTerminal) {
        return existe("SELECT EXISTS (SELECT 1 FROM terminal WHERE cod_terminal = ?)", codTerminal);
    }

    @Override
    public Terminal insertar(Terminal terminal) {
        terminal.setCodTerminal(insertarDevolviendoCodigo("""
                INSERT INTO terminal (codigo, nombre, direccion, cod_ciudad, cod_tipo)
                VALUES (?, ?, ?, ?, ?)
                RETURNING cod_terminal
                """, terminal.getCodigo(), terminal.getNombre(), terminal.getDireccion(),
                codigo(terminal.getCiudad(), Ciudad::getCodCiudad),
                codigo(terminal.getTipoTransporte(), TipoTransporte::getCodTipo)));
        return terminal;
    }

    @Override
    public Terminal actualizar(Terminal terminal) {
        jdbc.update("""
                UPDATE terminal
                SET codigo = ?, nombre = ?, direccion = ?, cod_ciudad = ?, cod_tipo = ?
                WHERE cod_terminal = ?
                """, terminal.getCodigo(), terminal.getNombre(), terminal.getDireccion(),
                codigo(terminal.getCiudad(), Ciudad::getCodCiudad),
                codigo(terminal.getTipoTransporte(), TipoTransporte::getCodTipo), terminal.getCodTerminal());
        return terminal;
    }

    @Override
    public void eliminar(Long codTerminal) {
        jdbc.update("DELETE FROM terminal WHERE cod_terminal = ?", codTerminal);
    }
}
