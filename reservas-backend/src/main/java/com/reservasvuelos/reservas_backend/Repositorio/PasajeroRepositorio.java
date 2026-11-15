package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Pais;
import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.TipoDocumento;
import com.reservasvuelos.reservas_backend.model.enums.TipoPasajero;

@Repository
public class PasajeroRepositorio extends RepositorioSql<Pasajero, Long> {

    private static final String SELECCION = """
            SELECT p.cod_pasajero, p.cod_tipo_documento, p.numero_documento, p.primer_nombre, p.segundo_nombre,
                   p.primer_apellido, p.segundo_apellido, p.fecha_nacimiento, p.genero, p.tipo_pasajero, p.cod_pais
            FROM pasajero p
            """;

    private final TipoDocumentoRepositorio tipoDocumentoRepositorio;
    private final PaisRepositorio paisRepositorio;

    public PasajeroRepositorio(JdbcTemplate jdbc, TipoDocumentoRepositorio tipoDocumentoRepositorio,
            PaisRepositorio paisRepositorio) {
        super(jdbc);
        this.tipoDocumentoRepositorio = tipoDocumentoRepositorio;
        this.paisRepositorio = paisRepositorio;
    }

    @Override
    protected Pasajero mapear(ResultSet fila) throws SQLException {
        Pasajero pasajero = new Pasajero();
        pasajero.setCodPasajero(fila.getLong("cod_pasajero"));
        pasajero.setTipoDocumento(referencia(TipoDocumento::new, TipoDocumento::setCodTipo,
                largo(fila, "cod_tipo_documento")));
        pasajero.setNumeroDocumento(fila.getString("numero_documento"));
        pasajero.setPrimerNombre(fila.getString("primer_nombre"));
        pasajero.setSegundoNombre(fila.getString("segundo_nombre"));
        pasajero.setPrimerApellido(fila.getString("primer_apellido"));
        pasajero.setSegundoApellido(fila.getString("segundo_apellido"));
        pasajero.setFechaNacimiento(fecha(fila, "fecha_nacimiento"));
        pasajero.setGenero(fila.getString("genero"));
        pasajero.setTipoPasajero(enumerado(fila, "tipo_pasajero", TipoPasajero.class));
        pasajero.setPais(referencia(Pais::new, Pais::setCodPais, largo(fila, "cod_pais")));
        return pasajero;
    }

    @Override
    protected List<Pasajero> completar(List<Pasajero> pasajeros) {
        cargar(pasajeros, Pasajero::getTipoDocumento, TipoDocumento::getCodTipo, tipoDocumentoRepositorio::porCodigos,
                Pasajero::setTipoDocumento);
        cargar(pasajeros, Pasajero::getPais, Pais::getCodPais, paisRepositorio::porCodigos, Pasajero::setPais);
        return pasajeros;
    }

    @Override
    public List<Pasajero> listar() {
        return consultar(SELECCION + "ORDER BY p.cod_pasajero");
    }

    @Override
    public Optional<Pasajero> buscarPorId(Long codPasajero) {
        return consultarUno(SELECCION + "WHERE p.cod_pasajero = ?", codPasajero);
    }

    public Map<Long, Pasajero> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE p.cod_pasajero = ANY(?)", arreglo(codigos)),
                Pasajero::getCodPasajero);
    }

    public Optional<Pasajero> buscarPorDocumento(Long codTipoDocumento, String numeroDocumento) {
        return consultarUno(SELECCION + "WHERE p.cod_tipo_documento = ? AND p.numero_documento = ?",
                codTipoDocumento, numeroDocumento);
    }

    @Override
    public boolean existePorId(Long codPasajero) {
        return existe("SELECT EXISTS (SELECT 1 FROM pasajero WHERE cod_pasajero = ?)", codPasajero);
    }

    @Override
    public Pasajero insertar(Pasajero pasajero) {
        pasajero.setCodPasajero(insertarDevolviendoCodigo("""
                INSERT INTO pasajero (cod_tipo_documento, numero_documento, primer_nombre, segundo_nombre,
                        primer_apellido, segundo_apellido, fecha_nacimiento, genero, tipo_pasajero, cod_pais)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING cod_pasajero
                """, codigo(pasajero.getTipoDocumento(), TipoDocumento::getCodTipo), pasajero.getNumeroDocumento(),
                pasajero.getPrimerNombre(), pasajero.getSegundoNombre(), pasajero.getPrimerApellido(),
                pasajero.getSegundoApellido(), pasajero.getFechaNacimiento(), pasajero.getGenero(),
                nombre(pasajero.getTipoPasajero()), codigo(pasajero.getPais(), Pais::getCodPais)));
        return pasajero;
    }

    @Override
    public Pasajero actualizar(Pasajero pasajero) {
        jdbc.update("""
                UPDATE pasajero
                SET cod_tipo_documento = ?, numero_documento = ?, primer_nombre = ?, segundo_nombre = ?,
                    primer_apellido = ?, segundo_apellido = ?, fecha_nacimiento = ?, genero = ?, tipo_pasajero = ?,
                    cod_pais = ?
                WHERE cod_pasajero = ?
                """, codigo(pasajero.getTipoDocumento(), TipoDocumento::getCodTipo), pasajero.getNumeroDocumento(),
                pasajero.getPrimerNombre(), pasajero.getSegundoNombre(), pasajero.getPrimerApellido(),
                pasajero.getSegundoApellido(), pasajero.getFechaNacimiento(), pasajero.getGenero(),
                nombre(pasajero.getTipoPasajero()), codigo(pasajero.getPais(), Pais::getCodPais),
                pasajero.getCodPasajero());
        return pasajero;
    }

    @Override
    public void eliminar(Long codPasajero) {
        jdbc.update("DELETE FROM pasajero WHERE cod_pasajero = ?", codPasajero);
    }
}
