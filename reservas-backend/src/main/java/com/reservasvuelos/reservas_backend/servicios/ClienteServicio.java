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

import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.model.Cliente;

@Service 
public class ClienteServicio implements ClienteRepositorio {
    @Autowired 
    private ClienteRepositorio clienteRepositorio;


    public ClienteServicio(ClienteRepositorio clienteRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
    }


    @Override
    public void flush() {
        clienteRepositorio.flush();
    }


    @Override
    public <S extends Cliente> S saveAndFlush(S entity) {
        return clienteRepositorio.saveAndFlush(entity);
    }


    @Override
    public <S extends Cliente> List<S> saveAllAndFlush(Iterable<S> entities) {
        return clienteRepositorio.saveAllAndFlush(entities);
    }


    @Override
    public void deleteAllInBatch(Iterable<Cliente> entities) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllInBatch'");
    }


    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllByIdInBatch'");
    }


    @Override
    public void deleteAllInBatch() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllInBatch'");
    }


    @Override
    public Cliente getOne(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOne'");
    }


    @Override
    public Cliente getById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }


    @Override
    public Cliente getReferenceById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getReferenceById'");
    }


    @Override
    public <S extends Cliente> List<S> findAll(Example<S> example) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }


    @Override
    public <S extends Cliente> List<S> findAll(Example<S> example, Sort sort) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }


    @Override
    public List<Cliente> findAll() {
        return clienteRepositorio.findAll();
    }


    @Override
    public List<Cliente> findAllById(Iterable<Long> ids) {
        return clienteRepositorio.findAllById(ids);
    }


    @Override
    public <S extends Cliente> List<S> saveAll(Iterable<S> entities) {
        return clienteRepositorio.saveAll(entities);
    }


    @Override
    public long count() {
        return clienteRepositorio.count();
    }


    @Override
    public void delete(Cliente entity) {
        clienteRepositorio.delete(entity);
    }


    @Override
    public void deleteAll() {
        clienteRepositorio.deleteAll();
    }


    @Override
    public void deleteAll(Iterable<? extends Cliente> entities) {
        clienteRepositorio.deleteAll(entities);
    }


    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        clienteRepositorio.deleteAllById(ids);
    }


    @Override
    public void deleteById(Long id) {
        clienteRepositorio.deleteById(id);
    
    }


    @Override
    public boolean existsById(Long id) {
       return clienteRepositorio.existsById(id);
    }


    @Override
    public Optional<Cliente> findById(Long id) {
        return clienteRepositorio.findById(id);
    }


    @Override
    public <S extends Cliente> S save(S entity) {
       return clienteRepositorio.save(entity);
    }


    @Override
    public List<Cliente> findAll(Sort sort) {
        return clienteRepositorio.findAll(sort);
    }


    @Override
    public Page<Cliente> findAll(Pageable pageable) {
        return clienteRepositorio.findAll(pageable);
    }


    @Override
    public <S extends Cliente> long count(Example<S> example) {
       return clienteRepositorio.count(example);
    }


    @Override
    public <S extends Cliente> boolean exists(Example<S> example) {
        return clienteRepositorio.exists(example);
    }


    @Override
    public <S extends Cliente> Page<S> findAll(Example<S> example, Pageable pageable) {
       return clienteRepositorio.findAll(example, pageable);
    }


    @Override
    public <S extends Cliente, R> R findBy(Example<S> arg0, Function<FetchableFluentQuery<S>, R> arg1) {
        return clienteRepositorio.findBy(arg0, arg1);
    }


    @Override
    public <S extends Cliente> Optional<S> findOne(Example<S> example) {
        return clienteRepositorio.findOne(example);
    }
    
    
}
