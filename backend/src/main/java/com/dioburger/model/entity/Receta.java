package com.dioburger.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa la relación entre un producto y un ingrediente.
 * Define la receta: qué ingredientes y en qué cantidad se necesitan para preparar un producto.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "recetas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receta {

    /**
     * Identificador único de la receta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Producto al que pertenece esta receta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /**
     * Ingrediente necesario para el producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    @JsonIgnoreProperties({"local", "hibernateLazyInitializer", "handler"})
    private Ingrediente ingrediente;

    /**
     * Cantidad del ingrediente requerida para preparar el producto.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadRequerida;
}
