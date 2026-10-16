package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.CiudadRepositorio;
import com.reservasvuelos.reservas_backend.model.Ciudad;
@Service 
public class CiudadServicio implements CiudadRepositorio {
    @Autowired
    private final CiudadRepositorio ciudadRepositorio;

    public CiudadServicio(CiudadRepositorio ciudadRepositorio) {
        this.ciudadRepositorio = ciudadRepositorio;
    }

    @Override
    public void flush() {
        ciudadRepositorio.flush();
    }

    @Override
    public <S extends Ciudad> S saveAndFlush(S entity) {
        return ciudadRepositorio.saveAndFlush(entity);
    }

    @Override
    public <S extends Ciudad> List<S> saveAllAndFlush(Iterable<S> entities) {
        return ciudadRepositorio.saveAllAndFlush(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Ciudad> entities) {
        ciudadRepositorio.deleteAllInBatch(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        ciudadRepositorio.deleteAllByIdInBatch(ids);
    }

    @Override
    public void deleteAllInBatch() {
        ciudadRepositorio.deleteAllInBatch();
    }

    @Override
    public Ciudad getOne(Long id) {
        return ciudadRepositorio.getReferenceById(id);
    }

    @Override
    public Ciudad getById(Long id) {
        return ciudadRepositorio.getReferenceById(id);
    }

    @Override
    public Ciudad getReferenceById(Long id) {
        return ciudadRepositorio.getReferenceById(id);
    }

    @Override
    public <S extends Ciudad> List<S> findAll(Example<S> example) {
        return ciudadRepositorio.findAll(example);
        
    }

    @Override
    public <S extends Ciudad> List<S> findAll(Example<S> example, Sort sort) {
        return ciudadRepositorio.findAll(example, sort);
    }

    @Override
    public <S extends Ciudad> List<S> saveAll(Iterable<S> entities) {
        return ciudadRepositorio.saveAll(entities);
    }

    @Override
    public List<Ciudad> findAll() {
        return ciudadRepositorio.findAll();
    }

    @Override
    public List<Ciudad> findAllById(Iterable<Long> ids) {
        return ciudadRepositorio.findAllById(ids);
    }

    @Override
    public <S extends Ciudad> S save(S entity) {
        return ciudadRepositorio.save(entity);
    }

    @Override
    public Optional<Ciudad> findById(Long id) {
        return ciudadRepositorio.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return ciudadRepositorio.existsById(id);
    }

    @Override
    public long count() {
        return ciudadRepositorio.count();
    }

    @Override
    public void deleteById(Long id) {
        ciudadRepositorio.deleteById(id);
    }

    @Override
    public void delete(Ciudad entity) {
        ciudadRepositorio.delete(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        ciudadRepositorio.deleteAllById(ids);
    }

    @Override
    public void deleteAll(Iterable<? extends Ciudad> entities) {
        ciudadRepositorio.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        ciudadRepositorio.deleteAll();
    }

    @Override
    public List<Ciudad> findAll(Sort sort) {
        return ciudadRepositorio.findAll(sort);
    }

    @Override
    public Page<Ciudad> findAll(Pageable pageable) {
        return ciudadRepositorio.findAll(pageable);
    }

    @Override
    public <S extends Ciudad> Optional<S> findOne(Example<S> example) {
        return ciudadRepositorio.findOne(example);
    }

    @Override
    public <S extends Ciudad> Page<S> findAll(Example<S> example, Pageable pageable) {
        return ciudadRepositorio.findAll(example, pageable);
    }

    @Override
    public <S extends Ciudad> long count(Example<S> example) {
        return ciudadRepositorio.count(example);
    }

    @Override
    public <S extends Ciudad> boolean exists(Example<S> example) {
        return ciudadRepositorio.exists(example);
    }

    @Override
    public <S extends Ciudad, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
        return ciudadRepositorio.findBy(example, queryFunction);
    }
    
}
