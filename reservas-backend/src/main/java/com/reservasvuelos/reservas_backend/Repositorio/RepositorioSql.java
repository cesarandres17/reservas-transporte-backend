package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public abstract class RepositorioSql<T, ID> {

    protected final JdbcTemplate jdbc;
    private final RowMapper<T> mapeador = (fila, numero) -> mapear(fila);

    protected RepositorioSql(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public abstract List<T> listar();

    public abstract Optional<T> buscarPorId(ID id);

    public abstract boolean existePorId(ID id);

    public abstract T insertar(T entidad);

    public abstract T actualizar(T entidad);

    public abstract void eliminar(ID id);

    protected abstract T mapear(ResultSet fila) throws SQLException;

    protected List<T> completar(List<T> lista) {
        return lista;
    }

    protected List<T> consultar(String sql, Object... parametros) {
        return completar(jdbc.query(sql, mapeador, parametros));
    }

    protected Optional<T> consultarUno(String sql, Object... parametros) {
        return consultar(sql, parametros).stream().findFirst();
    }

    protected boolean existe(String sql, Object... parametros) {
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, parametros));
    }

    protected Long insertarDevolviendoCodigo(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }

    protected <R, K> void cargar(List<T> lista, Function<T, R> obtener, Function<R, K> clave,
            Function<Collection<K>, Map<K, R>> buscar, BiConsumer<T, R> asignar) {
        Set<K> claves = new LinkedHashSet<>();
        for (T entidad : lista) {
            R referencia = obtener.apply(entidad);
            if (referencia != null) {
                claves.add(clave.apply(referencia));
            }
        }
        if (claves.isEmpty()) {
            return;
        }
        Map<K, R> encontrados = buscar.apply(claves);
        for (T entidad : lista) {
            R referencia = obtener.apply(entidad);
            if (referencia != null) {
                asignar.accept(entidad, encontrados.get(clave.apply(referencia)));
            }
        }
    }

    protected static <E, K> Map<K, E> porClave(List<E> lista, Function<E, K> clave) {
        Map<K, E> mapa = new LinkedHashMap<>();
        lista.forEach(elemento -> mapa.put(clave.apply(elemento), elemento));
        return mapa;
    }

    protected static Object arreglo(Collection<Long> codigos) {
        return codigos.toArray(Long[]::new);
    }

    protected static Object textos(Collection<String> valores) {
        return valores.toArray(String[]::new);
    }

    protected static <R> R referencia(Supplier<R> crear, BiConsumer<R, Long> asignarCodigo, Long codigo) {
        if (codigo == null) {
            return null;
        }
        R referencia = crear.get();
        asignarCodigo.accept(referencia, codigo);
        return referencia;
    }

    protected static <R> Long codigo(R referencia, Function<R, Long> codigo) {
        return referencia == null ? null : codigo.apply(referencia);
    }

    protected static Long largo(ResultSet fila, String columna) throws SQLException {
        long valor = fila.getLong(columna);
        return fila.wasNull() ? null : valor;
    }

    protected static Integer entero(ResultSet fila, String columna) throws SQLException {
        int valor = fila.getInt(columna);
        return fila.wasNull() ? null : valor;
    }

    protected static Boolean logico(ResultSet fila, String columna) throws SQLException {
        boolean valor = fila.getBoolean(columna);
        return fila.wasNull() ? null : valor;
    }

    protected static LocalDateTime fechaHora(ResultSet fila, String columna) throws SQLException {
        return fila.getObject(columna, LocalDateTime.class);
    }

    protected static LocalDate fecha(ResultSet fila, String columna) throws SQLException {
        return fila.getObject(columna, LocalDate.class);
    }

    protected static <E extends Enum<E>> E enumerado(ResultSet fila, String columna, Class<E> tipo)
            throws SQLException {
        String valor = fila.getString(columna);
        return valor == null ? null : Enum.valueOf(tipo, valor);
    }

    protected static String nombre(Enum<?> valor) {
        return valor == null ? null : valor.name();
    }
}
