package com.udb.hotel.controller;

import com.udb.hotel.entity.Hotel;
import com.udb.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hoteles")
@RequiredArgsConstructor
@Tag(name = "Hoteles", description = "Lectura: cualquier usuario autenticado. Escritura: solo ADMIN")
public class HotelController {

    private final HotelService service;

    @GetMapping
    public List<Hotel> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Hotel obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel crear(@Valid @RequestBody Hotel hotel) { return service.crear(hotel); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Hotel actualizar(@PathVariable Long id, @Valid @RequestBody Hotel hotel) { return service.actualizar(id, hotel); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
