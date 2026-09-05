package com.dioburger.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * Entidad que representa la relación entre PedidoItem y Extra.
 * Registra qué extras seleccionó el cliente para cada item del pedido.
 * 
 * Ejemplo: Si el cliente pidió una hamburguesa con "Queso Extra" y "Bacon",
 * se crearán dos registros PedidoItemExtra para ese item.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "pedido_item_extras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PedidoItemExtra implements Serializable {

    /**
     * Clave primaria compuesta para la relación.
     */
    @EmbeddedId
    private PedidoItemExtraId id;

    /**
     * Referencia al item del pedido.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pedidoItemId")
    @JoinColumn(name = "pedido_item_id", nullable = false)
    @JsonIgnoreProperties({"pedido", "producto", "extrasSeleccionados"})
    private PedidoItem pedidoItem;

    /**
     * Referencia al extra seleccionado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("extraId")
    @JoinColumn(name = "extra_id", nullable = false)
    @JsonIgnoreProperties({"productos", "local", "categoria"})
    private Extra extra;

    /**
     * Clave primaria compuesta para PedidoItemExtra.
     * Combina pedidoItemId y extraId.
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PedidoItemExtraId implements Serializable {
        
        @Column(name = "pedido_item_id")
        private Long pedidoItemId;
        
        @Column(name = "extra_id")
        private Long extraId;
    }

    /**
     * Constructor de conveniencia para crear una relación PedidoItem-Extra.
     * 
     * @param pedidoItem El item del pedido
     * @param extra El extra seleccionado
     */
    public PedidoItemExtra(PedidoItem pedidoItem, Extra extra) {
        this.pedidoItem = pedidoItem;
        this.extra = extra;
        this.id = new PedidoItemExtraId(pedidoItem.getId(), extra.getId());
    }
}
