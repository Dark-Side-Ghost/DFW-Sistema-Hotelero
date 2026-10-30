package com.udb.hotel.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Documentación Swagger en /swagger-ui.html. El botón "Authorize" acepta el token JWT. */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "API Sistema de Reserva de Habitaciones de Hotel", version = "1.0",
                description = "Backend del proyecto DWF404: hoteles, habitaciones, reservas, pagos, reclamos y estadísticas."),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
