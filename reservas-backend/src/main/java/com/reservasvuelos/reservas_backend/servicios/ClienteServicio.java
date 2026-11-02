package com.reservasvuelos.reservas_backend.servicios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.reservasvuelos.reservas_backend.Repositorio.ClienteRepositorio;
import com.reservasvuelos.reservas_backend.model.Cliente;

@Service
public class ClienteServicio {
    private final ClienteRepositorio clienteRepositorio;


    public ClienteServicio(ClienteRepositorio clienteRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
    }


    public List<Cliente> findAll() {
        return clienteRepositorio.findAll();
    }


    public Optional<Cliente> findById(Long id) {
        return clienteRepositorio.findById(id);
    }


    public Cliente save(Cliente cliente) {
       return clienteRepositorio.save(cliente);
    }


    public void deleteById(Long id) {
        clienteRepositorio.deleteById(id);
    }


}
