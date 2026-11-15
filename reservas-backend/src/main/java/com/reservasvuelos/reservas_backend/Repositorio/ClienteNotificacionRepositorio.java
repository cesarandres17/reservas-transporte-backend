package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.model.ClienteNotificacion;
import com.reservasvuelos.reservas_backend.model.ClienteNotificacionId;
import com.reservasvuelos.reservas_backend.model.EstadoNotificacion;
import com.reservasvuelos.reservas_backend.model.Notificacion;

@Repository
public class ClienteNotificacionRepositorio extends RepositorioSql<ClienteNotificacion, ClienteNotificacionId> {

    private static final String SELECCION = """
            SELECT cn.cod_cliente, cn.cod_notificacion, cn.cod_estado_notificacion, cn.fecha_envio,
                   cn.fecha_lectura
            FROM cliente_notificacion cn
            """;

    private static final String INSERCION = """
            INSERT INTO cliente_notificacion (cod_cliente, cod_notificacion, cod_estado_notificacion,
                    fecha_envio, fecha_lectura)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final ClienteRepositorio clienteRepositorio;
    private final NotificacionRepositorio notificacionRepositorio;
    private final EstadoNotificacionRepositorio estadoNotificacionRepositorio;

    public ClienteNotificacionRepositorio(JdbcTemplate jdbc, ClienteRepositorio clienteRepositorio,
            NotificacionRepositorio notificacionRepositorio,
            EstadoNotificacionRepositorio estadoNotificacionRepositorio) {
        super(jdbc);
        this.clienteRepositorio = clienteRepositorio;
        this.notificacionRepositorio = notificacionRepositorio;
        this.estadoNotificacionRepositorio = estadoNotificacionRepositorio;
    }

    @Override
    protected ClienteNotificacion mapear(ResultSet fila) throws SQLException {
        ClienteNotificacion envio = new ClienteNotificacion();
        envio.setCliente(referencia(Cliente::new, Cliente::setCodCliente, largo(fila, "cod_cliente")));
        envio.setNotificacion(referencia(Notificacion::new, Notificacion::setCodNotificacion,
                largo(fila, "cod_notificacion")));
        envio.setEstadoNotificacion(referencia(EstadoNotificacion::new, EstadoNotificacion::setCodEstado,
                largo(fila, "cod_estado_notificacion")));
        envio.setFechaEnvio(fechaHora(fila, "fecha_envio"));
        envio.setFechaLectura(fechaHora(fila, "fecha_lectura"));
        return envio;
    }

    @Override
    protected List<ClienteNotificacion> completar(List<ClienteNotificacion> envios) {
        cargar(envios, ClienteNotificacion::getCliente, Cliente::getCodCliente, clienteRepositorio::porCodigos,
                ClienteNotificacion::setCliente);
        cargar(envios, ClienteNotificacion::getNotificacion, Notificacion::getCodNotificacion,
                notificacionRepositorio::porCodigos, ClienteNotificacion::setNotificacion);
        cargar(envios, ClienteNotificacion::getEstadoNotificacion, EstadoNotificacion::getCodEstado,
                estadoNotificacionRepositorio::porCodigos, ClienteNotificacion::setEstadoNotificacion);
        return envios;
    }

    @Override
    public List<ClienteNotificacion> listar() {
        return consultar(SELECCION + "ORDER BY cn.cod_cliente, cn.cod_notificacion");
    }

    @Override
    public Optional<ClienteNotificacion> buscarPorId(ClienteNotificacionId id) {
        return consultarUno(SELECCION + "WHERE cn.cod_cliente = ? AND cn.cod_notificacion = ?", id.getCliente(),
                id.getNotificacion());
    }

    public List<ClienteNotificacion> deCliente(Long codCliente, LocalDateTime hasta) {
        return consultar(SELECCION + """
                JOIN notificacion n ON n.cod_notificacion = cn.cod_notificacion
                WHERE cn.cod_cliente = ? AND n.fecha_creacion <= ?
                ORDER BY n.fecha_creacion DESC, n.cod_notificacion DESC
                """, codCliente, hasta);
    }

    public List<ClienteNotificacion> destinatarios(Long codNotificacion) {
        return consultar(SELECCION + """
                WHERE cn.cod_notificacion = ?
                ORDER BY cn.cod_cliente
                """, codNotificacion);
    }

    public boolean yaNotificado(Long codCliente, Long codViaje, String tipo) {
        return existe("""
                SELECT EXISTS (SELECT 1 FROM cliente_notificacion cn
                               JOIN notificacion n ON n.cod_notificacion = cn.cod_notificacion
                               JOIN tipo_notificacion tn ON tn.cod_tipo = n.cod_tipo_notificacion
                               WHERE cn.cod_cliente = ? AND n.cod_viaje = ? AND tn.nombre = ?)
                """, codCliente, codViaje, tipo);
    }

    @Override
    public boolean existePorId(ClienteNotificacionId id) {
        return existe("""
                SELECT EXISTS (SELECT 1 FROM cliente_notificacion WHERE cod_cliente = ? AND cod_notificacion = ?)
                """, id.getCliente(), id.getNotificacion());
    }

    @Override
    public ClienteNotificacion insertar(ClienteNotificacion envio) {
        jdbc.update(INSERCION, valores(envio));
        return envio;
    }

    public List<ClienteNotificacion> insertarTodos(List<ClienteNotificacion> envios) {
        jdbc.batchUpdate(INSERCION, envios.stream().map(ClienteNotificacionRepositorio::valores).toList());
        return envios;
    }

    @Override
    public ClienteNotificacion actualizar(ClienteNotificacion envio) {
        jdbc.update("""
                UPDATE cliente_notificacion
                SET cod_estado_notificacion = ?, fecha_envio = ?, fecha_lectura = ?
                WHERE cod_cliente = ? AND cod_notificacion = ?
                """, codigo(envio.getEstadoNotificacion(), EstadoNotificacion::getCodEstado), envio.getFechaEnvio(),
                envio.getFechaLectura(), codigo(envio.getCliente(), Cliente::getCodCliente),
                codigo(envio.getNotificacion(), Notificacion::getCodNotificacion));
        return envio;
    }

    @Override
    public void eliminar(ClienteNotificacionId id) {
        jdbc.update("DELETE FROM cliente_notificacion WHERE cod_cliente = ? AND cod_notificacion = ?",
                id.getCliente(), id.getNotificacion());
    }

    private static Object[] valores(ClienteNotificacion envio) {
        return new Object[] {codigo(envio.getCliente(), Cliente::getCodCliente),
                codigo(envio.getNotificacion(), Notificacion::getCodNotificacion),
                codigo(envio.getEstadoNotificacion(), EstadoNotificacion::getCodEstado), envio.getFechaEnvio(),
                envio.getFechaLectura()};
    }
}
