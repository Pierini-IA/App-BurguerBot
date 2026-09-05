package com.dioburger.model.entity;

import com.dioburger.model.enums.TipoProducto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un producto del menú (hamburguesa, pizza, bebida, etc.).
 * Cada producto puede tener una receta asociada que define qué ingredientes necesita.
 * Soporta sistema de promociones con precios dinámicos por horario y día.
 * Ahora incluye soporte para categorías y extras personalizables.
 * 
 * @author Dio Burger Team
 * @version 3.0.0
 */
@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Producto {

    /**
     * Identificador único del producto.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Local al que pertenece este producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id", nullable = false)
    @JsonIgnoreProperties({"configuracion", "productos", "ingredientes", "usuarios", "pedidos", "reservas", "mesas"})
    private Local local;

    /**
     * Categoría a la que pertenece este producto.
     * Ejemplos: "Hamburguesas", "Pizzas", "Bebidas", "Postres", etc.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    @JsonIgnoreProperties({"productos", "extras", "local"})
    private Categoria categoria;

    /**
     * Nombre del producto.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Precio actual del producto (mantenido por compatibilidad).
     * Se calcula automáticamente según si hay promoción activa.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /**
     * Precio base del producto sin descuento.
     */
    @Column(name = "precio_base", precision = 10, scale = 2)
    private BigDecimal precioBase;

    /**
     * Precio del producto cuando está en promoción.
     */
    @Column(name = "precio_promocion", precision = 10, scale = 2)
    private BigDecimal precioPromocion;

    /**
     * Indica si el producto tiene promoción configurada.
     */
    @Column(name = "tiene_promocion")
    @Builder.Default
    private Boolean tienePromocion = false;

    /**
     * Hora de inicio de la promoción (ej: 18:00 para Happy Hour).
     */
    @Column(name = "hora_inicio_promo")
    private LocalTime horaInicioPromo;

    /**
     * Hora de fin de la promoción (ej: 21:00).
     */
    @Column(name = "hora_fin_promo")
    private LocalTime horaFinPromo;

    /**
     * Días de la semana en los que aplica la promoción.
     * Almacenado como String JSON en DB, convertido a List<DayOfWeek>.
     */
    @Column(name = "dias_promocion", length = 100)
    private String diasPromocion;

    /**
     * Descripción del producto (opcional).
     */
    @Column(length = 500)
    private String descripcion;

    /**
     * Tipo de producto según su composición.
     * SIMPLE: No tiene receta (ej: bebidas, productos pre-empaquetados)
     * CON_RECETA: Tiene receta con ingredientes (ej: hamburguesas, pizzas)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto", length = 20)
    @Builder.Default
    private TipoProducto tipoProducto = TipoProducto.CON_RECETA;

    /**
     * Indica si este producto es un extra/adicional.
     * TRUE: Es un extra que se puede agregar a otros productos (ej: "Papas Fritas" como acompañamiento)
     * FALSE: Es un producto principal
     */
    @Column(name = "es_extra")
    @Builder.Default
    private Boolean esExtra = false;

    /**
     * Indica si este producto permite agregar extras.
     * TRUE: Se pueden agregar extras (ej: hamburguesas, pizzas)
     * FALSE: No se pueden agregar extras (ej: bebidas embotelladas)
     */
    @Column(name = "permite_extras")
    @Builder.Default
    private Boolean permiteExtras = true;

    /**
     * Indica si el producto está agotado (sin stock de ingredientes).
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean estaAgotado = false;

    /**
     * Receta del producto (lista de ingredientes y cantidades necesarias).
     * Solo aplica para productos con tipoProducto = CON_RECETA.
     */
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties({"producto"})
    private List<Receta> recetas = new ArrayList<>();

    /**
     * Extras disponibles para este producto.
     * Relación many-to-many con tabla intermedia producto_extras.
     */
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties({"producto", "extra"})
    private List<ProductoExtra> extras = new ArrayList<>();

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Agrega un extra a este producto.
     * 
     * @param extra El extra a agregar
     * @param esObligatorio Si el extra es obligatorio
     */
    public void agregarExtra(Extra extra, Boolean esObligatorio) {
        ProductoExtra productoExtra = new ProductoExtra(this, extra, esObligatorio);
        this.extras.add(productoExtra);
        extra.getProductoExtras().add(productoExtra);
    }

    /**
     * Remueve un extra de este producto.
     * 
     * @param extra El extra a remover
     */
    public void removerExtra(Extra extra) {
        this.extras.removeIf(pe -> pe.getExtra().getId().equals(extra.getId()));
        extra.getProductoExtras().removeIf(pe -> pe.getProducto().equals(this));
    }
}
