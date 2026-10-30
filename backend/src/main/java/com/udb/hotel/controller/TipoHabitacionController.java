package com.udb.hotel.controller;

import com.udb.hotel.entity.TipoHabitacion;
import com.udb.hotel.service.TipoHabitacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tipos-habitacion")
@RequiredArgsConstructor
@Tag(name = "Tipos de habitación", description = "Lectura: autenticado. Escritura: solo ADMIN")
public class TipoHabitacionController {

    private final TipoHabitacionService service;

    @GetMapping
    public List<TipoHabitacion> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public TipoHabitacion obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public TipoHabitacion crear(@Valid @RequestBody TipoHabitacion tipo) { return service.crear(tipo); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TipoHabitacion actualizar(@PathVariable Long id, @Valid @RequestBody TipoHabitacion tipo) {
        return service.actualizar(id, tipo);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
