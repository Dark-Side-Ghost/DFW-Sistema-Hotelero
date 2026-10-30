package com.udb.hotel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;

    /** ADMIN, RECEPCIONISTA o CLIENTE */
    @Column(name = "nombre_rol", nullable = false, length = 50)
    private String nombre;
}
