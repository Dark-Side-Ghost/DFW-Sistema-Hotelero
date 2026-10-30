package com.udb.hotel.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class ReclamoDtos {
    private ReclamoDtos() {}

    public record ReclamoRequest(
            @NotNull(message = "Selecciona la reserva relacionada")
            Long idReserva,
            @NotBlank(message = "Selecciona el tipo de reclamo")
            @Pattern(regexp = "LIMPIEZA|FACTURACION|INFRAESTRUCTURA|ATENCION", message = "Tipo de reclamo no válido")
            String tipo,
            @NotBlank(message = "Describe el problema")
            @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
            String descripcion) {}

    public record ReclamoEstadoRequest(
            @NotBlank(message = "El estado es obligatorio")
            @Pattern(regexp = "PENDIENTE|EN_PROCESO|RESUELTO|RECHAZADO", message = "Estado no válido")
            String estado) {}

    public record ReclamoResponse(Long id, String tipo, String descripcion, String estado, LocalDateTime fechaCreacion,
                                  Long idReserva, String codigoReserva, String huesped, String hotel, String atendidoPor) {}
}
