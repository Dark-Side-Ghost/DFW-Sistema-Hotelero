package com.udb.hotel.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "habitaciones")
@Getter @Setter @NoArgsConstructor
public class Habitacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habitacion")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_hotel", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_tipo_habitacion", nullable = false)
    private TipoHabitacion tipoHabitacion;

    @Column(name = "numero_habitacion", nullable = false, length = 20)
    private String numeroHabitacion;

    @Column(name = "precio_base_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBaseNoche;

    /** DISPONIBLE, OCUPADA o MANTENIMIENTO */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "DISPONIBLE";

    /** Servicios incluidos en la habitación (tabla habitacion_servicio). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "habitacion_servicio",
            joinColumns = @JoinColumn(name = "id_habitacion"),
            inverseJoinColumns = @JoinColumn(name = "id_servicio"))
    private Set<Servicio> servicios = new HashSet<>();
}
