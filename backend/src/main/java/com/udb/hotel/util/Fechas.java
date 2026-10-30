package com.udb.hotel.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Fecha "de hoy" siempre en la zona horaria de El Salvador, sin depender de la zona del servidor. */
public final class Fechas {
    public static final ZoneId ZONA = ZoneId.of("America/El_Salvador");

    private Fechas() {}

    public static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA);
    }
}
