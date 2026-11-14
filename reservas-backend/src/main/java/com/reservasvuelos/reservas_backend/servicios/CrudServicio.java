package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

public abstract class CrudServicio<T, ID> {

    protected final JpaRepository<T, ID> repositorio;
    private final String nombre;

    protected CrudServicio(JpaRepository<T, ID> repositorio, String nombre) {
        this.repositorio = repositorio;
        this.nombre = nombre;
    }

    public List<T> listar() {
        return repositorio.findAll();
    }

    public T buscar(ID id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe " + nombre + " " + id));
    }

    @Transactional
    public T crear(T entidad) {
        asignarId(entidad, null);
        completar(entidad, null);
        validarNuevo(entidad);
        return repositorio.saveAndFlush(entidad);
    }

    @Transactional
    public T actualizar(ID id, T entidad) {
        T actual = buscar(id);
        asignarId(entidad, id);
        completar(entidad, actual);
        return repositorio.saveAndFlush(entidad);
    }

    @Transactional
    public void eliminar(ID id) {
        repositorio.delete(buscar(id));
        repositorio.flush();
    }

    protected abstract void asignarId(T entidad, ID id);

    protected void completar(T entidad, T actual) {
    }

    protected void validarNuevo(T entidad) {
    }

    protected void rechazarSiExiste(ID id) {
        if (repositorio.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe " + nombre + " " + id);
        }
    }

    protected <R, K> R referencia(R valor, Function<R, K> codigo, JpaRepository<R, K> repositorioReferencia,
            String nombreReferencia) {
        if (valor == null) {
            return null;
        }
        K id = codigo.apply(valor);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el código de " + nombreReferencia);
        }
        return repositorioReferencia.findById(id)
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
