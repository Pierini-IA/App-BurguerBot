package com.dioburger.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un item individual dentro de un pedido.
 * Relaciona un producto con su cantidad, extras seleccionados y observaciones.
 * 
 * @author Dio Burger Team
 * @version 2.0.0
 */
@Entity
@Table(name = "pedido_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PedidoItem {

    /**
     * Identificador único del item.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Pedido al que pertenece este item.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnoreProperties({"items", "cliente", "local"})
    private Pedido pedido;

    /**
     * Producto solicitado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnoreProperties({"recetas", "extras", "local", "categoria"})
    private Producto producto;

    /**
     * Cantidad del producto solicitado.
     */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * Observaciones específicas del item (ej: "sin lechuga", "punto de cocción medio").
     */
    @Column(length = 500)
    private String observaciones;

    /**
     * Extras seleccionados para este item del pedido.
     * Ejemplo: Si pidió hamburguesa con "Queso Extra" y "Bacon".
     */
    @OneToMany(mappedBy = "pedidoItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties({"pedidoItem"})
    private List<PedidoItemExtra> extrasSeleccionados = new ArrayList<>();

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Agrega un extra a este item del pedido.
     * 
     * @param extra El extra a agregar
     */
    public void agregarExtra(Extra extra) {
        PedidoItemExtra pedidoItemExtra = new PedidoItemExtra(this, extra);
        this.extrasSeleccionados.add(pedidoItemExtra);
    }

    /**
     * Remueve un extra de este item del pedido.
     * 
     * @param extra El extra a remover
     */
    public void removerExtra(Extra extra) {
        this.extrasSeleccionados.removeIf(pie -> pie.getExtra().getId().equals(extra.getId()));
    }

    /**
     * Calcula el subtotal de este item incluyendo extras.
     * Formula: (precio_producto + suma_extras) * cantidad
     * 
     * @return El subtotal del item con extras
     */
    public BigDecimal calcularSubtotal() {
        BigDecimal precioProducto = producto.getPrecio();
        
        // Sumar el precio de todos los extras seleccionados
        BigDecimal totalExtras = extrasSeleccionados.stream()
            .map(pe -> pe.getExtra().getPrecioAdicional())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // (precio_producto + extras) * cantidad
        return precioProducto.add(totalExtras).multiply(BigDecimal.valueOf(cantidad));
    }

    /**
     * Obtiene el precio unitario del item (producto + extras).
     * 
     * @return El precio unitario incluyendo extras
     */
    public BigDecimal getPrecioUnitario() {
        BigDecimal precioProducto = producto.getPrecio();
        
        BigDecimal totalExtras = extrasSeleccionados.stream()
            .map(pe -> pe.getExtra().getPrecioAdicional())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return precioProducto.add(totalExtras);
    }
}
