package com.udb.hotel.controller;

import com.udb.hotel.entity.Servicio;
import com.udb.hotel.service.ServicioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
@Tag(name = "Servicios adicionales", description = "Lectura: autenticado. Escritura: solo ADMIN")
public class ServicioController {

    private final ServicioService service;

    @GetMapping
    public List<Servicio> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Servicio obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Servicio crear(@Valid @RequestBody Servicio servicio) { return service.crear(servicio); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Servicio actualizar(@PathVariable Long id, @Valid @RequestBody Servicio servicio) {
        return service.actualizar(id, servicio);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
