package com.udb.hotel.dto;

import com.udb.hotel.util.Reglas;
import jakarta.validation.constraints.*;

public final class UsuarioDtos {
    private UsuarioDtos() {}

    /** En la edición la contraseña es opcional: si no se envía, se conserva la actual. */
    public record UsuarioRequest(
            @NotBlank(message = "Los nombres son obligatorios")
            @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
            @Pattern(regexp = Reglas.NOMBRE, message = Reglas.MSG_NOMBRE)
            String nombres,
            @NotBlank(message = "Los apellidos son obligatorios")
            @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
            @Pattern(regexp = Reglas.NOMBRE, message = Reglas.MSG_NOMBRE)
            String apellidos,
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            @Size(max = 150, message = "El correo no puede superar 150 caracteres")
            String email,
            @Pattern(regexp = Reglas.CLAVE, message = Reglas.MSG_CLAVE)
            String password,
            @NotBlank(message = "El rol es obligatorio")
            @Pattern(regexp = "ADMIN|RECEPCIONISTA|CLIENTE", message = "Rol no válido (ADMIN, RECEPCIONISTA o CLIENTE)")
            String rol,
            @Pattern(regexp = "ACTIVO|INACTIVO|BLOQUEADO", message = "Estado no válido (ACTIVO, INACTIVO o BLOQUEADO)")
            String estado) {}

    public record EstadoRequest(
            @NotBlank(message = "El estado es obligatorio")
            @Pattern(regexp = "ACTIVO|INACTIVO|BLOQUEADO", message = "Estado no válido (ACTIVO, INACTIVO o BLOQUEADO)")
            String estado) {}

    public record UsuarioResponse(Long id, String nombres, String apellidos, String email, String rol, String estado) {}
}
