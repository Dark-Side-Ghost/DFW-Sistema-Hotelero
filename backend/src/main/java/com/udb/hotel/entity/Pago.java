package com.udb.hotel.entity;

import com.udb.hotel.util.Fechas;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pagos")
@Getter @Setter @NoArgsConstructor
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    /** TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA o EFECTIVO */
    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    /** PENDIENTE, COMPLETADO, RECHAZADO o REEMBOLSADO */
    @Column(name = "estado_pago", nullable = false, length = 20)
    private String estado = "COMPLETADO";

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @PrePersist
    void antesDeGuardar() {
        if (fechaPago == null) fechaPago = Fechas.ahora();
    }
}
