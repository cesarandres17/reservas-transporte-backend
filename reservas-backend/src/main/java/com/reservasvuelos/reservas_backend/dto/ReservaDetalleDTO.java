package com.reservasvuelos.reservas_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.reservasvuelos.reservas_backend.model.Asiento;
import com.reservasvuelos.reservas_backend.model.Factura;
import com.reservasvuelos.reservas_backend.model.Pago;
import com.reservasvuelos.reservas_backend.model.Pasajero;
import com.reservasvuelos.reservas_backend.model.Reserva;
import com.reservasvuelos.reservas_backend.model.Ruta;
import com.reservasvuelos.reservas_backend.model.Tiquete;
import com.reservasvuelos.reservas_backend.model.Usuario;
import com.reservasvuelos.reservas_backend.model.Viaje;

public record ReservaDetalleDTO(
        Long codReserva,
        String codigoReserva,
        String estado,
        String canal,
        LocalDateTime fechaReserva,
        LocalDateTime fechaLimitePago,
        Long codCliente,
        String cliente,
        String emailCliente,
        String empleado,
        String codigoPromocion,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal total,
        String numeroFactura,
        List<TiqueteReserva> tiquetes,
        List<PagoReserva> pagos) {

    public record TiqueteReserva(
            Long codTiquete,
            String numeroTiquete,
            String estado,
            Long codPasajero,
            String pasajero,
            String documento,
            String tipoPasajero,
            Long codViaje,
            String codigoViaje,
            String origen,
            String destino,
            LocalDateTime fechaHoraSalida,
            String clase,
            String numeroAsiento,
            BigDecimal precioPagado) {

        public static TiqueteReserva desde(Tiquete tiquete) {
            Pasajero pasajero = tiquete.getPasajero();
            Viaje viaje = tiquete.getTarifa().getViaje();
            Ruta ruta = viaje.getRuta();
            Asiento asiento = tiquete.getAsiento();
            return new TiqueteReserva(
                    tiquete.getCodTiquete(),
                    tiquete.getNumeroTiquete(),
                    tiquete.getEstado().getNombre(),
                    pasajero.getCodPasajero(),
                    nombre(pasajero.getPrimerNombre(), pasajero.getPrimerApellido(), pasajero.getSegundoApellido()),
                    pasajero.getTipoDocumento().getSigla() + " " + pasajero.getNumeroDocumento(),
                    pasajero.getTipoPasajero().name(),
                    viaje.getCodViaje(),
                    viaje.getCodigoViaje(),
                    ruta.getTerminalOrigen().getCiudad().getNombre(),
                    ruta.getTerminalDestino().getCiudad().getNombre(),
                    viaje.getFechaHoraSalida(),
                    tiquete.getTarifa().getClaseServicio().getNombre(),
                    asiento == null ? null : asiento.getNumeroAsiento(),
                    tiquete.getPrecioPagado());
        }
    }

    public record PagoReserva(
            Long codPago,
            LocalDateTime fechaPago,
            String metodo,
            BigDecimal monto,
            Integer cuotas,
            String referenciaTransaccion,
            String estado) {

        public static PagoReserva desde(Pago pago) {
            return new PagoReserva(
                    pago.getCodPago(),
                    pago.getFechaPago(),
                    pago.getMetodoPago().getNombre(),
                    pago.getMonto(),
                    pago.getCuotas(),
                    pago.getReferenciaTransaccion(),
                    pago.getEstado().getNombre());
        }
    }

    public static ReservaDetalleDTO desde(Reserva reserva, List<Tiquete> tiquetes, List<Pago> pagos,
            Factura factura) {
        Usuario cliente = reserva.getCliente().getUsuario();
        Usuario empleado = reserva.getEmpleado() == null ? null : reserva.getEmpleado().getUsuario();
        return new ReservaDetalleDTO(
                reserva.getCodReserva(),
                reserva.getCodigoReserva(),
                reserva.getEstado().getNombre(),
                reserva.getCanal().name(),
                reserva.getFechaReserva(),
                reserva.getFechaLimitePago(),
                cliente.getCodUsuario(),
                nombre(cliente.getPrimerNombre(), cliente.getPrimerApellido(), cliente.getSegundoApellido()),
                cliente.getEmail(),
                empleado == null ? null : nombre(empleado.getPrimerNombre(), empleado.getPrimerApellido(), null),
                reserva.getPromocion() == null ? null : reserva.getPromocion().getCodigo(),
                reserva.getSubtotal(),
                reserva.getDescuento(),
                reserva.getTotal(),
                factura == null ? null : factura.getNumeroFactura(),
                tiquetes.stream().map(TiqueteReserva::desde).toList(),
                pagos.stream().map(PagoReserva::desde).toList());
    }

    private static String nombre(String primerNombre, String primerApellido, String segundoApellido) {
        String nombre = primerNombre + " " + primerApellido;
        return segundoApellido == null || segundoApellido.isBlank() ? nombre : nombre + " " + segundoApellido;
    }
}
