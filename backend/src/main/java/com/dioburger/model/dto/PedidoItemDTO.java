package com.dioburger.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para representar un item individual de un pedido (Request).
 * Contiene el producto, cantidad, observaciones y extras seleccionados.
 *
 * @author Dio Burger Team
 * @version 2.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoItemDTO {

    /**
     * ID del producto a pedir.
     */
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    /**
     * Cantidad de unidades del producto.
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    /**
     * Observaciones opcionales para el item.
     * Ejemplo: "sin lechuga", "bien cocida", "sin cebolla"
     */
    private String observaciones;

    /**
     * Lista de IDs de extras seleccionados para este item.
     * Ejemplo: [1, 3, 5] para agregar "Queso Extra", "Bacon" y "Huevo"
     */
    @Builder.Default
    private List<Long> extrasIds = new ArrayList<>();
}
