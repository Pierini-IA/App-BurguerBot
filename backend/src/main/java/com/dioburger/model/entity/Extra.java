package com.dioburger.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un extra o adicional para productos.
 * Los extras tienen un precio adicional y pueden agregarse a productos.
 * 
 * Ejemplos: Queso extra, Bacon, Papas fritas, Aceitunas, etc.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "extras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Extra {

    /**
     * Identificador único del extra.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del extra.
     * Ejemplos: "Queso Cheddar Extra", "Bacon", "Papas Fritas"
     */
    @Column(nullable = false, length = 100)
    private String nombre;

    /**
     * Descripción opcional del extra.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Precio adicional que se suma al producto base.
     * Debe ser mayor o igual a 0.
     */
    @Column(name = "precio_adicional", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAdicional;

    /**
     * Indica si el extra está disponible.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Local al que pertenece este extra.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id", nullable = false)
    @JsonIgnoreProperties({"productos", "ingredientes", "usuarios", "pedidos", "reservas", "mesas"})
    private Local local;

    /**
     * Categoría opcional para agrupar extras.
     * Ejemplo: "Extras de Carne", "Extras de Vegetales", etc.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    @JsonIgnoreProperties({"productos", "extras", "local"})
    private Categoria categoria;

    /**
     * Relación many-to-many con Producto a través de la tabla intermedia ProductoExtra.
     * Permite saber en qué productos está disponible este extra.
     */
    @OneToMany(mappedBy = "extra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties({"producto", "extra"})
    private List<ProductoExtra> productoExtras = new ArrayList<>();

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
