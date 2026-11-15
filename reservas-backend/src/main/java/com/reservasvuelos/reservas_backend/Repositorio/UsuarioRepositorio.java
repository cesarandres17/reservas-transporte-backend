package com.reservasvuelos.reservas_backend.Repositorio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reservasvuelos.reservas_backend.model.Ciudad;
import com.reservasvuelos.reservas_backend.model.Rol;
import com.reservasvuelos.reservas_backend.model.TipoDocumento;
import com.reservasvuelos.reservas_backend.model.Usuario;
import com.reservasvuelos.reservas_backend.model.enums.EstadoUsuario;

@Repository
public class UsuarioRepositorio extends RepositorioSql<Usuario, Long> {

    private static final String SELECCION = """
            SELECT u.cod_usuario, u.cod_tipo_documento, u.numero_documento, u.primer_nombre, u.segundo_nombre,
                   u.primer_apellido, u.segundo_apellido, u.email, u.password, u.telefono, u.fecha_nacimiento,
                   u.direccion, u.cod_ciudad, u.fecha_registro, u.estado, u.cod_rol
            FROM usuario u
            """;

    private final TipoDocumentoRepositorio tipoDocumentoRepositorio;
    private final CiudadRepositorio ciudadRepositorio;
    private final RolRepositorio rolRepositorio;

    public UsuarioRepositorio(JdbcTemplate jdbc, TipoDocumentoRepositorio tipoDocumentoRepositorio,
            CiudadRepositorio ciudadRepositorio, RolRepositorio rolRepositorio) {
        super(jdbc);
        this.tipoDocumentoRepositorio = tipoDocumentoRepositorio;
        this.ciudadRepositorio = ciudadRepositorio;
        this.rolRepositorio = rolRepositorio;
    }

    @Override
    protected Usuario mapear(ResultSet fila) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setCodUsuario(fila.getLong("cod_usuario"));
        usuario.setTipoDocumento(referencia(TipoDocumento::new, TipoDocumento::setCodTipo,
                largo(fila, "cod_tipo_documento")));
        usuario.setNumeroDocumento(fila.getString("numero_documento"));
        usuario.setPrimerNombre(fila.getString("primer_nombre"));
        usuario.setSegundoNombre(fila.getString("segundo_nombre"));
        usuario.setPrimerApellido(fila.getString("primer_apellido"));
        usuario.setSegundoApellido(fila.getString("segundo_apellido"));
        usuario.setEmail(fila.getString("email"));
        usuario.setPassword(fila.getString("password"));
        usuario.setTelefono(fila.getString("telefono"));
        usuario.setFechaNacimiento(fecha(fila, "fecha_nacimiento"));
        usuario.setDireccion(fila.getString("direccion"));
        usuario.setCiudad(referencia(Ciudad::new, Ciudad::setCodCiudad, largo(fila, "cod_ciudad")));
        usuario.setFechaRegistro(fecha(fila, "fecha_registro"));
        usuario.setEstado(enumerado(fila, "estado", EstadoUsuario.class));
        usuario.setRol(referencia(Rol::new, Rol::setCodRol, largo(fila, "cod_rol")));
        return usuario;
    }

    @Override
    protected List<Usuario> completar(List<Usuario> usuarios) {
        cargar(usuarios, Usuario::getTipoDocumento, TipoDocumento::getCodTipo, tipoDocumentoRepositorio::porCodigos,
                Usuario::setTipoDocumento);
        cargar(usuarios, Usuario::getCiudad, Ciudad::getCodCiudad, ciudadRepositorio::porCodigos, Usuario::setCiudad);
        cargar(usuarios, Usuario::getRol, Rol::getCodRol, rolRepositorio::porCodigos, Usuario::setRol);
        return usuarios;
    }

    @Override
    public List<Usuario> listar() {
        return consultar(SELECCION + "ORDER BY u.cod_usuario");
    }

    @Override
    public Optional<Usuario> buscarPorId(Long codUsuario) {
        return consultarUno(SELECCION + "WHERE u.cod_usuario = ?", codUsuario);
    }

    public Map<Long, Usuario> porCodigos(Collection<Long> codigos) {
        return porClave(consultar(SELECCION + "WHERE u.cod_usuario = ANY(?)", arreglo(codigos)),
                Usuario::getCodUsuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return consultarUno(SELECCION + "WHERE LOWER(u.email) = LOWER(?)", email);
    }

    public boolean existeEmail(String email) {
        return existe("SELECT EXISTS (SELECT 1 FROM usuario WHERE LOWER(email) = LOWER(?))", email);
    }

    public boolean existeDocumento(Long codTipoDocumento, String numeroDocumento) {
        return existe("""
                SELECT EXISTS (SELECT 1 FROM usuario
                               WHERE cod_tipo_documento = ? AND numero_documento = ?)
                """, codTipoDocumento, numeroDocumento);
    }

    public boolean existeConRol(String nombreRol) {
        return existe("""
                SELECT EXISTS (SELECT 1 FROM usuario u
                               JOIN rol r ON r.cod_rol = u.cod_rol
                               WHERE r.nombre = ?)
                """, nombreRol);
    }

    @Override
    public boolean existePorId(Long codUsuario) {
        return existe("SELECT EXISTS (SELECT 1 FROM usuario WHERE cod_usuario = ?)", codUsuario);
    }

    @Override
    public Usuario insertar(Usuario usuario) {
        usuario.setCodUsuario(insertarDevolviendoCodigo("""
                INSERT INTO usuario (cod_tipo_documento, numero_documento, primer_nombre, segundo_nombre,
                        primer_apellido, segundo_apellido, email, password, telefono, fecha_nacimiento, direccion,
                        cod_ciudad, fecha_registro, estado, cod_rol)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING cod_usuario
                """, codigo(usuario.getTipoDocumento(), TipoDocumento::getCodTipo), usuario.getNumeroDocumento(),
                usuario.getPrimerNombre(), usuario.getSegundoNombre(), usuario.getPrimerApellido(),
                usuario.getSegundoApellido(), usuario.getEmail(), usuario.getPassword(), usuario.getTelefono(),
                usuario.getFechaNacimiento(), usuario.getDireccion(), codigo(usuario.getCiudad(), Ciudad::getCodCiudad),
                usuario.getFechaRegistro(), nombre(usuario.getEstado()), codigo(usuario.getRol(), Rol::getCodRol)));
        return usuario;
    }

    @Override
    public Usuario actualizar(Usuario usuario) {
        jdbc.update("""
                UPDATE usuario
                SET cod_tipo_documento = ?, numero_documento = ?, primer_nombre = ?, segundo_nombre = ?,
                    primer_apellido = ?, segundo_apellido = ?, email = ?, password = ?, telefono = ?,
                    fecha_nacimiento = ?, direccion = ?, cod_ciudad = ?, fecha_registro = ?, estado = ?, cod_rol = ?
                WHERE cod_usuario = ?
                """, codigo(usuario.getTipoDocumento(), TipoDocumento::getCodTipo), usuario.getNumeroDocumento(),
                usuario.getPrimerNombre(), usuario.getSegundoNombre(), usuario.getPrimerApellido(),
                usuario.getSegundoApellido(), usuario.getEmail(), usuario.getPassword(), usuario.getTelefono(),
                usuario.getFechaNacimiento(), usuario.getDireccion(), codigo(usuario.getCiudad(), Ciudad::getCodCiudad),
                usuario.getFechaRegistro(), nombre(usuario.getEstado()), codigo(usuario.getRol(), Rol::getCodRol),
                usuario.getCodUsuario());
        return usuario;
    }

    @Override
    public void eliminar(Long codUsuario) {
        jdbc.update("DELETE FROM usuario WHERE cod_usuario = ?", codUsuario);
    }
}
