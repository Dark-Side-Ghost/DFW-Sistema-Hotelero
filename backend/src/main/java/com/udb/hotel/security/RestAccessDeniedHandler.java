package com.udb.hotel.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udb.hotel.exception.ApiError;
import com.udb.hotel.util.Fechas;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Respuesta 403 en JSON cuando el usuario está autenticado pero su rol no permite la acción. */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getOutputStream(), new ApiError(Fechas.ahora(), 403, "Forbidden",
                "No tienes permisos para realizar esta acción.", req.getRequestURI(), null));
    }
}
