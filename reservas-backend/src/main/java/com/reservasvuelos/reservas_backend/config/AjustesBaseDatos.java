package com.reservasvuelos.reservas_backend.config;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class AjustesBaseDatos implements ApplicationRunner {

    public static final String INDICE_PASAJERO_VIGENTE = "uk_tiquete_pasajero_vigente";

    private static final Logger log = LoggerFactory.getLogger(AjustesBaseDatos.class);

    private final JdbcTemplate jdbcTemplate;

    public AjustesBaseDatos(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        quitarRestriccionPasajeroPorViaje();
        crearIndicePasajeroVigente();
    }

    private void quitarRestriccionPasajeroPorViaje() {
        List<String> restricciones = jdbcTemplate.queryForList("""
                select con.conname from pg_constraint con
                join pg_class tabla on tabla.oid = con.conrelid
                where tabla.relname = 'tiquete' and con.contype = 'u'
                  and (select array_agg(columna.attname::text order by columna.attname)
                       from unnest(con.conkey) as llave(numero)
                       join pg_attribute columna on columna.attrelid = con.conrelid and columna.attnum = llave.numero)
                      = array['cod_pasajero', 'cod_viaje']
                """, String.class);
        for (String restriccion : restricciones) {
            jdbcTemplate.execute("alter table tiquete drop constraint \"" + restriccion.replace("\"", "") + "\"");
            log.info("Se quito la restriccion {} para que un pasajero pueda volver a comprar un viaje que cancelo",
                    restriccion);
        }
    }

    private void crearIndicePasajeroVigente() {
        Boolean existe = jdbcTemplate.queryForObject(
                "select exists (select 1 from pg_indexes where tablename = 'tiquete' and indexname = ?)", Boolean.class,
                INDICE_PASAJERO_VIGENTE);
        if (Boolean.TRUE.equals(existe)) {
            return;
        }
        List<Long> vigentes = jdbcTemplate.queryForList(
                "select cod_estado from estado_tiquete where nombre in ('EMITIDO', 'USADO') order by cod_estado",
                Long.class);
        if (vigentes.size() < 2) {
            log.info("Todavia no estan los estados de tiquete; el indice {} se crea en el siguiente arranque",
                    INDICE_PASAJERO_VIGENTE);
            return;
        }
        String estados = vigentes.stream().map(String::valueOf).collect(Collectors.joining(", "));
        try {
            jdbcTemplate.execute("create unique index " + INDICE_PASAJERO_VIGENTE
                    + " on tiquete (cod_viaje, cod_pasajero) where cod_estado_tiquete in (" + estados + ")");
            log.info("Se creo el indice {}: un pasajero solo puede tener un tiquete vigente por viaje",
                    INDICE_PASAJERO_VIGENTE);
        } catch (DataAccessException e) {
            log.warn("No se pudo crear el indice {}: {}", INDICE_PASAJERO_VIGENTE, e.getMostSpecificCause().getMessage());
        }
    }
}
