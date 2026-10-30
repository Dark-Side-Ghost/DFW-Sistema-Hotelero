package com.udb.hotel.controller;

import com.udb.hotel.dto.ReclamoDtos.*;
import com.udb.hotel.service.ReclamoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reclamos")
@RequiredArgsConstructor
@Tag(name = "Reclamos", description = "Clientes crean reclamos; recepción/admin los gestionan")
public class ReclamoController {

    private final ReclamoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReclamoResponse crear(@Valid @RequestBody ReclamoRequest req) { return service.crear(req); }

    @GetMapping("/mios")
    public List<ReclamoResponse> mios() { return service.listarMios(); }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public List<ReclamoResponse> todos() { return service.listarTodos(); }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ReclamoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody ReclamoEstadoRequest req) {
        return service.cambiarEstado(id, req.estado());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
