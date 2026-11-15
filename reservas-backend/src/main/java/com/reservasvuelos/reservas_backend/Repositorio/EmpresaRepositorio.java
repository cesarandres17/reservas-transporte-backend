package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Empresa;
import com.reservasvuelos.reservas_backend.model.Pais;
import com.reservasvuelos.reservas_backend.model.TipoTransporte;
import com.reservasvuelos.reservas_backend.model.enums.EstadoEmpresa;

@Repository
public class EmpresaRepositorio extends RepositorioSql<Empresa, Long> {

    private static final String SELECCION = """
            SELECT e.cod_empresa, e.nit, e.nombre, e.codigo_operador, e.telefono, e.email, e.estado, e.cod_tipo,
                   e.cod_pais
            FROM empresa e
            """;

    private final TipoTransporteRepositorio tipoTransporteRepositorio;
    private final PaisRepositorio paisRepositorio;

    public EmpresaRepositorio(JdbcTemplate jdbc, TipoTransporteRepositorio tipoTransporteRepositorio,
            PaisRepositorio paisRepositorio) {
        super(jdbc);
        this.tipoTransporteRepositorio = tipoTransporteRepositorio;
        this.paisRepositorio = paisRepositorio;
    }

    @Override
    protected Empresa mapear(ResultSet fila) throws SQLException {
        Empresa empresa = new Empresa();
        empresa.setCodEmpresa(fila.getLong("cod_empresa"));
        empresa.setNit(fila.getString("nit"));
        empresa.setNombre(fila.getString("nombre"));
        empresa.setCodigoOperador(fila.getString("codigo_operador"));
        empresa.setTelefono(fila.getString("telefono"));
        empresa.setEmail(fila.getString("email"));
        empresa.setEstado(enumerado(fila, "estado", EstadoEmpresa.class));
        empresa.setTipoTransporte(referencia(TipoTransporte::new, TipoTransporte::setCodTipo, largo(fila, "cod_tipo")));
        empresa.setPais(referencia(Pais::new, Pais::setCodPais, largo(fila, "cod_pais")));
        return empresa;
    }

    @Override
    protected List<Empresa> completar(List<Empresa> empresas) {
        cargar(empresas, Empresa::getTipoTransporte, TipoTransporte::getCodTipo, tipoTransporteRepositorio::porCodigos,
                Empresa::setTipoTransporte);
        cargar(empresas, Empresa::getPais, Pais::getCodPais, paisRepositorio::porCodigos, Empresa::setPais);
        return empresas;
    }

    @Override
    public List<Empresa> listar() {
        return consultar(SELECCION + "ORDER BY e.cod_empresa");
    }

    @Override
    public Optional<Empresa> buscarPorId(Long codEmpresa) {
        return consultarUno(SELECCION + "WHERE e.cod_empresa = ?", codEmpresa);
    }

    public Map<Long, Empresa> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE e.cod_empresa = ANY(?)", arreglo(codigos)),
                Empresa::getCodEmpresa);
    }

    @Override
    public boolean existePorId(Long codEmpresa) {
        return existe("SELECT EXISTS (SELECT 1 FROM empresa WHERE cod_empresa = ?)", codEmpresa);
    }

    @Override
    public Empresa insertar(Empresa empresa) {
        empresa.setCodEmpresa(insertarDevolviendoCodigo("""
                INSERT INTO empresa (nit, nombre, codigo_operador, telefono, email, estado, cod_tipo, cod_pais)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING cod_empresa
                """, empresa.getNit(), empresa.getNombre(), empresa.getCodigoOperador(), empresa.getTelefono(),
                empresa.getEmail(), nombre(empresa.getEstado()),
                codigo(empresa.getTipoTransporte(), TipoTransporte::getCodTipo),
                codigo(empresa.getPais(), Pais::getCodPais)));
        return empresa;
    }

    @Override
    public Empresa actualizar(Empresa empresa) {
        jdbc.update("""
                UPDATE empresa
                SET nit = ?, nombre = ?, codigo_operador = ?, telefono = ?, email = ?, estado = ?, cod_tipo = ?,
                    cod_pais = ?
                WHERE cod_empresa = ?
                """, empresa.getNit(), empresa.getNombre(), empresa.getCodigoOperador(), empresa.getTelefono(),
                empresa.getEmail(), nombre(empresa.getEstado()),
                codigo(empresa.getTipoTransporte(), TipoTransporte::getCodTipo),
                codigo(empresa.getPais(), Pais::getCodPais), empresa.getCodEmpresa());
        return empresa;
    }

    @Override
    public void eliminar(Long codEmpresa) {
        jdbc.update("DELETE FROM empresa WHERE cod_empresa = ?", codEmpresa);
    }
}
