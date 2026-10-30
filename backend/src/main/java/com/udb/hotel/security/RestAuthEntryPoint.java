package com.udb.hotel.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udb.hotel.exception.ApiError;
import com.udb.hotel.util.Fechas;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** Respuesta 401 en JSON cuando no hay token o el token no sirve. */
@Component
@RequiredArgsConstructor
public class RestAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getOutputStream(), new ApiError(Fechas.ahora(), 401, "Unauthorized",
                "Tu sesión no es válida o ha expirado. Inicia sesión nuevamente.", req.getRequestURI(), null));
    }
}
