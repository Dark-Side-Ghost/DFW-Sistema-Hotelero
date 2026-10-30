package com.udb.hotel.controller;

import com.udb.hotel.dto.ReservaDtos.*;
import com.udb.hotel.service.ReservaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Reservar, pagar, modificar, cancelar, check-in y check-out")
public class ReservaController {

    private final ReservaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(@Valid @RequestBody ReservaRequest req) { return service.crear(req); }

    @GetMapping("/mias")
    public List<ReservaResponse> mias() { return service.listarMias(); }

    @GetMapping("/hoy")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public List<ReservaResponse> hoy() { return service.listarDelDia(); }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public List<ReservaResponse> todas() { return service.listarTodas(); }

    @GetMapping("/{id}")
    public ReservaResponse obtener(@PathVariable Long id) { return service.obtener(id); }

    @PutMapping("/{id}")
    public ReservaResponse modificar(@PathVariable Long id, @Valid @RequestBody ModificarRequest req) {
        return service.modificar(id, req);
    }

    @PostMapping("/{id}/pagos")
    public ReservaResponse pagar(@PathVariable Long id, @Valid @RequestBody PagoRequest req) {
        return service.pagar(id, req);
    }

    @PostMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelarRequest req) {
        return service.cancelar(id, req.motivo());
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ReservaResponse checkIn(@PathVariable Long id) { return service.checkIn(id); }

    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ReservaResponse checkOut(@PathVariable Long id) { return service.checkOut(id); }
}
