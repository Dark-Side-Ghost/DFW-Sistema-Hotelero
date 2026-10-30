package com.udb.hotel.service;

import com.udb.hotel.dto.CatalogoDtos.HabitacionRequest;
import com.udb.hotel.entity.Habitacion;
import com.udb.hotel.entity.Hotel;
import com.udb.hotel.entity.Servicio;
import com.udb.hotel.entity.TipoHabitacion;
import com.udb.hotel.exception.ApiException;
import com.udb.hotel.repository.HabitacionRepository;
import com.udb.hotel.repository.HotelRepository;
import com.udb.hotel.repository.ServicioRepository;
import com.udb.hotel.repository.TipoHabitacionRepository;
import com.udb.hotel.util.Fechas;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HabitacionService {

    private final HabitacionRepository repo;
    private final HotelRepository hotelRepo;
    private final TipoHabitacionRepository tipoRepo;
    private final ServicioRepository servicioRepo;

    public List<Habitacion> listar(Long hotelId) {
        return hotelId == null ? repo.findAll() : repo.findByHotelId(hotelId);
    }

    public Habitacion obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("La habitación con id " + id + " no existe."));
    }

    @Transactional
    public Habitacion crear(HabitacionRequest req) {
        return guardar(new Habitacion(), req);
    }

    @Transactional
    public Habitacion actualizar(Long id, HabitacionRequest req) {
        return guardar(obtener(id), req);
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(obtener(id));
        repo.flush();
    }

    /** Búsqueda de habitaciones libres (ubicación, fechas, huéspedes y tipo opcional). */
    public List<Habitacion> disponibles(String ciudad, LocalDate entrada, LocalDate salida, Integer huespedes, Long tipoId) {
        if (entrada.isBefore(Fechas.hoy())) {
            throw ApiException.badRequest("La fecha de entrada no puede ser anterior a hoy.");
        }
        if (!salida.isAfter(entrada)) {
            throw ApiException.badRequest("La fecha de salida debe ser posterior a la fecha de entrada.");
        }
        if (ChronoUnit.DAYS.between(entrada, salida) > 30) {
            throw ApiException.badRequest("La estadía máxima es de 30 noches.");
        }
        if (huespedes == null || huespedes < 1 || huespedes > 10) {
            throw ApiException.badRequest("El número de huéspedes debe ser un entero entre 1 y 10.");
        }
        String texto = "%" + (ciudad == null ? "" : ciudad.trim().toLowerCase()) + "%";
        return repo.buscarDisponibles(texto, entrada, salida, huespedes).stream()
                .filter(h -> tipoId == null || h.getTipoHabitacion().getId().equals(tipoId))
                .toList();
    }

    private Habitacion guardar(Habitacion h, HabitacionRequest req) {
        Hotel hotel = hotelRepo.findById(req.idHotel())
                .orElseThrow(() -> ApiException.notFound("El hotel indicado no existe."));
        TipoHabitacion tipo = tipoRepo.findById(req.idTipoHabitacion())
                .orElseThrow(() -> ApiException.notFound("El tipo de habitación indicado no existe."));
        String numero = req.numeroHabitacion().trim().toUpperCase();
        repo.findByHotelIdAndNumeroHabitacion(hotel.getId(), numero)
                .filter(otra -> !otra.getId().equals(h.getId()))
                .ifPresent(otra -> {
                    throw ApiException.conflict("Ya existe la habitación " + numero + " en ese hotel.");
                });

        Set<Servicio> servicios = new HashSet<>();
        if (req.serviciosIds() != null && !req.serviciosIds().isEmpty()) {
            servicios.addAll(servicioRepo.findAllById(req.serviciosIds()));
            if (servicios.size() != new HashSet<>(req.serviciosIds()).size()) {
                throw ApiException.badRequest("Alguno de los servicios indicados no existe.");
            }
        }
        h.setHotel(hotel);
        h.setTipoHabitacion(tipo);
        h.setNumeroHabitacion(numero);
        h.setPrecioBaseNoche(req.precioBaseNoche());
        h.setEstado(req.estado() == null ? "DISPONIBLE" : req.estado());
        h.setServicios(servicios);
        return repo.save(h);
    }
}
