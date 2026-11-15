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

import com.reservasvuelos.reservas_backend.model.Cliente;
import com.reservasvuelos.reservas_backend.model.Empleado;
import com.reservasvuelos.reservas_backend.model.EstadoReserva;
import com.reservasvuelos.reservas_backend.model.Promocion;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.enums.CanalReserva;

@Repository
public class ReservaRepositorio extends RepositorioSql<Reserva, Long> {

    private static final String SELECCION = """
            SELECT r.cod_reserva, r.codigo_reserva, r.fecha_reserva, r.fecha_limite_pago, r.canal, r.cod_estado_reserva,
                   r.subtotal, r.descuento, r.total, r.cod_cliente, r.cod_empleado, r.cod_promocion
            FROM reserva r
            """;

    private final EstadoReservaRepositorio estadoReservaRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;
    private final PromocionRepositorio promocionRepositorio;

    public ReservaRepositorio(JdbcTemplate jdbc, EstadoReservaRepositorio estadoReservaRepositorio,
            ClienteRepositorio clienteRepositorio, EmpleadoRepositorio empleadoRepositorio,
            PromocionRepositorio promocionRepositorio) {
        super(jdbc);
        this.estadoReservaRepositorio = estadoReservaRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
        this.promocionRepositorio = promocionRepositorio;
    }

    @Override
    protected Reserva mapear(ResultSet fila) throws SQLException {
        Reserva reserva = new Reserva();
        reserva.setCodReserva(fila.getLong("cod_reserva"));
        reserva.setCodigoReserva(fila.getString("codigo_reserva"));
        reserva.setFechaReserva(fechaHora(fila, "fecha_reserva"));
        reserva.setFechaLimitePago(fechaHora(fila, "fecha_limite_pago"));
        reserva.setCanal(enumerado(fila, "canal", CanalReserva.class));
        reserva.setEstado(referencia(EstadoReserva::new, EstadoReserva::setCodEstado,
                largo(fila, "cod_estado_reserva")));
        reserva.setSubtotal(fila.getBigDecimal("subtotal"));
        reserva.setDescuento(fila.getBigDecimal("descuento"));
        reserva.setTotal(fila.getBigDecimal("total"));
        reserva.setCliente(referencia(Cliente::new, Cliente::setCodCliente, largo(fila, "cod_cliente")));
        reserva.setEmpleado(referencia(Empleado::new, Empleado::setCodEmpleado, largo(fila, "cod_empleado")));
        reserva.setPromocion(referencia(Promocion::new, Promocion::setCodPromocion, largo(fila, "cod_promocion")));
        return reserva;
    }

    @Override
    protected List<Reserva> completar(List<Reserva> reservas) {
        cargar(reservas, Reserva::getEstado, EstadoReserva::getCodEstado, estadoReservaRepositorio::porCodigos,
                Reserva::setEstado);
        cargar(reservas, Reserva::getCliente, Cliente::getCodCliente, clienteRepositorio::porCodigos,
                Reserva::setCliente);
        cargar(reservas, Reserva::getEmpleado, Empleado::getCodEmpleado, empleadoRepositorio::porCodigos,
                Reserva::setEmpleado);
        cargar(reservas, Reserva::getPromocion, Promocion::getCodPromocion, promocionRepositorio::porCodigos,
                Reserva::setPromocion);
        return reservas;
    }

    @Override
    public List<Reserva> listar() {
        return consultar(SELECCION + "ORDER BY r.cod_reserva");
    }

    @Override
    public Optional<Reserva> buscarPorId(Long codReserva) {
        return consultarUno(SELECCION + "WHERE r.cod_reserva = ?", codReserva);
    }

    public Map<Long, Reserva> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE r.cod_reserva = ANY(?)", arreglo(codigos)),
                Reserva::getCodReserva);
    }

    public boolean existeCodigo(String codigoReserva) {
        return existe("SELECT EXISTS (SELECT 1 FROM reserva WHERE codigo_reserva = ?)", codigoReserva);
    }

    public List<Reserva> deCliente(Long codCliente) {
        return consultar(SELECCION + """
                WHERE r.cod_cliente = ?
                ORDER BY r.fecha_reserva DESC, r.cod_reserva DESC
                """, codCliente);
    }

    public List<Reserva> conEstadoYLimiteAntesDe(String estado, LocalDateTime limite) {
        return consultar(SELECCION + """
                JOIN estado_reserva e ON e.cod_estado = r.cod_estado_reserva
                WHERE e.nombre = ? AND r.fecha_limite_pago < ?
                ORDER BY r.cod_reserva
                """, estado, limite);
    }

    public void cambiarEstado(Long codReserva, Long codEstado) {
        jdbc.update("UPDATE reserva SET cod_estado_reserva = ? WHERE cod_reserva = ?", codEstado, codReserva);
    }

    @Override
    public boolean existePorId(Long codReserva) {
        return existe("SELECT EXISTS (SELECT 1 FROM reserva WHERE cod_reserva = ?)", codReserva);
    }

    @Override
    public Reserva insertar(Reserva reserva) {
        reserva.setCodReserva(insertarDevolviendoCodigo("""
                INSERT INTO reserva (codigo_reserva, fecha_reserva, fecha_limite_pago, canal, cod_estado_reserva,
                        subtotal, descuento, total, cod_cliente, cod_empleado, cod_promocion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING cod_reserva
                """, reserva.getCodigoReserva(), reserva.getFechaReserva(), reserva.getFechaLimitePago(),
                nombre(reserva.getCanal()), codigo(reserva.getEstado(), EstadoReserva::getCodEstado),
                reserva.getSubtotal(), reserva.getDescuento(), reserva.getTotal(),
                codigo(reserva.getCliente(), Cliente::getCodCliente),
                codigo(reserva.getEmpleado(), Empleado::getCodEmpleado),
                codigo(reserva.getPromocion(), Promocion::getCodPromocion)));
        return reserva;
    }

    @Override
    public Reserva actualizar(Reserva reserva) {
        jdbc.update("""
                UPDATE reserva
                SET codigo_reserva = ?, fecha_reserva = ?, fecha_limite_pago = ?, canal = ?, cod_estado_reserva = ?,
                    subtotal = ?, descuento = ?, total = ?, cod_cliente = ?, cod_empleado = ?, cod_promocion = ?
                WHERE cod_reserva = ?
                """, reserva.getCodigoReserva(), reserva.getFechaReserva(), reserva.getFechaLimitePago(),
                nombre(reserva.getCanal()), codigo(reserva.getEstado(), EstadoReserva::getCodEstado),
                reserva.getSubtotal(), reserva.getDescuento(), reserva.getTotal(),
                codigo(reserva.getCliente(), Cliente::getCodCliente),
                codigo(reserva.getEmpleado(), Empleado::getCodEmpleado),
                codigo(reserva.getPromocion(), Promocion::getCodPromocion), reserva.getCodReserva());
        return reserva;
    }

    @Override
    public void eliminar(Long codReserva) {
        jdbc.update("DELETE FROM reserva WHERE cod_reserva = ?", codReserva);
    }
}
