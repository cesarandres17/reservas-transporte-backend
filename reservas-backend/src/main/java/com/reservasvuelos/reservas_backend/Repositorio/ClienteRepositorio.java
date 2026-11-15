package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.model.Usuario;

@Repository
public class ClienteRepositorio extends RepositorioSql<Cliente, Long> {

    private static final String SELECCION = """
            SELECT c.cod_cliente, c.recibe_notificaciones, c.horas_recordatorio
            FROM cliente c
            """;

    private final UsuarioRepositorio usuarioRepositorio;

    public ClienteRepositorio(JdbcTemplate jdbc, UsuarioRepositorio usuarioRepositorio) {
        super(jdbc);
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    protected Cliente mapear(ResultSet fila) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setCodCliente(fila.getLong("cod_cliente"));
        cliente.setUsuario(referencia(Usuario::new, Usuario::setCodUsuario, cliente.getCodCliente()));
        cliente.setRecibeNotificaciones(logico(fila, "recibe_notificaciones"));
        cliente.setHorasRecordatorio(entero(fila, "horas_recordatorio"));
        return cliente;
    }

    @Override
    protected List<Cliente> completar(List<Cliente> clientes) {
        cargar(clientes, Cliente::getUsuario, Usuario::getCodUsuario, usuarioRepositorio::porCodigos,
                Cliente::setUsuario);
        return clientes;
    }

    @Override
    public List<Cliente> listar() {
        return consultar(SELECCION + "ORDER BY c.cod_cliente");
    }

    @Override
    public Optional<Cliente> buscarPorId(Long codCliente) {
        return consultarUno(SELECCION + "WHERE c.cod_cliente = ?", codCliente);
    }

    public Map<Long, Cliente> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE c.cod_cliente = ANY(?)", arreglo(codigos)),
                Cliente::getCodCliente);
    }

    public List<Cliente> conNotificaciones() {
        return consultar(SELECCION + "WHERE c.recibe_notificaciones = TRUE ORDER BY c.cod_cliente");
    }

    public List<Cliente> conTiquetesEnViaje(Long codViaje) {
        return consultar(SELECCION + """
                WHERE c.cod_cliente IN (
                    SELECT r.cod_cliente
                    FROM tiquete t
                    JOIN estado_tiquete et ON et.cod_estado = t.cod_estado_tiquete
                    JOIN reserva r ON r.cod_reserva = t.cod_reserva
                    JOIN estado_reserva er ON er.cod_estado = r.cod_estado_reserva
                    WHERE t.cod_viaje = ? AND et.nombre = 'EMITIDO'
                      AND er.nombre IN ('PENDIENTE', 'CONFIRMADA', 'PAGADA'))
                ORDER BY c.cod_cliente
                """, codViaje);
    }

    @Override
    public boolean existePorId(Long codCliente) {
        return existe("SELECT EXISTS (SELECT 1 FROM cliente WHERE cod_cliente = ?)", codCliente);
    }

    @Override
    public Cliente insertar(Cliente cliente) {
        cliente.setCodCliente(cliente.getUsuario().getCodUsuario());
        jdbc.update("""
                INSERT INTO cliente (cod_cliente, recibe_notificaciones, horas_recordatorio)
                VALUES (?, ?, ?)
                """, cliente.getCodCliente(), cliente.getRecibeNotificaciones(), cliente.getHorasRecordatorio());
        return cliente;
    }

    @Override
    public Cliente actualizar(Cliente cliente) {
        jdbc.update("""
                UPDATE cliente
                SET recibe_notificaciones = ?, horas_recordatorio = ?
                WHERE cod_cliente = ?
                """, cliente.getRecibeNotificaciones(), cliente.getHorasRecordatorio(), cliente.getCodCliente());
        return cliente;
    }

    @Override
    public void eliminar(Long codCliente) {
        jdbc.update("DELETE FROM cliente WHERE cod_cliente = ?", codCliente);
    }
}
