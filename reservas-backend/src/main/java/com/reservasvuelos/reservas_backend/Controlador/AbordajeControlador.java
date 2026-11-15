package com.reservasvuelos.reservas_backend.Controlador;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservasvuelos.reservas_backend.dto.CheckinReservaDTO;
import com.reservasvuelos.reservas_backend.dto.CheckinSolicitudDTO;
import com.reservasvuelos.reservas_backend.dto.EquipajeRegistradoDTO;
import com.reservasvuelos.reservas_backend.dto.EquipajeSolicitudDTO;
import com.reservasvuelos.reservas_backend.servicios.AbordajeServicio;
import com.reservasvuelos.reservas_backend.servicios.RegistroEquipajeServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/check-in")
public class AbordajeControlador {

    private final AbordajeServicio abordajeServicio;
    private final RegistroEquipajeServicio registroEquipajeServicio;

    public AbordajeControlador(AbordajeServicio abordajeServicio, RegistroEquipajeServicio registroEquipajeServicio) {
        this.abordajeServicio = abordajeServicio;
        this.registroEquipajeServicio = registroEquipajeServicio;
    }

    @GetMapping("/reservas/{codReserva}")
    public CheckinReservaDTO consultar(@PathVariable Long codReserva, Authentication autenticacion) {
        return abordajeServicio.consultar(codReserva, autenticacion);
    }

    @PostMapping("/reservas/{codReserva}")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckinReservaDTO hacerCheckin(@PathVariable Long codReserva,
            @Valid @RequestBody(required = false) CheckinSolicitudDTO datos, Authentication autenticacion) {
        return abordajeServicio.hacerCheckin(codReserva, datos, autenticacion);
    }

    @GetMapping("/reservas/{codReserva}/pases")
    public ResponseEntity<byte[]> pases(@PathVariable Long codReserva, Authentication autenticacion) {
        return RespuestaPdf.de(abordajeServicio.pasesDeReserva(codReserva, autenticacion));
    }

    @GetMapping("/tiquetes/{codTiquete}/pase")
    public ResponseEntity<byte[]> pase(@PathVariable Long codTiquete, Authentication autenticacion) {
        return RespuestaPdf.de(abordajeServicio.paseDeTiquete(codTiquete, autenticacion));
    }

    @PostMapping("/tiquetes/{codTiquete}/equipajes")
    @ResponseStatus(HttpStatus.CREATED)
    public EquipajeRegistradoDTO registrarEquipaje(@PathVariable Long codTiquete,
            @Valid @RequestBody EquipajeSolicitudDTO datos, Authentication autenticacion) {
        return registroEquipajeServicio.registrar(codTiquete, datos, autenticacion);
    }
}
