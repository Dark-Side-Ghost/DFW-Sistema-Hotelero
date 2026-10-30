package com.udb.hotel.repository;

import com.udb.hotel.entity.Reserva;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuario_EmailOrderByIdDesc(String email);

    List<Reserva> findAllByOrderByIdDesc();

    /** Llegadas de hoy, salidas de hoy y huéspedes alojados actualmente. */
    @Query("""
            select r from Reserva r
            where r.fechaEntrada = :hoy or r.fechaSalida = :hoy or r.estado = 'EN_CURSO'
            order by r.id desc
            """)
    List<Reserva> delDia(@Param("hoy") LocalDate hoy);

    /** Cuántas reservas activas se cruzan con las fechas (excluyendo una reserva, útil al modificar). */
    @Query("""
            select count(r) from Reserva r
            where r.habitacion.id = :habitacionId
              and r.estado in ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')
              and r.fechaEntrada < :salida
              and r.fechaSalida > :entrada
              and r.id <> :excluirId
            """)
    long contarSolapadas(@Param("habitacionId") Long habitacionId,
                         @Param("entrada") LocalDate entrada,
                         @Param("salida") LocalDate salida,
                         @Param("excluirId") Long excluirId);
}
