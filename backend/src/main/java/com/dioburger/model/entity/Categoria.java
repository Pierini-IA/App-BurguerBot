package com.dioburger.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una categoría de productos.
 * Permite organizar el menú en secciones (Hamburguesas, Pizzas, Bebidas, etc.).
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    /**
     * Identificador único de la categoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de la categoría.
     * Ejemplos: "Hamburguesas", "Pizzas", "Bebidas", "Postres"
     */
    @Column(nullable = false, length = 100)
    private String nombre;

    /**
     * Descripción opcional de la categoría.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Orden de visualización en el menú.
     * Menor número = mayor prioridad.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;

    /**
     * Indica si la categoría está activa y visible.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Local al que pertenece esta categoría.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id", nullable = false)
    private Local local;

    /**
     * Productos asociados a esta categoría.
     */
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();

    /**
     * Extras asociados a esta categoría (opcional).
     */
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Extra> extras = new ArrayList<>();

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha de última actualización del registro.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Se ejecuta antes de persistir la entidad.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Se ejecuta antes de actualizar la entidad.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
