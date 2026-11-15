package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservasvuelos.reservas_backend.Repositorio.RepositorioSql;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public abstract class CrudServicio<T, ID> {

    protected final RepositorioSql<T, ID> repositorio;
    private final String nombre;
    private Validator validador;

    protected CrudServicio(RepositorioSql<T, ID> repositorio, String nombre) {
        this.repositorio = repositorio;
        this.nombre = nombre;
    }

    @Autowired
    public void setValidador(Validator validador) {
        this.validador = validador;
    }

    public List<T> listar() {
        return repositorio.listar();
    }

    public T buscar(ID id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe " + nombre + " " + id));
    }

    @Transactional
    public T crear(T entidad) {
        asignarId(entidad, null);
        completar(entidad, null);
        validarNuevo(entidad);
        validar(entidad);
        return repositorio.insertar(entidad);
    }

    @Transactional
    public T actualizar(ID id, T entidad) {
        T actual = buscar(id);
        asignarId(entidad, id);
        completar(entidad, actual);
        validar(entidad);
        return repositorio.actualizar(entidad);
    }

    @Transactional
    public void eliminar(ID id) {
        buscar(id);
        repositorio.eliminar(id);
    }

    protected abstract void asignarId(T entidad, ID id);

    protected void completar(T entidad, T actual) {
    }

    protected void validarNuevo(T entidad) {
    }

    protected void rechazarSiExiste(ID id) {
        if (repositorio.existePorId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe " + nombre + " " + id);
        }
    }

    protected void validar(T entidad) {
        Set<ConstraintViolation<T>> errores = validador.validate(entidad);
        if (!errores.isEmpty()) {
            throw new ConstraintViolationException(errores);
        }
    }

    protected <R, K> R referencia(R valor, Function<R, K> codigo, RepositorioSql<R, K> repositorioReferencia,
            String nombreReferencia) {
        if (valor == null) {
            return null;
        }
        K id = codigo.apply(valor);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el código de " + nombreReferencia);
        }
        return repositorioReferencia.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe " + nombreReferencia + " " + id));
    }

    protected <R> R obligatorio(R valor, String nombreDato) {
        if (valor == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta " + nombreDato);
        }
        return valor;
    }
}
