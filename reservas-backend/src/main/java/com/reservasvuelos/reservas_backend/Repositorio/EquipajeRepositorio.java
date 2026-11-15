package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Equipaje;
import com.reservasvuelos.reservas_backend.model.EquipajeId;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.enums.EstadoEquipaje;
import com.reservasvuelos.reservas_backend.model.enums.TipoEquipaje;

@Repository
public class EquipajeRepositorio extends RepositorioSql<Equipaje, EquipajeId> {

    private static final String SELECCION = """
            SELECT e.cod_tiquete, e.numero_pieza, e.codigo_etiqueta, e.tipo, e.peso_kg, e.dimensiones_cm,
                   e.costo_adicional, e.estado
            FROM equipaje e
            """;

    private static final String INSERCION = """
            INSERT INTO equipaje (cod_tiquete, numero_pieza, codigo_etiqueta, tipo, peso_kg, dimensiones_cm,
                    costo_adicional, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final TiqueteRepositorio tiqueteRepositorio;

    public EquipajeRepositorio(JdbcTemplate jdbc, TiqueteRepositorio tiqueteRepositorio) {
        super(jdbc);
        this.tiqueteRepositorio = tiqueteRepositorio;
    }

    @Override
    protected Equipaje mapear(ResultSet fila) throws SQLException {
        Equipaje equipaje = new Equipaje();
        equipaje.setTiquete(referencia(Tiquete::new, Tiquete::setCodTiquete, largo(fila, "cod_tiquete")));
        equipaje.setNumeroPieza(entero(fila, "numero_pieza"));
        equipaje.setCodigoEtiqueta(fila.getString("codigo_etiqueta"));
        equipaje.setTipo(enumerado(fila, "tipo", TipoEquipaje.class));
        equipaje.setPesoKg(fila.getBigDecimal("peso_kg"));
        equipaje.setDimensionesCm(fila.getString("dimensiones_cm"));
        equipaje.setCostoAdicional(fila.getBigDecimal("costo_adicional"));
        equipaje.setEstado(enumerado(fila, "estado", EstadoEquipaje.class));
        return equipaje;
    }

    @Override
    protected List<Equipaje> completar(List<Equipaje> equipajes) {
        cargar(equipajes, Equipaje::getTiquete, Tiquete::getCodTiquete, tiqueteRepositorio::porCodigos,
                Equipaje::setTiquete);
        return equipajes;
    }

    @Override
    public List<Equipaje> listar() {
        return consultar(SELECCION + "ORDER BY e.cod_tiquete, e.numero_pieza");
    }

    @Override
    public Optional<Equipaje> buscarPorId(EquipajeId id) {
        return consultarUno(SELECCION + "WHERE e.cod_tiquete = ? AND e.numero_pieza = ?", id.getTiquete(),
                id.getNumeroPieza());
    }

    public List<Equipaje> deTiquete(Long codTiquete) {
        return consultar(SELECCION + "WHERE e.cod_tiquete = ? ORDER BY e.numero_pieza", codTiquete);
    }

    public List<Equipaje> deReserva(Long codReserva) {
        return consultar(SELECCION + """
                JOIN tiquete t ON t.cod_tiquete = e.cod_tiquete
                WHERE t.cod_reserva = ?
                ORDER BY t.cod_tiquete, e.numero_pieza
                """, codReserva);
    }

    @Override
    public boolean existePorId(EquipajeId id) {
        return existe("SELECT EXISTS (SELECT 1 FROM equipaje WHERE cod_tiquete = ? AND numero_pieza = ?)",
                id.getTiquete(), id.getNumeroPieza());
    }

    @Override
    public Equipaje insertar(Equipaje equipaje) {
        jdbc.update(INSERCION, valores(equipaje));
        return equipaje;
    }

    public List<Equipaje> insertarTodos(List<Equipaje> equipajes) {
        jdbc.batchUpdate(INSERCION, equipajes.stream().map(EquipajeRepositorio::valores).toList());
        return equipajes;
    }

    @Override
    public Equipaje actualizar(Equipaje equipaje) {
        jdbc.update("""
                UPDATE equipaje
                SET codigo_etiqueta = ?, tipo = ?, peso_kg = ?, dimensiones_cm = ?, costo_adicional = ?, estado = ?
                WHERE cod_tiquete = ? AND numero_pieza = ?
                """, equipaje.getCodigoEtiqueta(), nombre(equipaje.getTipo()), equipaje.getPesoKg(),
                equipaje.getDimensionesCm(), equipaje.getCostoAdicional(), nombre(equipaje.getEstado()),
                codigo(equipaje.getTiquete(), Tiquete::getCodTiquete), equipaje.getNumeroPieza());
        return equipaje;
    }

    @Override
    public void eliminar(EquipajeId id) {
        jdbc.update("DELETE FROM equipaje WHERE cod_tiquete = ? AND numero_pieza = ?", id.getTiquete(),
                id.getNumeroPieza());
    }

    private static Object[] valores(Equipaje equipaje) {
        return new Object[] {codigo(equipaje.getTiquete(), Tiquete::getCodTiquete), equipaje.getNumeroPieza(),
                equipaje.getCodigoEtiqueta(), nombre(equipaje.getTipo()), equipaje.getPesoKg(),
                equipaje.getDimensionesCm(), equipaje.getCostoAdicional(), nombre(equipaje.getEstado())};
    }
}
