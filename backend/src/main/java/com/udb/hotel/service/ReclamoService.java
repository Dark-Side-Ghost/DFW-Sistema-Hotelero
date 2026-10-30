package com.udb.hotel.service;

import com.udb.hotel.dto.ReclamoDtos.*;
import com.udb.hotel.entity.Reclamo;
import com.udb.hotel.entity.Reserva;
import com.udb.hotel.exception.ApiException;
import com.udb.hotel.repository.ReclamoRepository;
import com.udb.hotel.util.SecurityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReclamoService {

    private final ReclamoRepository reclamoRepo;
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    @Transactional
    public ReclamoResponse crear(ReclamoRequest req) {
        // obtenerEntidad ya verifica que la reserva sea del usuario (o que sea personal del hotel)
        Reserva reserva = reservaService.obtenerEntidad(req.idReserva());
        Reclamo r = new Reclamo();
        r.setReserva(reserva);
        r.setHuesped(reserva.getHuesped());
        r.setTipo(req.tipo());
        r.setDescripcion(req.descripcion().trim());
        r.setEstado("PENDIENTE");
        return aRespuesta(reclamoRepo.save(r));
    }

    public List<ReclamoResponse> listarTodos() {
        return reclamoRepo.findAllByOrderByIdDesc().stream().map(this::aRespuesta).toList();
    }

    public List<ReclamoResponse> listarMios() {
        return reclamoRepo.findByReserva_Usuario_EmailOrderByIdDesc(SecurityUtil.emailActual()).stream().map(this::aRespuesta).toList();
    }

    @Transactional
    public ReclamoResponse cambiarEstado(Long id, String estado) {
        Reclamo r = buscar(id);
        r.setEstado(estado);
        r.setUsuarioAtiende(usuarioService.actual());
        return aRespuesta(reclamoRepo.save(r));
    }

    @Transactional
    public void eliminar(Long id) {
        reclamoRepo.delete(buscar(id));
        reclamoRepo.flush();
    }

    private Reclamo buscar(Long id) {
        return reclamoRepo.findById(id).orElseThrow(() -> ApiException.notFound("El reclamo con id " + id + " no existe."));
    }

    private ReclamoResponse aRespuesta(Reclamo r) {
        Reserva res = r.getReserva();
        String atiende = r.getUsuarioAtiende() == null ? null
                : r.getUsuarioAtiende().getNombres() + " " + r.getUsuarioAtiende().getApellidos();
        return new ReclamoResponse(r.getId(), r.getTipo(), r.getDescripcion(), r.getEstado(), r.getFechaCreacion(),
                res.getId(), "RSV-" + String.format("%04d", res.getId()), r.getHuesped().getNombreCompleto(),
                res.getHabitacion().getHotel().getNombre(), atiende);
    }
}
