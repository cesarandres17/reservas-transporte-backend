package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Checkin;
import com.reservasvuelos.reservas_backend.model.Empleado;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.enums.CanalCheckin;

@Repository
public class CheckinRepositorio extends RepositorioSql<Checkin, Long> {

    private static final String SELECCION = """
            SELECT c.cod_tiquete, c.fecha_hora, c.canal, c.codigo_pase_abordar, c.grupo_abordaje, c.cod_empleado
            FROM checkin c
            """;

    private final TiqueteRepositorio tiqueteRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    public CheckinRepositorio(JdbcTemplate jdbc, TiqueteRepositorio tiqueteRepositorio,
            EmpleadoRepositorio empleadoRepositorio) {
        super(jdbc);
        this.tiqueteRepositorio = tiqueteRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
    }

    @Override
    protected Checkin mapear(ResultSet fila) throws SQLException {
        Checkin checkin = new Checkin();
        checkin.setCodTiquete(fila.getLong("cod_tiquete"));
        checkin.setTiquete(referencia(Tiquete::new, Tiquete::setCodTiquete, checkin.getCodTiquete()));
        checkin.setFechaHora(fechaHora(fila, "fecha_hora"));
        checkin.setCanal(enumerado(fila, "canal", CanalCheckin.class));
        checkin.setCodigoPaseAbordar(fila.getString("codigo_pase_abordar"));
        checkin.setGrupoAbordaje(fila.getString("grupo_abordaje"));
        checkin.setEmpleado(referencia(Empleado::new, Empleado::setCodEmpleado, largo(fila, "cod_empleado")));
        return checkin;
    }

    @Override
    protected List<Checkin> completar(List<Checkin> checkins) {
        cargar(checkins, Checkin::getTiquete, Tiquete::getCodTiquete, tiqueteRepositorio::porCodigos,
                Checkin::setTiquete);
        cargar(checkins, Checkin::getEmpleado, Empleado::getCodEmpleado, empleadoRepositorio::porCodigos,
                Checkin::setEmpleado);
        return checkins;
    }

    @Override
    public List<Checkin> listar() {
        return consultar(SELECCION + "ORDER BY c.cod_tiquete");
    }

    @Override
    public Optional<Checkin> buscarPorId(Long codTiquete) {
        return consultarUno(SELECCION + "WHERE c.cod_tiquete = ?", codTiquete);
    }

    public List<Checkin> deReserva(Long codReserva) {
        return consultar(SELECCION + """
                JOIN tiquete t ON t.cod_tiquete = c.cod_tiquete
                WHERE t.cod_reserva = ?
                ORDER BY t.cod_tiquete
                """, codReserva);
    }

    public boolean existeEnReserva(Long codReserva) {
        return existe("""
                SELECT EXISTS (SELECT 1 FROM checkin c
                               JOIN tiquete t ON t.cod_tiquete = c.cod_tiquete
                               WHERE t.cod_reserva = ?)
                """, codReserva);
    }

    @Override
    public boolean existePorId(Long codTiquete) {
        return existe("SELECT EXISTS (SELECT 1 FROM checkin WHERE cod_tiquete = ?)", codTiquete);
    }

    @Override
    public Checkin insertar(Checkin checkin) {
        checkin.setCodTiquete(checkin.getTiquete().getCodTiquete());
        jdbc.update("""
                INSERT INTO checkin (cod_tiquete, fecha_hora, canal, codigo_pase_abordar, grupo_abordaje,
                        cod_empleado)
                VALUES (?, ?, ?, ?, ?, ?)
                """, checkin.getCodTiquete(), checkin.getFechaHora(), nombre(checkin.getCanal()),
                checkin.getCodigoPaseAbordar(), checkin.getGrupoAbordaje(),
                codigo(checkin.getEmpleado(), Empleado::getCodEmpleado));
        return checkin;
    }

    @Override
    public Checkin actualizar(Checkin checkin) {
        jdbc.update("""
                UPDATE checkin
                SET fecha_hora = ?, canal = ?, codigo_pase_abordar = ?, grupo_abordaje = ?, cod_empleado = ?
                WHERE cod_tiquete = ?
                """, checkin.getFechaHora(), nombre(checkin.getCanal()), checkin.getCodigoPaseAbordar(),
                checkin.getGrupoAbordaje(), codigo(checkin.getEmpleado(), Empleado::getCodEmpleado),
                checkin.getCodTiquete());
        return checkin;
    }

    @Override
    public void eliminar(Long codTiquete) {
        jdbc.update("DELETE FROM checkin WHERE cod_tiquete = ?", codTiquete);
    }
}
