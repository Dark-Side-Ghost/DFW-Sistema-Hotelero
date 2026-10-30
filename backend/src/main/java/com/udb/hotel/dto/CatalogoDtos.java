package com.udb.hotel.dto;

import com.udb.hotel.util.Reglas;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public final class CatalogoDtos {
    private CatalogoDtos() {}

    public record HabitacionRequest(
            @NotNull(message = "Selecciona un hotel")
            Long idHotel,
            @NotNull(message = "Selecciona un tipo de habitación")
            Long idTipoHabitacion,
            @NotBlank(message = "El número de habitación es obligatorio")
            @Pattern(regexp = Reglas.NUMERO_HABITACION, message = "El número solo puede tener letras, números y guiones (máx. 20)")
            String numeroHabitacion,
            @NotNull(message = "La tarifa por noche es obligatoria")
            @DecimalMin(value = "0.01", message = "La tarifa debe ser mayor a 0")
            @Digits(integer = 8, fraction = 2, message = "La tarifa admite máximo 2 decimales")
            BigDecimal precioBaseNoche,
            @Pattern(regexp = "DISPONIBLE|OCUPADA|MANTENIMIENTO", message = "Estado no válido (DISPONIBLE, OCUPADA o MANTENIMIENTO)")
            String estado,
            List<Long> serviciosIds) {}

    public record PoliticaRequest(
            @NotNull(message = "Selecciona un hotel")
            Long idHotel,
            @NotNull(message = "Los días previos son obligatorios")
            @Min(value = 0, message = "Los días previos no pueden ser negativos")
            @Max(value = 365, message = "Los días previos no pueden superar 365")
            Integer diasPreviosLimite,
            @NotNull(message = "El porcentaje de cargo es obligatorio")
            @DecimalMin(value = "0.00", message = "El porcentaje no puede ser negativo")
            @DecimalMax(value = "100.00", message = "El porcentaje no puede superar 100")
            BigDecimal porcentajeCargo) {}
}
