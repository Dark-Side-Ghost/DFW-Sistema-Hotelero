package com.udb.hotel.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "politicas_cancelacion")
@Getter @Setter @NoArgsConstructor
public class PoliticaCancelacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_politica")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_hotel", nullable = false)
    private Hotel hotel;

    /** Si se cancela con al menos estos días de anticipación no hay cargo. */
    @Column(name = "dias_previos_limite", nullable = false)
    private Integer diasPreviosLimite;

    /** Porcentaje del precio total que se cobra si se cancela fuera del límite. */
    @Column(name = "porcentaje_cargo", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeCargo;
}
