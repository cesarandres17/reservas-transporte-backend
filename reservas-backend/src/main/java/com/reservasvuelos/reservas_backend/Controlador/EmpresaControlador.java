package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.model.Empresa;
import com.reservasvuelos.reservas_backend.servicios.EmpresaServicio;

@RestController
@RequestMapping("/empresas")
public class EmpresaControlador extends CrudControlador<Empresa, Long> {

    public EmpresaControlador(EmpresaServicio empresaServicio) {
        super(empresaServicio);
    }
}
