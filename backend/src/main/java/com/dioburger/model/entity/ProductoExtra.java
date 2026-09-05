package com.dioburger.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * Entidad que representa la relación muchos-a-muchos entre Producto y Extra.
 * Incluye el campo adicional 'esObligatorio' para indicar si un extra es requerido.
 * 
 * Ejemplo: Una pizza puede tener "Queso Mozzarella" como extra obligatorio,
 * pero "Aceitunas" como extra opcional.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "producto_extras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductoExtra implements Serializable {

    /**
     * Clave primaria compuesta para la relación.
     */
    @EmbeddedId
    private ProductoExtraId id;

    /**
     * Referencia al producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productoId")
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnoreProperties({"extras", "receta", "local", "categoria"})
    private Producto producto;

    /**
     * Referencia al extra.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("extraId")
    @JoinColumn(name = "extra_id", nullable = false)
    @JsonIgnoreProperties({"productos", "local", "categoria"})
    private Extra extra;

    /**
     * Indica si este extra es obligatorio para el producto.
     * 
     * Si es TRUE: El cliente DEBE seleccionar este extra (puede ser sin cargo adicional).
     * Si es FALSE: El cliente PUEDE seleccionar este extra (opcional).
     */
    @Column(name = "es_obligatorio", nullable = false)
    @Builder.Default
    private Boolean esObligatorio = false;

    /**
     * Clave primaria compuesta para ProductoExtra.
     * Combina productoId y extraId.
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ProductoExtraId implements Serializable {
        
        @Column(name = "producto_id")
        private Long productoId;
        
        @Column(name = "extra_id")
        private Long extraId;
    }

    /**
     * Constructor de conveniencia para crear una relación Producto-Extra.
     * 
     * @param producto El producto
     * @param extra El extra
     * @param esObligatorio Si el extra es obligatorio
     */
    public ProductoExtra(Producto producto, Extra extra, Boolean esObligatorio) {
        this.producto = producto;
        this.extra = extra;
        this.esObligatorio = esObligatorio;
        this.id = new ProductoExtraId(producto.getId(), extra.getId());
    }
}
