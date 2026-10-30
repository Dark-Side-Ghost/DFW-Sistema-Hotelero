package com.udb.hotel.controller;

import com.udb.hotel.dto.CatalogoDtos.PoliticaRequest;
import com.udb.hotel.entity.PoliticaCancelacion;
import com.udb.hotel.service.PoliticaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/politicas")
@RequiredArgsConstructor
@Tag(name = "Políticas de cancelación", description = "Lectura: autenticado. Escritura: solo ADMIN")
public class PoliticaController {

    private final PoliticaService service;

    @GetMapping
    public List<PoliticaCancelacion> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public PoliticaCancelacion obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public PoliticaCancelacion crear(@Valid @RequestBody PoliticaRequest req) { return service.crear(req); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PoliticaCancelacion actualizar(@PathVariable Long id, @Valid @RequestBody PoliticaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
