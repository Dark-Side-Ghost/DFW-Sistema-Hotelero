package com.udb.hotel.dto;

import com.udb.hotel.util.Reglas;
import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            String email,
            @NotBlank(message = "La contraseña es obligatoria")
            String password) {}

    public record RegistroRequest(
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
            @NotBlank(message = "La contraseña es obligatoria")
            @Pattern(regexp = Reglas.CLAVE, message = Reglas.MSG_CLAVE)
            String password) {}

    public record AuthResponse(String token, String tipo, Long id, String nombres, String apellidos, String email, String rol) {}
}
