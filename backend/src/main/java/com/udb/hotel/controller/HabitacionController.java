package com.udb.hotel.controller;

import com.udb.hotel.dto.CatalogoDtos.HabitacionRequest;
import com.udb.hotel.entity.Habitacion;
import com.udb.hotel.service.HabitacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/habitaciones")
@RequiredArgsConstructor
@Tag(name = "Habitaciones", description = "Consulta y búsqueda: autenticado. Escritura: solo ADMIN")
public class HabitacionController {

    private final HabitacionService service;

    @GetMapping
    public List<Habitacion> listar(@RequestParam(required = false) Long hotelId) { return service.listar(hotelId); }

    @GetMapping("/disponibles")
    public List<Habitacion> disponibles(@RequestParam(required = false, defaultValue = "") String ciudad,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entrada,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
                                        @RequestParam Integer huespedes,
                                        @RequestParam(required = false) Long tipoId) {
        return service.disponibles(ciudad, entrada, salida, huespedes, tipoId);
    }

    @GetMapping("/{id}")
    public Habitacion obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Habitacion crear(@Valid @RequestBody HabitacionRequest req) { return service.crear(req); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Habitacion actualizar(@PathVariable Long id, @Valid @RequestBody HabitacionRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
