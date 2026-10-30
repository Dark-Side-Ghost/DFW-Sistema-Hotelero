package com.udb.hotel.repository;

import com.udb.hotel.entity.Habitacion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    List<Habitacion> findByHotelId(Long hotelId);

    Optional<Habitacion> findByHotelIdAndNumeroHabitacion(Long hotelId, String numeroHabitacion);

    /**
     * Habitaciones libres: que no estén en mantenimiento, que quepan los huéspedes y que
     * no tengan ninguna reserva activa que se cruce con las fechas pedidas.
     * "texto" ya viene en minúsculas y con % al inicio y al final.
     */
    @Query("""
            select h from Habitacion h
            where (lower(h.hotel.ciudad) like :texto or lower(h.hotel.nombre) like :texto)
              and h.estado <> 'MANTENIMIENTO'
              and h.tipoHabitacion.capacidadMaxima >= :huespedes
              and not exists (
                    select 1 from Reserva r
                    where r.habitacion = h
                      and r.estado in ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')
                      and r.fechaEntrada < :salida
                      and r.fechaSalida > :entrada)
            order by h.hotel.nombre, h.numeroHabitacion
            """)
    List<Habitacion> buscarDisponibles(@Param("texto") String texto,
                                       @Param("entrada") LocalDate entrada,
                                       @Param("salida") LocalDate salida,
                                       @Param("huespedes") Integer huespedes);
}
