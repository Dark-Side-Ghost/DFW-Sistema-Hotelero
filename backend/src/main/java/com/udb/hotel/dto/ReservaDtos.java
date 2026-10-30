package com.udb.hotel.dto;

import com.udb.hotel.util.Reglas;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ReservaDtos {
    private ReservaDtos() {}

    public record HuespedRequest(
            @NotBlank(message = "El nombre completo del huésped es obligatorio")
            @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
            @Pattern(regexp = Reglas.NOMBRE, message = Reglas.MSG_NOMBRE)
            String nombreCompleto,
            @NotNull(message = "La fecha de nacimiento es obligatoria")
            @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
            LocalDate fechaNacimiento,
            @NotBlank(message = "El tipo de documento es obligatorio")
            @Pattern(regexp = "DUI|PASAPORTE|DNI|NIT", message = "Tipo de documento no válido (DUI, PASAPORTE, DNI o NIT)")
            String tipoDocumento,
            @NotBlank(message = "El número de documento es obligatorio")
            @Size(max = 50, message = "El número de documento no puede superar 50 caracteres")
            String numeroDocumento,
            @NotBlank(message = "El teléfono es obligatorio")
            @Pattern(regexp = Reglas.TELEFONO, message = Reglas.MSG_TELEFONO)
            String telefono,
            @NotBlank(message = "El correo del huésped es obligatorio")
            @Email(message = "El correo del huésped no tiene un formato válido")
            @Size(max = 150, message = "El correo no puede superar 150 caracteres")
            String correoElectronico) {}

    public record ServicioSolicitado(
            @NotNull(message = "Indica el servicio adicional")
            Long idServicio,
            @NotNull(message = "Indica la cantidad del servicio")
            @Min(value = 1, message = "La cantidad mínima es 1")
            @Max(value = 20, message = "La cantidad máxima es 20")
            Integer cantidad) {}

    public record ReservaRequest(
            @NotNull(message = "Selecciona una habitación")
            Long idHabitacion,
            @NotNull(message = "La fecha de entrada es obligatoria")
            LocalDate fechaEntrada,
            @NotNull(message = "La fecha de salida es obligatoria")
            LocalDate fechaSalida,
            @NotNull(message = "El número de huéspedes es obligatorio")
            @Min(value = 1, message = "Debe haber al menos 1 huésped")
            @Max(value = 10, message = "El máximo es 10 huéspedes")
            Integer numeroHuespedes,
            @NotNull(message = "Los datos del huésped son obligatorios")
            @Valid
            HuespedRequest huesped,
            @Valid
            List<ServicioSolicitado> servicios) {}

    public record ModificarRequest(
            @NotNull(message = "La fecha de entrada es obligatoria")
            LocalDate fechaEntrada,
            @NotNull(message = "La fecha de salida es obligatoria")
            LocalDate fechaSalida,
            @NotNull(message = "El número de huéspedes es obligatorio")
            @Min(value = 1, message = "Debe haber al menos 1 huésped")
            @Max(value = 10, message = "El máximo es 10 huéspedes")
            Integer numeroHuespedes) {}

    /** Los datos de tarjeta solo se validan (no se guardan): el pago es simulado. */
    public record PagoRequest(
            @NotBlank(message = "Selecciona un método de pago")
            @Pattern(regexp = "TARJETA_CREDITO|TARJETA_DEBITO|TRANSFERENCIA|EFECTIVO", message = "Método de pago no válido")
            String metodoPago,
            String numeroTarjeta,
            String vencimiento,
            String cvv,
            String titular) {}

    public record CancelarRequest(
            @NotBlank(message = "Indica el motivo de la cancelación")
            @Size(min = 5, max = 500, message = "El motivo debe tener entre 5 y 500 caracteres")
            String motivo) {}

    public record ServicioReservaResponse(String nombre, Integer cantidad, BigDecimal subtotal) {}

    public record ReservaResponse(
            Long id,
            String codigo,
            String estado,
            String estadoPago,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            long noches,
            int numeroHuespedes,
            BigDecimal precioTotal,
            BigDecimal montoPagado,
            BigDecimal montoReembolso,
            String motivoCancelacion,
            String hotel,
            String ciudad,
            Long idHabitacion,
            String numeroHabitacion,
            String tipoHabitacion,
            BigDecimal precioNoche,
            String huespedNombre,
            String huespedDocumento,
            String huespedCorreo,
            String usuarioEmail,
            int politicaDias,
            BigDecimal politicaPorcentaje,
            List<ServicioReservaResponse> servicios,
            LocalDateTime fechaCreacion) {}
}
