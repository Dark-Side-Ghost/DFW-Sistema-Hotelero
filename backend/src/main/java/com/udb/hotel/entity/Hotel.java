package com.udb.hotel.entity;

import com.udb.hotel.util.Reglas;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Se usa directamente como cuerpo de la petición (con sus validaciones) porque es una entidad simple. */
@Entity
@Table(name = "hoteles")
@Getter @Setter @NoArgsConstructor
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hotel")
    private Long id;

    @NotBlank(message = "El nombre del hotel es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no puede superar 100 caracteres")
    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100, message = "El país no puede superar 100 caracteres")
    @Column(name = "pais", nullable = false, length = 100)
    private String pais;

    @NotNull(message = "La categoría (estrellas) es obligatoria")
    @Min(value = 1, message = "La categoría mínima es 1 estrella")
    @Max(value = 5, message = "La categoría máxima es 5 estrellas")
    @Column(name = "categoria")
    private Integer categoria;

    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    @Column(name = "direccion", length = 255)
    private String direccion;

    @Pattern(regexp = Reglas.TELEFONO_HOTEL, message = "El teléfono no es válido (ejemplo: 2260-0000)")
    @Column(name = "telefono", length = 20)
    private String telefono;
}
