package com.udb.hotel.controller;

import com.udb.hotel.dto.EstadisticasResponse;
import com.udb.hotel.service.EstadisticasService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Estadísticas", description = "Ocupación, ingresos y cancelaciones (solo ADMIN)")
public class EstadisticasController {

    private final EstadisticasService service;

    @GetMapping
    public EstadisticasResponse generar() { return service.generar(); }
}
