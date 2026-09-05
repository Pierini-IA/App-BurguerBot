package com.dioburger.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa una mesa en un local.
 * Las mesas pueden unirse para grupos grandes mediante reservas.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "mesas",
    uniqueConstraints = @UniqueConstraint(columnNames = {"local_id", "numero"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mesa {

    /**
     * Identificador único de la mesa.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Local al que pertenece esta mesa.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id", nullable = false)
    @JsonIgnoreProperties({"configuracion", "productos", "ingredientes", "mesas", "hibernateLazyInitializer", "handler"})
    private Local local;

    /**
     * Número de la mesa (único por local).
     */
    @Column(nullable = false)
    private Integer numero;

    /**
     * Capacidad máxima de personas de la mesa.
     */
    @Column(nullable = false)
    private Integer capacidad;

    /**
     * Indica si la mesa está disponible para nuevas reservas.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean disponible = true;
}
