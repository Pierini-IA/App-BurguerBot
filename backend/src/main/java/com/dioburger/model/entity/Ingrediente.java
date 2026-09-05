package com.dioburger.model.entity;

import com.dioburger.model.enums.UnidadMedida;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa un ingrediente utilizado en la preparación de productos.
 * Cada ingrediente tiene un stock actual que se descuenta al confirmar pedidos.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "ingredientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingrediente {

    /**
     * Identificador único del ingrediente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Local al que pertenece este ingrediente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id", nullable = false)
    @JsonIgnoreProperties({"configuracion", "productos", "ingredientes", "mesas", "hibernateLazyInitializer", "handler"})
    private Local local;

    /**
     * Nombre del ingrediente.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Stock actual disponible del ingrediente.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal stockActual;

    /**
     * Unidad de medida del ingrediente (UNIDAD, FETA, HOJA, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnidadMedida unidadMedida;
}
