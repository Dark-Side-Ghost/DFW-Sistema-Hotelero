package com.udb.hotel.entity;

import com.udb.hotel.util.Fechas;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reclamos")
@Getter @Setter @NoArgsConstructor
public class Reclamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reclamo")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_huesped", nullable = false)
    private Huesped huesped;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    /** Usuario del staff que atiende el reclamo (puede ser null al inicio). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario_atiende")
    private Usuario usuarioAtiende;

    /** LIMPIEZA, FACTURACION, INFRAESTRUCTURA o ATENCION */
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /** PENDIENTE, EN_PROCESO, RESUELTO o RECHAZADO */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void antesDeGuardar() {
        if (fechaCreacion == null) fechaCreacion = Fechas.ahora();
    }
}
