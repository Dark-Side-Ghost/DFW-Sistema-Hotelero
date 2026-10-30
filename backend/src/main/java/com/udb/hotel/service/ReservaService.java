package com.udb.hotel.service;

import com.udb.hotel.dto.ReservaDtos.*;
import com.udb.hotel.entity.*;
import com.udb.hotel.exception.ApiException;
import com.udb.hotel.repository.*;
import com.udb.hotel.util.Fechas;
import com.udb.hotel.util.SecurityUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Corazón del sistema: búsqueda/validación de disponibilidad, creación, pago, modificación,
 * cancelación (con cargo o reembolso según la política), check-in y check-out.
 */
@Service
@RequiredArgsConstructor
public class ReservaService {

    private static final int MAX_NOCHES = 30;
    private static final List<String> ACTIVAS = List.of("PENDIENTE", "CONFIRMADA");

    private static final Map<String, Pattern> DOCUMENTOS = Map.of(
            "DUI", Pattern.compile("^\\d{8}-\\d$"),
            "PASAPORTE", Pattern.compile("^[A-Z0-9]{6,15}$"),
            "DNI", Pattern.compile("^[A-Z0-9-]{6,20}$"),
            "NIT", Pattern.compile("^\\d{4}-\\d{6}-\\d{3}-\\d$"));
    private static final Map<String, String> EJEMPLOS = Map.of(
            "DUI", "01234567-8", "PASAPORTE", "A1234567", "DNI", "12345678-9", "NIT", "0614-010190-101-1");

    private final ReservaRepository reservaRepo;
    private final HabitacionRepository habitacionRepo;
    private final HuespedRepository huespedRepo;
    private final PoliticaRepository politicaRepo;
    private final ServicioRepository servicioRepo;
    private final PagoRepository pagoRepo;
    private final UsuarioService usuarioService;

    // ------------------------------------------------------------------ crear

    @Transactional
    public ReservaResponse crear(ReservaRequest req) {
        Usuario usuario = usuarioService.actual();
        long noches = validarFechas(req.fechaEntrada(), req.fechaSalida());

        Habitacion hab = habitacionRepo.findById(req.idHabitacion())
                .orElseThrow(() -> ApiException.notFound("La habitación seleccionada no existe."));
        validarHabitacion(hab, req.numeroHuespedes(), req.fechaEntrada(), req.fechaSalida(), 0L);

        Huesped huesped = guardarHuesped(req.huesped());
        PoliticaCancelacion politica = politicaRepo.findFirstByHotelIdOrderByIdAsc(hab.getHotel().getId())
                .orElseGet(() -> politicaPorDefecto(hab.getHotel()));

        Reserva r = new Reserva();
        r.setHuesped(huesped);
        r.setHabitacion(hab);
        r.setUsuario(usuario);
        r.setPolitica(politica);
        r.setFechaEntrada(req.fechaEntrada());
        r.setFechaSalida(req.fechaSalida());
        r.setNumeroHuespedes(req.numeroHuespedes());
        r.setEstado("PENDIENTE");

        BigDecimal extras = BigDecimal.ZERO;
        if (req.servicios() != null) {
            for (ServicioSolicitado s : req.servicios()) {
                Servicio sv = servicioRepo.findById(s.idServicio())
                        .orElseThrow(() -> ApiException.notFound("El servicio adicional con id " + s.idServicio() + " no existe."));
                ReservaServicio linea = new ReservaServicio();
                linea.setReserva(r);
                linea.setServicio(sv);
                linea.setCantidad(s.cantidad());
                linea.setSubtotal(sv.getPrecio().multiply(BigDecimal.valueOf(s.cantidad())));
                r.getServicios().add(linea);
                extras = extras.add(linea.getSubtotal());
            }
        }
        r.setPrecioTotal(hab.getPrecioBaseNoche().multiply(BigDecimal.valueOf(noches)).add(extras));
        return aRespuesta(reservaRepo.save(r));
    }

    // ---------------------------------------------------------------- consultas

    public List<ReservaResponse> listarMias() {
        return reservaRepo.findByUsuario_EmailOrderByIdDesc(SecurityUtil.emailActual()).stream().map(this::aRespuesta).toList();
    }

    public List<ReservaResponse> listarTodas() {
        return reservaRepo.findAllByOrderByIdDesc().stream().map(this::aRespuesta).toList();
    }

    public List<ReservaResponse> listarDelDia() {
        return reservaRepo.delDia(Fechas.hoy()).stream().map(this::aRespuesta).toList();
    }

    public ReservaResponse obtener(Long id) {
        return aRespuesta(obtenerEntidad(id));
    }

    /** Busca la reserva y verifica que el usuario sea su dueño (o personal del hotel). */
    public Reserva obtenerEntidad(Long id) {
        Reserva r = reservaRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("La reserva con id " + id + " no existe."));
        if (!SecurityUtil.esStaff() && !r.getUsuario().getEmail().equalsIgnoreCase(SecurityUtil.emailActual())) {
            throw ApiException.forbidden("No tienes permiso para ver o modificar esta reserva.");
        }
        return r;
    }

    // --------------------------------------------------------------- modificar

    @Transactional
    public ReservaResponse modificar(Long id, ModificarRequest req) {
        Reserva r = obtenerEntidad(id);
        if (!ACTIVAS.contains(r.getEstado())) {
            throw ApiException.badRequest("Solo se pueden modificar reservas pendientes o confirmadas.");
        }
        long noches = validarFechas(req.fechaEntrada(), req.fechaSalida());
        validarHabitacion(r.getHabitacion(), req.numeroHuespedes(), req.fechaEntrada(), req.fechaSalida(), r.getId());

        BigDecimal extras = r.getServicios().stream().map(ReservaServicio::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        r.setFechaEntrada(req.fechaEntrada());
        r.setFechaSalida(req.fechaSalida());
        r.setNumeroHuespedes(req.numeroHuespedes());
        r.setPrecioTotal(r.getHabitacion().getPrecioBaseNoche().multiply(BigDecimal.valueOf(noches)).add(extras));
        return aRespuesta(reservaRepo.save(r));
    }

    // -------------------------------------------------------------------- pago

    /**
     * Registra un pago por el saldo pendiente. Tarjeta/transferencia -> pago COMPLETADO y reserva CONFIRMADA.
     * Efectivo hecho por un cliente -> pago PENDIENTE (lo cobra recepción). Efectivo en recepción -> COMPLETADO.
     */
    @Transactional
    public ReservaResponse pagar(Long id, PagoRequest req) {
        Reserva r = obtenerEntidad(id);
        if (!ACTIVAS.contains(r.getEstado())) {
            throw ApiException.badRequest("Solo se pueden pagar reservas pendientes o confirmadas.");
        }
        validarTarjeta(req);

        BigDecimal saldo = r.getPrecioTotal().subtract(sumaPagos(r.getId(), "COMPLETADO"));
        if (saldo.signum() <= 0) {
            throw ApiException.badRequest("Esta reserva ya está pagada en su totalidad.");
        }
        Pago pago = pagoRepo.findByReservaId(id).stream()
                .filter(p -> "PENDIENTE".equals(p.getEstado()))
                .findFirst()
                .orElseGet(() -> {
                    Pago nuevo = new Pago();
                    nuevo.setReserva(r);
                    return nuevo;
                });
        pago.setMetodoPago(req.metodoPago());
        pago.setMonto(saldo);

        boolean diferido = "EFECTIVO".equals(req.metodoPago()) && !SecurityUtil.esStaff();
        if (diferido) {
            pago.setEstado("PENDIENTE");
        } else {
            pago.setEstado("COMPLETADO");
            pago.setFechaPago(Fechas.ahora());
            r.setEstado("CONFIRMADA");
            reservaRepo.save(r);
        }
        pagoRepo.save(pago);
        return aRespuesta(r);
    }

    // ---------------------------------------------------------------- cancelar

    @Transactional
    public ReservaResponse cancelar(Long id, String motivo) {
        Reserva r = obtenerEntidad(id);
        if (!ACTIVAS.contains(r.getEstado())) {
            throw ApiException.badRequest("Solo se pueden cancelar reservas pendientes o confirmadas.");
        }
        PoliticaCancelacion pol = r.getPolitica();
        long diasAnticipacion = ChronoUnit.DAYS.between(Fechas.hoy(), r.getFechaEntrada());
        BigDecimal pagado = sumaPagos(r.getId(), "COMPLETADO");
        BigDecimal cargo = diasAnticipacion >= pol.getDiasPreviosLimite()
                ? BigDecimal.ZERO
                : r.getPrecioTotal().multiply(pol.getPorcentajeCargo()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal reembolso = pagado.subtract(cargo).max(BigDecimal.ZERO);

        for (Pago p : pagoRepo.findByReservaId(id)) {
            if ("COMPLETADO".equals(p.getEstado()) && reembolso.signum() > 0) {
                p.setEstado("REEMBOLSADO");
                pagoRepo.save(p);
            } else if ("PENDIENTE".equals(p.getEstado())) {
                p.setEstado("RECHAZADO");
                pagoRepo.save(p);
            }
        }
        r.setEstado("CANCELADA");
        r.setFechaCancelacion(Fechas.ahora());
        r.setMotivoCancelacion(motivo.trim());
        r.setMontoReembolso(reembolso);
        return aRespuesta(reservaRepo.save(r));
    }

    // ------------------------------------------------------- check-in / check-out

    @Transactional
    public ReservaResponse checkIn(Long id) {
        Reserva r = obtenerEntidad(id);
        if ("PENDIENTE".equals(r.getEstado())) {
            throw ApiException.badRequest("La reserva aún no tiene el pago registrado. Registra el pago antes del check-in.");
        }
        if (!"CONFIRMADA".equals(r.getEstado())) {
            throw ApiException.badRequest("Solo se puede hacer check-in a reservas confirmadas.");
        }
        if (r.getFechaEntrada().isAfter(Fechas.hoy())) {
            throw ApiException.badRequest("El check-in solo puede registrarse desde la fecha de entrada (" + r.getFechaEntrada() + ").");
        }
        BigDecimal saldo = r.getPrecioTotal().subtract(sumaPagos(r.getId(), "COMPLETADO"));
        if (saldo.signum() > 0) {
            throw ApiException.badRequest("La reserva tiene un saldo pendiente de $" + saldo + ". Registra el pago primero.");
        }
        r.setEstado("EN_CURSO");
        r.getHabitacion().setEstado("OCUPADA");
        habitacionRepo.save(r.getHabitacion());
        return aRespuesta(reservaRepo.save(r));
    }

    @Transactional
    public ReservaResponse checkOut(Long id) {
        Reserva r = obtenerEntidad(id);
        if (!"EN_CURSO".equals(r.getEstado())) {
            throw ApiException.badRequest("Solo se puede hacer check-out a reservas con check-in realizado.");
        }
        r.setEstado("FINALIZADA");
        r.getHabitacion().setEstado("DISPONIBLE");
        habitacionRepo.save(r.getHabitacion());
        return aRespuesta(reservaRepo.save(r));
    }

    // ------------------------------------------------------------- validaciones

    /** Reglas de congruencia: no fechas pasadas, salida posterior a entrada, estadía máxima. Devuelve las noches. */
    private long validarFechas(LocalDate entrada, LocalDate salida) {
        if (entrada.isBefore(Fechas.hoy())) {
            throw ApiException.badRequest("La fecha de entrada no puede ser anterior a hoy.");
        }
        if (!salida.isAfter(entrada)) {
            throw ApiException.badRequest("La fecha de salida debe ser posterior a la fecha de entrada.");
        }
        long noches = ChronoUnit.DAYS.between(entrada, salida);
        if (noches > MAX_NOCHES) {
            throw ApiException.badRequest("La estadía máxima es de " + MAX_NOCHES + " noches.");
        }
        return noches;
    }

    private void validarHabitacion(Habitacion hab, int huespedes, LocalDate entrada, LocalDate salida, Long excluirReservaId) {
        if ("MANTENIMIENTO".equals(hab.getEstado())) {
            throw ApiException.badRequest("La habitación " + hab.getNumeroHabitacion() + " está en mantenimiento y no se puede reservar.");
        }
        int capacidad = hab.getTipoHabitacion().getCapacidadMaxima();
        if (huespedes > capacidad) {
            throw ApiException.badRequest("La habitación admite un máximo de " + capacidad + " huésped(es).");
        }
        if (reservaRepo.contarSolapadas(hab.getId(), entrada, salida, excluirReservaId) > 0) {
            throw ApiException.conflict("La habitación ya está reservada en las fechas seleccionadas. Elige otras fechas u otra habitación.");
        }
    }

    private Huesped guardarHuesped(HuespedRequest h) {
        if (Period.between(h.fechaNacimiento(), Fechas.hoy()).getYears() < 18) {
            throw ApiException.badRequest("El huésped titular debe ser mayor de edad (18 años o más).");
        }
        String numero = h.numeroDocumento().trim().toUpperCase();
        if (!DOCUMENTOS.get(h.tipoDocumento()).matcher(numero).matches()) {
            throw ApiException.badRequest("El número de documento no tiene un formato válido para "
                    + h.tipoDocumento() + ". Ejemplo: " + EJEMPLOS.get(h.tipoDocumento()));
        }
        Huesped e = huespedRepo.findByNumeroDocumento(numero).orElseGet(Huesped::new);
        e.setNombreCompleto(h.nombreCompleto().trim());
        e.setFechaNacimiento(h.fechaNacimiento());
        e.setTipoDocumento(h.tipoDocumento());
        e.setNumeroDocumento(numero);
        e.setTelefono(h.telefono());
        e.setCorreoElectronico(h.correoElectronico().trim());
        return huespedRepo.save(e);
    }

    /** El pago es simulado: los datos de la tarjeta solo se validan, nunca se guardan. */
    private void validarTarjeta(PagoRequest p) {
        if (!p.metodoPago().startsWith("TARJETA")) return;
        String numero = p.numeroTarjeta() == null ? "" : p.numeroTarjeta().replaceAll("\\s", "");
        if (!numero.matches("\\d{13,19}")) {
            throw ApiException.badRequest("El número de tarjeta debe tener entre 13 y 19 dígitos.");
        }
        if (p.cvv() == null || !p.cvv().matches("\\d{3,4}")) {
            throw ApiException.badRequest("El código de seguridad (CVV) debe tener 3 o 4 dígitos.");
        }
        if (p.titular() == null || p.titular().isBlank()) {
            throw ApiException.badRequest("El nombre del titular de la tarjeta es obligatorio.");
        }
        if (p.vencimiento() == null || !p.vencimiento().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw ApiException.badRequest("El vencimiento debe tener el formato MM/AA (ejemplo: 08/29).");
        }
        int mes = Integer.parseInt(p.vencimiento().substring(0, 2));
        int anio = 2000 + Integer.parseInt(p.vencimiento().substring(3, 5));
        if (YearMonth.of(anio, mes).isBefore(YearMonth.from(Fechas.hoy()))) {
            throw ApiException.badRequest("La tarjeta está vencida.");
        }
    }

    private PoliticaCancelacion politicaPorDefecto(Hotel hotel) {
        PoliticaCancelacion p = new PoliticaCancelacion();
        p.setHotel(hotel);
        p.setDiasPreviosLimite(2);
        p.setPorcentajeCargo(new BigDecimal("50.00"));
        return politicaRepo.save(p);
    }

    private BigDecimal sumaPagos(Long reservaId, String estado) {
        return pagoRepo.findByReservaId(reservaId).stream()
                .filter(p -> estado.equals(p.getEstado()))
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ------------------------------------------------------------------ mapeo

    private ReservaResponse aRespuesta(Reserva r) {
        List<Pago> pagos = pagoRepo.findByReservaId(r.getId());
        BigDecimal pagado = pagos.stream().filter(p -> "COMPLETADO".equals(p.getEstado()))
                .map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean reembolsado = pagos.stream().anyMatch(p -> "REEMBOLSADO".equals(p.getEstado()));
        boolean pendiente = pagos.stream().anyMatch(p -> "PENDIENTE".equals(p.getEstado()));

        String estadoPago;
        if (reembolsado && pagado.signum() == 0) estadoPago = "REEMBOLSADO";
        else if (pagado.compareTo(r.getPrecioTotal()) >= 0) estadoPago = "PAGADO";
        else if (pagado.signum() > 0) estadoPago = "PARCIAL";
        else if (pendiente) estadoPago = "PENDIENTE";
        else estadoPago = "SIN_PAGO";

        Habitacion h = r.getHabitacion();
        Hotel hotel = h.getHotel();
        long noches = ChronoUnit.DAYS.between(r.getFechaEntrada(), r.getFechaSalida());
        List<ServicioReservaResponse> servicios = r.getServicios().stream()
                .map(s -> new ServicioReservaResponse(s.getServicio().getNombre(), s.getCantidad(), s.getSubtotal()))
                .toList();

        return new ReservaResponse(
                r.getId(), "RSV-" + String.format("%04d", r.getId()), r.getEstado(), estadoPago,
                r.getFechaEntrada(), r.getFechaSalida(), noches, r.getNumeroHuespedes(),
                r.getPrecioTotal(), pagado, r.getMontoReembolso(), r.getMotivoCancelacion(),
                hotel.getNombre(), hotel.getCiudad(), h.getId(), h.getNumeroHabitacion(),
                h.getTipoHabitacion().getNombre(), h.getPrecioBaseNoche(),
                r.getHuesped().getNombreCompleto(), r.getHuesped().getNumeroDocumento(), r.getHuesped().getCorreoElectronico(),
                r.getUsuario().getEmail(), r.getPolitica().getDiasPreviosLimite(), r.getPolitica().getPorcentajeCargo(),
                servicios, r.getFechaCreacion());
    }
}
