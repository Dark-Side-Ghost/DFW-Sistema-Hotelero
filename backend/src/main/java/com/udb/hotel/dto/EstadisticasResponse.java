package com.udb.hotel.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record EstadisticasResponse(
        long totalReservas,
        long reservasMes,
        BigDecimal ingresosTotal,
        BigDecimal ingresosMes,
        double ocupacionPromedio,
        long cancelaciones,
        List<IngresoMes> ingresosMensuales,
        List<OcupacionHotel> ocupacionPorHotel,
        Map<String, Long> reservasPorEstado) {

    public record IngresoMes(String mes, BigDecimal total) {}

    public record OcupacionHotel(String hotel, long ocupadas, long total, double porcentaje) {}
}
