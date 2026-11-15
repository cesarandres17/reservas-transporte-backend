package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.EstadoPago;
import com.reservasvuelos.reservas_backend.model.MetodoPago;
import com.reservasvuelos.reservas_backend.model.Pago;
import com.reservasvuelos.reservas_backend.model.Reserva;

@Repository
public class PagoRepositorio extends RepositorioSql<Pago, Long> {

    private static final String SELECCION = """
            SELECT p.cod_pago, p.fecha_pago, p.monto, p.cuotas, p.referencia_transaccion, p.cod_estado_pago,
                   p.cod_reserva, p.cod_metodo
            FROM pago p
            """;

    private final EstadoPagoRepositorio estadoPagoRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final MetodoPagoRepositorio metodoPagoRepositorio;

    public PagoRepositorio(JdbcTemplate jdbc, EstadoPagoRepositorio estadoPagoRepositorio,
            ReservaRepositorio reservaRepositorio, MetodoPagoRepositorio metodoPagoRepositorio) {
        super(jdbc);
        this.estadoPagoRepositorio = estadoPagoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.metodoPagoRepositorio = metodoPagoRepositorio;
    }

    @Override
    protected Pago mapear(ResultSet fila) throws SQLException {
        Pago pago = new Pago();
        pago.setCodPago(fila.getLong("cod_pago"));
        pago.setFechaPago(fechaHora(fila, "fecha_pago"));
        pago.setMonto(fila.getBigDecimal("monto"));
        pago.setCuotas(entero(fila, "cuotas"));
        pago.setReferenciaTransaccion(fila.getString("referencia_transaccion"));
        pago.setEstado(referencia(EstadoPago::new, EstadoPago::setCodEstado, largo(fila, "cod_estado_pago")));
        pago.setReserva(referencia(Reserva::new, Reserva::setCodReserva, largo(fila, "cod_reserva")));
        pago.setMetodoPago(referencia(MetodoPago::new, MetodoPago::setCodMetodo, largo(fila, "cod_metodo")));
        return pago;
    }

    @Override
    protected List<Pago> completar(List<Pago> pagos) {
        cargar(pagos, Pago::getEstado, EstadoPago::getCodEstado, estadoPagoRepositorio::porCodigos, Pago::setEstado);
        cargar(pagos, Pago::getReserva, Reserva::getCodReserva, reservaRepositorio::porCodigos, Pago::setReserva);
        cargar(pagos, Pago::getMetodoPago, MetodoPago::getCodMetodo, metodoPagoRepositorio::porCodigos,
                Pago::setMetodoPago);
        return pagos;
    }

    @Override
    public List<Pago> listar() {
        return consultar(SELECCION + "ORDER BY p.cod_pago");
    }

    @Override
    public Optional<Pago> buscarPorId(Long codPago) {
        return consultarUno(SELECCION + "WHERE p.cod_pago = ?", codPago);
    }

    public Map<Long, Pago> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE p.cod_pago = ANY(?)", arreglo(codigos)), Pago::getCodPago);
    }

    public List<Pago> deReserva(Long codReserva) {
        return consultar(SELECCION + "WHERE p.cod_reserva = ? ORDER BY p.fecha_pago, p.cod_pago", codReserva);
    }

    public boolean existeReferencia(String referenciaTransaccion) {
        return existe("SELECT EXISTS (SELECT 1 FROM pago WHERE referencia_transaccion = ?)", referenciaTransaccion);
    }

    public void cambiarEstado(Long codPago, Long codEstado) {
        jdbc.update("UPDATE pago SET cod_estado_pago = ? WHERE cod_pago = ?", codEstado, codPago);
    }

    @Override
    public boolean existePorId(Long codPago) {
        return existe("SELECT EXISTS (SELECT 1 FROM pago WHERE cod_pago = ?)", codPago);
    }

    @Override
    public Pago insertar(Pago pago) {
        pago.setCodPago(insertarDevolviendoCodigo("""
                INSERT INTO pago (fecha_pago, monto, cuotas, referencia_transaccion, cod_estado_pago, cod_reserva,
                        cod_metodo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING cod_pago
                """, pago.getFechaPago(), pago.getMonto(), pago.getCuotas(), pago.getReferenciaTransaccion(),
                codigo(pago.getEstado(), EstadoPago::getCodEstado), codigo(pago.getReserva(), Reserva::getCodReserva),
                codigo(pago.getMetodoPago(), MetodoPago::getCodMetodo)));
        return pago;
    }

    @Override
    public Pago actualizar(Pago pago) {
        jdbc.update("""
                UPDATE pago
                SET fecha_pago = ?, monto = ?, cuotas = ?, referencia_transaccion = ?, cod_estado_pago = ?,
                    cod_reserva = ?, cod_metodo = ?
                WHERE cod_pago = ?
                """, pago.getFechaPago(), pago.getMonto(), pago.getCuotas(), pago.getReferenciaTransaccion(),
                codigo(pago.getEstado(), EstadoPago::getCodEstado), codigo(pago.getReserva(), Reserva::getCodReserva),
                codigo(pago.getMetodoPago(), MetodoPago::getCodMetodo), pago.getCodPago());
        return pago;
    }

    @Override
    public void eliminar(Long codPago) {
        jdbc.update("DELETE FROM pago WHERE cod_pago = ?", codPago);
    }
}
