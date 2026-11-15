package com.reservasvuelos.reservas_backend.servicios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.ReporteRepositorio;
import com.reservasvuelos.reservas_backend.dto.ReporteDTO;
import com.reservasvuelos.reservas_backend.dto.ReporteInfoDTO;
import com.reservasvuelos.reservas_backend.servicios.DefinicionReporte.Grafico;

@Service
public class ReporteServicio {

    private static final String SIMPLE = "Simple";
    private static final String INTERMEDIO = "Intermedio";
    private static final String SUBCONSULTAS = "Con subconsultas";

    private static final List<DefinicionReporte> REPORTES = List.of(
            new DefinicionReporte(1, "Calendario de promociones", SIMPLE,
                    "Todas las promociones con su descuento, sus fechas, cuántos días duran y si a la fecha de hoy están vencidas, vigentes o programadas.",
                    """
                    SELECT p.codigo AS "Código",
                           p.descripcion AS "Descripción",
                           p.porcentaje_descuento AS "Descuento (%)",
                           p.fecha_inicio AS "Inicio",
                           p.fecha_fin AS "Fin",
                           p.fecha_fin - p.fecha_inicio + 1 AS "Días",
                           CASE
                               WHEN p.fecha_fin < CURRENT_DATE THEN 'Vencida'
                               WHEN p.fecha_inicio > CURRENT_DATE THEN 'Programada'
                               ELSE 'Vigente'
                           END AS "Estado"
                    FROM promocion p
                    ORDER BY p.fecha_inicio
                    """,
                    null, null, null, List.of()),
            new DefinicionReporte(2, "Flota de vehículos", SIMPLE,
                    "Vehículos registrados ordenados de mayor a menor capacidad, con la antigüedad calculada a partir del año de fabricación y su estado actual.",
                    """
                    SELECT v.matricula AS "Matrícula",
                           v.fabricante AS "Fabricante",
                           v.modelo AS "Modelo",
                           v.capacidad AS "Capacidad",
                           v.anio_fabricacion AS "Año",
                           EXTRACT(YEAR FROM CURRENT_DATE)::int - v.anio_fabricacion AS "Antigüedad (años)",
                           v.estado AS "Estado"
                    FROM vehiculo v
                    ORDER BY v.capacidad DESC, v.matricula
                    """,
                    null, null, null, List.of("Capacidad")),
            new DefinicionReporte(3, "Usuarios registrados por mes", SIMPLE,
                    "Cuántas cuentas se crearon cada mes y cuántas de ellas siguen activas, agrupando la tabla de usuarios por el mes de registro.",
                    """
                    SELECT to_char(date_trunc('month', u.fecha_registro), 'YYYY-MM') AS "Mes",
                           COUNT(*) AS "Registrados",
                           COUNT(*) FILTER (WHERE u.estado = 'ACTIVO') AS "Activos",
                           COUNT(*) FILTER (WHERE u.estado <> 'ACTIVO') AS "Inactivos o bloqueados"
                    FROM usuario u
                    GROUP BY date_trunc('month', u.fecha_registro)
                    ORDER BY date_trunc('month', u.fecha_registro)
                    """,
                    null, null, null, List.of("Registrados", "Activos", "Inactivos o bloqueados")),
            new DefinicionReporte(4, "Ingresos por mes", INTERMEDIO,
                    "Pagos aprobados de los últimos 12 meses: cuántos hubo, cuánto se recaudó y el valor promedio de cada pago.",
                    """
                    SELECT to_char(date_trunc('month', p.fecha_pago), 'YYYY-MM') AS "Mes",
                           COUNT(*) AS "Pagos",
                           SUM(p.monto) AS "Ingresos ($)",
                           ROUND(AVG(p.monto), 0) AS "Pago promedio ($)"
                    FROM pago p
                    JOIN estado_pago ep ON ep.cod_estado = p.cod_estado_pago
                    WHERE ep.nombre = 'APROBADO'
                      AND p.fecha_pago >= date_trunc('month', CURRENT_DATE) - INTERVAL '11 months'
                    GROUP BY date_trunc('month', p.fecha_pago)
                    ORDER BY date_trunc('month', p.fecha_pago)
                    """,
                    Grafico.BARRAS, "Mes", "Ingresos ($)", List.of("Pagos", "Ingresos ($)")),
            new DefinicionReporte(5, "Ingresos por método de pago", INTERMEDIO,
                    "Cuánto se ha recaudado con cada método de pago, la comisión que cobra cada uno y su participación en el total de pagos aprobados.",
                    """
                    SELECT mp.nombre AS "Método de pago",
                           mp.tipo AS "Tipo",
                           COUNT(*) AS "Pagos",
                           SUM(p.monto) AS "Ingresos ($)",
                           ROUND(SUM(p.monto * mp.comision_pct / 100), 0) AS "Comisión ($)",
                           ROUND(100.0 * SUM(p.monto) / SUM(SUM(p.monto)) OVER (), 1) AS "Participación (%)"
                    FROM pago p
                    JOIN metodo_pago mp ON mp.cod_metodo = p.cod_metodo
                    JOIN estado_pago ep ON ep.cod_estado = p.cod_estado_pago
                    WHERE ep.nombre = 'APROBADO'
                    GROUP BY mp.cod_metodo, mp.nombre, mp.tipo
                    ORDER BY SUM(p.monto) DESC
                    """,
                    Grafico.TORTA, "Método de pago", "Ingresos ($)",
                    List.of("Pagos", "Ingresos ($)", "Comisión ($)", "Participación (%)")),
            new DefinicionReporte(6, "Pasajeros por ruta", INTERMEDIO,
                    "Pasajeros con tiquete emitido o usado en cada ruta, con su origen y destino, el número de viajes y lo vendido. Solo rutas con 10 o más pasajeros.",
                    """
                    SELECT r.codigo AS "Ruta",
                           co.nombre || ' - ' || cd.nombre AS "Trayecto",
                           tt.nombre AS "Transporte",
                           COUNT(DISTINCT v.cod_viaje) AS "Viajes",
                           COUNT(t.cod_tiquete) AS "Pasajeros",
                           SUM(t.precio_pagado) AS "Ventas ($)"
                    FROM tiquete t
                    JOIN estado_tiquete et ON et.cod_estado = t.cod_estado_tiquete
                    JOIN viaje v ON v.cod_viaje = t.cod_viaje
                    JOIN ruta r ON r.cod_ruta = v.cod_ruta
                    JOIN terminal tor ON tor.cod_terminal = r.cod_terminal_origen
                    JOIN terminal tde ON tde.cod_terminal = r.cod_terminal_destino
                    JOIN ciudad co ON co.cod_ciudad = tor.cod_ciudad
                    JOIN ciudad cd ON cd.cod_ciudad = tde.cod_ciudad
                    JOIN tipo_transporte tt ON tt.cod_tipo = tor.cod_tipo
                    WHERE et.nombre IN ('EMITIDO', 'USADO')
                    GROUP BY r.cod_ruta, r.codigo, co.nombre, cd.nombre, tt.nombre
                    HAVING COUNT(t.cod_tiquete) >= 10
                    ORDER BY COUNT(t.cod_tiquete) DESC
                    """,
                    Grafico.BARRAS_HORIZONTALES, "Ruta", "Pasajeros", List.of("Viajes", "Pasajeros", "Ventas ($)")),
            new DefinicionReporte(7, "Clientes frecuentes", INTERMEDIO,
                    "Clientes con 10 o más reservas pagadas, ordenados por lo que han gastado, con su ciudad y la fecha de su última reserva.",
                    """
                    SELECT u.primer_nombre || ' ' || u.primer_apellido AS "Cliente",
                           u.email AS "Correo",
                           ci.nombre AS "Ciudad",
                           COUNT(r.cod_reserva) AS "Reservas pagadas",
                           SUM(r.total) AS "Total gastado ($)",
                           MAX(r.fecha_reserva) AS "Última reserva"
                    FROM reserva r
                    JOIN estado_reserva er ON er.cod_estado = r.cod_estado_reserva
                    JOIN cliente c ON c.cod_cliente = r.cod_cliente
                    JOIN usuario u ON u.cod_usuario = c.cod_cliente
                    LEFT JOIN ciudad ci ON ci.cod_ciudad = u.cod_ciudad
                    WHERE er.nombre = 'PAGADA'
                    GROUP BY u.cod_usuario, u.primer_nombre, u.primer_apellido, u.email, ci.nombre
                    HAVING COUNT(r.cod_reserva) >= 10
                    ORDER BY SUM(r.total) DESC
                    """,
                    null, null, null, List.of()),
            new DefinicionReporte(8, "Viajes con más pasajeros que el promedio de su ruta", SUBCONSULTAS,
                    "Viajes finalizados en los que viajaron más pasajeros que el promedio de los viajes finalizados de la misma ruta. El conteo y el promedio salen de subconsultas correlacionadas.",
                    """
                    SELECT v.codigo_viaje AS "Viaje",
                           r.codigo AS "Ruta",
                           v.fecha_hora_salida AS "Salida",
                           ve.matricula AS "Vehículo",
                           x.pasajeros AS "Pasajeros",
                           ROUND(x.promedio_ruta, 1) AS "Promedio de la ruta",
                           ROUND(x.pasajeros - x.promedio_ruta, 1) AS "Diferencia"
                    FROM (SELECT v1.cod_viaje,
                                 (SELECT COUNT(*)
                                  FROM tiquete t
                                  WHERE t.cod_viaje = v1.cod_viaje
                                    AND t.cod_estado_tiquete = (SELECT cod_estado FROM estado_tiquete WHERE nombre = 'USADO')
                                 ) AS pasajeros,
                                 (SELECT AVG(por_viaje.total)
                                  FROM (SELECT COUNT(t2.cod_tiquete) AS total
                                        FROM viaje v2
                                        LEFT JOIN tiquete t2
                                               ON t2.cod_viaje = v2.cod_viaje
                                              AND t2.cod_estado_tiquete = (SELECT cod_estado
                                                                           FROM estado_tiquete
                                                                           WHERE nombre = 'USADO')
                                        WHERE v2.cod_ruta = v1.cod_ruta
                                          AND v2.cod_estado_viaje = v1.cod_estado_viaje
                                        GROUP BY v2.cod_viaje) AS por_viaje
                                 ) AS promedio_ruta
                          FROM viaje v1
                          WHERE v1.cod_estado_viaje = (SELECT cod_estado FROM estado_viaje WHERE nombre = 'FINALIZADO')) x
                    JOIN viaje v ON v.cod_viaje = x.cod_viaje
                    JOIN ruta r ON r.cod_ruta = v.cod_ruta
                    JOIN vehiculo ve ON ve.cod_vehiculo = v.cod_vehiculo
                    WHERE x.pasajeros > x.promedio_ruta
                    ORDER BY r.codigo, v.fecha_hora_salida
                    """,
                    null, null, null, List.of()),
            new DefinicionReporte(9, "Rutas con ingresos por encima del promedio", SUBCONSULTAS,
                    "Rutas cuyas ventas en tiquetes emitidos o usados superan el promedio de ventas por ruta. El promedio se calcula con una subconsulta dentro del HAVING.",
                    """
                    SELECT r.codigo AS "Ruta",
                           co.nombre || ' - ' || cd.nombre AS "Trayecto",
                           COUNT(t.cod_tiquete) AS "Tiquetes",
                           SUM(t.precio_pagado) AS "Ingresos ($)",
                           ROUND((SELECT AVG(por_ruta.ingreso)
                                  FROM (SELECT SUM(t2.precio_pagado) AS ingreso
                                        FROM tiquete t2
                                        JOIN viaje v2 ON v2.cod_viaje = t2.cod_viaje
                                        WHERE t2.cod_estado_tiquete IN (SELECT cod_estado
                                                                        FROM estado_tiquete
                                                                        WHERE nombre IN ('EMITIDO', 'USADO'))
                                        GROUP BY v2.cod_ruta) AS por_ruta), 0) AS "Promedio por ruta ($)"
                    FROM tiquete t
                    JOIN viaje v ON v.cod_viaje = t.cod_viaje
                    JOIN ruta r ON r.cod_ruta = v.cod_ruta
                    JOIN terminal tor ON tor.cod_terminal = r.cod_terminal_origen
                    JOIN terminal tde ON tde.cod_terminal = r.cod_terminal_destino
                    JOIN ciudad co ON co.cod_ciudad = tor.cod_ciudad
                    JOIN ciudad cd ON cd.cod_ciudad = tde.cod_ciudad
                    WHERE t.cod_estado_tiquete IN (SELECT cod_estado FROM estado_tiquete WHERE nombre IN ('EMITIDO', 'USADO'))
                    GROUP BY r.cod_ruta, r.codigo, co.nombre, cd.nombre
                    HAVING SUM(t.precio_pagado) > (SELECT AVG(por_ruta.ingreso)
                                                   FROM (SELECT SUM(t2.precio_pagado) AS ingreso
                                                         FROM tiquete t2
                                                         JOIN viaje v2 ON v2.cod_viaje = t2.cod_viaje
                                                         WHERE t2.cod_estado_tiquete IN (SELECT cod_estado
                                                                                         FROM estado_tiquete
                                                                                         WHERE nombre IN ('EMITIDO', 'USADO'))
                                                         GROUP BY v2.cod_ruta) AS por_ruta)
                    ORDER BY SUM(t.precio_pagado) DESC
                    """,
                    null, null, null, List.of()),
            new DefinicionReporte(10, "Clientes sin compras en los últimos 90 días", SUBCONSULTAS,
                    "Clientes activos que ya compraron alguna vez pero no han hecho reservas en los últimos 90 días, para invitarlos con una promoción. Usa EXISTS, NOT EXISTS y subconsultas correlacionadas.",
                    """
                    SELECT u.primer_nombre || ' ' || u.primer_apellido AS "Cliente",
                           u.email AS "Correo",
                           u.telefono AS "Teléfono",
                           (SELECT COUNT(*) FROM reserva r WHERE r.cod_cliente = c.cod_cliente) AS "Reservas",
                           (SELECT MAX(r.fecha_reserva) FROM reserva r WHERE r.cod_cliente = c.cod_cliente) AS "Última reserva",
                           CURRENT_DATE - (SELECT MAX(r.fecha_reserva)
                                           FROM reserva r
                                           WHERE r.cod_cliente = c.cod_cliente)::date AS "Días sin comprar"
                    FROM cliente c
                    JOIN usuario u ON u.cod_usuario = c.cod_cliente
                    WHERE u.estado = 'ACTIVO'
                      AND EXISTS (SELECT 1 FROM reserva r WHERE r.cod_cliente = c.cod_cliente)
                      AND NOT EXISTS (SELECT 1
                                        FROM reserva r
                                       WHERE r.cod_cliente = c.cod_cliente
                                         AND r.fecha_reserva >= CURRENT_DATE - INTERVAL '90 days')
                    ORDER BY 6 DESC, 1
                    """,
                    null, null, null, List.of()));

    private final ReporteRepositorio reporteRepositorio;

    public ReporteServicio(ReporteRepositorio reporteRepositorio) {
        this.reporteRepositorio = reporteRepositorio;
    }

    public List<ReporteInfoDTO> listar() {
        return REPORTES.stream()
                .map(r -> new ReporteInfoDTO(r.numero(), r.titulo(), r.tipo(), r.descripcion(), nombreGrafico(r)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReporteDTO ejecutar(int numero) {
        DefinicionReporte definicion = definicion(numero);
        ReporteRepositorio.Resultado resultado = reporteRepositorio.consultar(definicion.sql());
        return new ReporteDTO(definicion.numero(), definicion.titulo(), definicion.tipo(), definicion.descripcion(),
                nombreGrafico(definicion), definicion.columnaEtiqueta(), definicion.columnaValor(),
                definicion.columnasTotal(), definicion.sql().strip(), LocalDateTime.now(), resultado.columnas(),
                resultado.filas());
    }

    private DefinicionReporte definicion(int numero) {
        return REPORTES.stream()
                .filter(r -> r.numero() == numero)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el reporte " + numero));
    }

    private String nombreGrafico(DefinicionReporte definicion) {
        return definicion.grafico() == null ? null : definicion.grafico().name();
    }
}
