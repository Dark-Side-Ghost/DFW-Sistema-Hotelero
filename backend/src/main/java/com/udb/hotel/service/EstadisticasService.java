package com.udb.hotel.service;

import com.udb.hotel.dto.EstadisticasResponse;
import com.udb.hotel.dto.EstadisticasResponse.IngresoMes;
import com.udb.hotel.dto.EstadisticasResponse.OcupacionHotel;
import com.udb.hotel.entity.Habitacion;
import com.udb.hotel.entity.Pago;
import com.udb.hotel.entity.Reserva;
import com.udb.hotel.repository.HabitacionRepository;
import com.udb.hotel.repository.HotelRepository;
import com.udb.hotel.repository.PagoRepository;
import com.udb.hotel.repository.ReservaRepository;
import com.udb.hotel.util.Fechas;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Reportes básicos: ocupación, ingresos y cancelaciones. Los volúmenes son pequeños, se calculan en memoria. */
@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private final ReservaRepository reservaRepo;
    private final PagoRepository pagoRepo;
    private final HabitacionRepository habitacionRepo;
    private final HotelRepository hotelRepo;

    public EstadisticasResponse generar() {
        LocalDate hoy = Fechas.hoy();
        YearMonth mesActual = YearMonth.from(hoy);
        List<Reserva> reservas = reservaRepo.findAll();
        List<Pago> pagos = pagoRepo.findByEstado("COMPLETADO");
        List<Habitacion> habitaciones = habitacionRepo.findAll();

        long reservasMes = reservas.stream().filter(r -> YearMonth.from(r.getFechaCreacion()).equals(mesActual)).count();
        long cancelaciones = reservas.stream().filter(r -> "CANCELADA".equals(r.getEstado())).count();
        BigDecimal ingresosTotal = sumar(pagos, null);
        BigDecimal ingresosMes = sumar(pagos, mesActual);

        List<IngresoMes> ingresosMensuales = IntStream.rangeClosed(0, 5)
                .mapToObj(i -> mesActual.minusMonths(5 - i))
                .map(m -> new IngresoMes(m.toString(), sumar(pagos, m)))
                .toList();

        List<OcupacionHotel> porHotel = hotelRepo.findAll().stream().map(h -> {
            long total = habitaciones.stream().filter(x -> x.getHotel().getId().equals(h.getId())).count();
            long ocupadas = reservas.stream()
                    .filter(r -> r.getHabitacion().getHotel().getId().equals(h.getId()) && vigenteHoy(r, hoy))
                    .map(r -> r.getHabitacion().getId()).distinct().count();
            return new OcupacionHotel(h.getNombre(), ocupadas, total, porcentaje(ocupadas, total));
        }).toList();

        long totalHab = habitaciones.size();
        long ocupadasHoy = reservas.stream().filter(r -> vigenteHoy(r, hoy)).map(r -> r.getHabitacion().getId()).distinct().count();

        Map<String, Long> porEstado = reservas.stream().collect(Collectors.groupingBy(Reserva::getEstado, Collectors.counting()));

        return new EstadisticasResponse(reservas.size(), reservasMes, ingresosTotal, ingresosMes,
                porcentaje(ocupadasHoy, totalHab), cancelaciones, ingresosMensuales, porHotel, porEstado);
    }

    /** Reserva que ocupa una habitación hoy (confirmada o con check-in, y hoy está entre entrada y salida). */
    private boolean vigenteHoy(Reserva r, LocalDate hoy) {
        return ("CONFIRMADA".equals(r.getEstado()) || "EN_CURSO".equals(r.getEstado()))
                && !r.getFechaEntrada().isAfter(hoy) && r.getFechaSalida().isAfter(hoy);
    }

    private BigDecimal sumar(List<Pago> pagos, YearMonth mes) {
        return pagos.stream()
                .filter(p -> mes == null || (p.getFechaPago() != null && YearMonth.from(p.getFechaPago()).equals(mes)))
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double porcentaje(long parte, long total) {
        return total == 0 ? 0 : Math.round(parte * 1000.0 / total) / 10.0;
    }
}
