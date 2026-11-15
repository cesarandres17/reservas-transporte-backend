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

import com.reservasvuelos.reservas_backend.model.Notificacion;
import com.reservasvuelos.reservas_backend.model.Promocion;
import com.reservasvuelos.reservas_backend.model.TipoNotificacion;
import com.reservasvuelos.reservas_backend.model.Viaje;

@Repository
public class NotificacionRepositorio extends RepositorioSql<Notificacion, Long> {

    private static final String SELECCION = """
            SELECT n.cod_notificacion, n.titulo, n.mensaje, n.fecha_creacion, n.cod_tipo_notificacion, n.cod_promocion,
                   n.cod_viaje
            FROM notificacion n
            """;

    private final TipoNotificacionRepositorio tipoNotificacionRepositorio;
    private final PromocionRepositorio promocionRepositorio;
    private final ViajeRepositorio viajeRepositorio;

    public NotificacionRepositorio(JdbcTemplate jdbc, TipoNotificacionRepositorio tipoNotificacionRepositorio,
            PromocionRepositorio promocionRepositorio, ViajeRepositorio viajeRepositorio) {
        super(jdbc);
        this.tipoNotificacionRepositorio = tipoNotificacionRepositorio;
        this.promocionRepositorio = promocionRepositorio;
        this.viajeRepositorio = viajeRepositorio;
    }

    @Override
    protected Notificacion mapear(ResultSet fila) throws SQLException {
        Notificacion notificacion = new Notificacion();
        notificacion.setCodNotificacion(fila.getLong("cod_notificacion"));
        notificacion.setTitulo(fila.getString("titulo"));
        notificacion.setMensaje(fila.getString("mensaje"));
        notificacion.setFechaCreacion(fechaHora(fila, "fecha_creacion"));
        notificacion.setTipoNotificacion(referencia(TipoNotificacion::new, TipoNotificacion::setCodTipo,
                largo(fila, "cod_tipo_notificacion")));
        notificacion.setPromocion(referencia(Promocion::new, Promocion::setCodPromocion, largo(fila, "cod_promocion")));
        notificacion.setViaje(referencia(Viaje::new, Viaje::setCodViaje, largo(fila, "cod_viaje")));
        return notificacion;
    }

    @Override
    protected List<Notificacion> completar(List<Notificacion> notificaciones) {
        cargar(notificaciones, Notificacion::getTipoNotificacion, TipoNotificacion::getCodTipo,
                tipoNotificacionRepositorio::porCodigos, Notificacion::setTipoNotificacion);
        cargar(notificaciones, Notificacion::getPromocion, Promocion::getCodPromocion, promocionRepositorio::porCodigos,
                Notificacion::setPromocion);
        cargar(notificaciones, Notificacion::getViaje, Viaje::getCodViaje, viajeRepositorio::porCodigos,
                Notificacion::setViaje);
        return notificaciones;
    }

    @Override
    public List<Notificacion> listar() {
        return consultar(SELECCION + "ORDER BY n.cod_notificacion");
    }

    @Override
    public Optional<Notificacion> buscarPorId(Long codNotificacion) {
        return consultarUno(SELECCION + "WHERE n.cod_notificacion = ?", codNotificacion);
    }

    public Map<Long, Notificacion> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE n.cod_notificacion = ANY(?)", arreglo(codigos)),
                Notificacion::getCodNotificacion);
    }

    public List<Notificacion> programadasPorEnviar(LocalDateTime desde, LocalDateTime hasta) {
        return consultar(SELECCION + """
                LEFT JOIN viaje v ON v.cod_viaje = n.cod_viaje
                WHERE n.fecha_creacion > ? AND n.fecha_creacion <= ?
                  AND (v.cod_viaje IS NULL OR v.fecha_hora_salida > ?)
                  AND EXISTS (SELECT 1 FROM cliente_notificacion cn
                              JOIN estado_notificacion e ON e.cod_estado = cn.cod_estado_notificacion
                              WHERE cn.cod_notificacion = n.cod_notificacion
                                AND e.nombre = 'PENDIENTE' AND cn.fecha_envio IS NULL)
                ORDER BY n.fecha_creacion, n.cod_notificacion
                """, desde, hasta, hasta);
    }

    @Override
    public boolean existePorId(Long codNotificacion) {
        return existe("SELECT EXISTS (SELECT 1 FROM notificacion WHERE cod_notificacion = ?)", codNotificacion);
    }

    @Override
    public Notificacion insertar(Notificacion notificacion) {
        notificacion.setCodNotificacion(insertarDevolviendoCodigo("""
                INSERT INTO notificacion (titulo, mensaje, fecha_creacion, cod_tipo_notificacion, cod_promocion,
                        cod_viaje)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING cod_notificacion
                """, notificacion.getTitulo(), notificacion.getMensaje(), notificacion.getFechaCreacion(),
                codigo(notificacion.getTipoNotificacion(), TipoNotificacion::getCodTipo),
                codigo(notificacion.getPromocion(), Promocion::getCodPromocion),
                codigo(notificacion.getViaje(), Viaje::getCodViaje)));
        return notificacion;
    }

    @Override
    public Notificacion actualizar(Notificacion notificacion) {
        jdbc.update("""
                UPDATE notificacion
                SET titulo = ?, mensaje = ?, fecha_creacion = ?, cod_tipo_notificacion = ?, cod_promocion = ?,
                    cod_viaje = ?
                WHERE cod_notificacion = ?
                """, notificacion.getTitulo(), notificacion.getMensaje(), notificacion.getFechaCreacion(),
                codigo(notificacion.getTipoNotificacion(), TipoNotificacion::getCodTipo),
                codigo(notificacion.getPromocion(), Promocion::getCodPromocion),
                codigo(notificacion.getViaje(), Viaje::getCodViaje), notificacion.getCodNotificacion());
        return notificacion;
    }

    @Override
    public void eliminar(Long codNotificacion) {
        jdbc.update("DELETE FROM notificacion WHERE cod_notificacion = ?", codNotificacion);
    }
}
