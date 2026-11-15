package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Factura;
import com.reservasvuelos.reservas_backend.model.Reserva;

@Repository
public class FacturaRepositorio extends RepositorioSql<Factura, Long> {

    private static final String SELECCION = """
            SELECT f.cod_reserva, f.numero_factura, f.fecha_emision
            FROM factura f
            """;

    private final ReservaRepositorio reservaRepositorio;

    public FacturaRepositorio(JdbcTemplate jdbc, ReservaRepositorio reservaRepositorio) {
        super(jdbc);
        this.reservaRepositorio = reservaRepositorio;
    }

    @Override
    protected Factura mapear(ResultSet fila) throws SQLException {
        Factura factura = new Factura();
        factura.setCodReserva(fila.getLong("cod_reserva"));
        factura.setReserva(referencia(Reserva::new, Reserva::setCodReserva, factura.getCodReserva()));
        factura.setNumeroFactura(fila.getString("numero_factura"));
        factura.setFechaEmision(fechaHora(fila, "fecha_emision"));
        return factura;
    }

    @Override
    protected List<Factura> completar(List<Factura> facturas) {
        cargar(facturas, Factura::getReserva, Reserva::getCodReserva, reservaRepositorio::porCodigos,
                Factura::setReserva);
        return facturas;
    }

    @Override
    public List<Factura> listar() {
        return consultar(SELECCION + "ORDER BY f.cod_reserva");
    }

    @Override
    public Optional<Factura> buscarPorId(Long codReserva) {
        return consultarUno(SELECCION + "WHERE f.cod_reserva = ?", codReserva);
    }

    public int ultimoConsecutivo() {
        Integer ultimo = jdbc.queryForObject("""
                SELECT COALESCE(MAX(CAST(SUBSTRING(numero_factura FROM 4) AS INTEGER)), 0)
                FROM factura
                WHERE numero_factura ~ '^FE-[0-9]{6}$'
                """, Integer.class);
        return ultimo == null ? 0 : ultimo;
    }

    @Override
    public boolean existePorId(Long codReserva) {
        return existe("SELECT EXISTS (SELECT 1 FROM factura WHERE cod_reserva = ?)", codReserva);
    }

    @Override
    public Factura insertar(Factura factura) {
        factura.setCodReserva(factura.getReserva().getCodReserva());
        jdbc.update("""
                INSERT INTO factura (cod_reserva, numero_factura, fecha_emision)
                VALUES (?, ?, ?)
                """, factura.getCodReserva(), factura.getNumeroFactura(), factura.getFechaEmision());
        return factura;
    }

    @Override
    public Factura actualizar(Factura factura) {
        jdbc.update("""
                UPDATE factura
                SET numero_factura = ?, fecha_emision = ?
                WHERE cod_reserva = ?
                """, factura.getNumeroFactura(), factura.getFechaEmision(), factura.getCodReserva());
        return factura;
    }

    @Override
    public void eliminar(Long codReserva) {
        jdbc.update("DELETE FROM factura WHERE cod_reserva = ?", codReserva);
    }
}
