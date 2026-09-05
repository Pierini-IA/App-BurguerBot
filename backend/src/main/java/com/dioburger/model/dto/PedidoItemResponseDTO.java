package com.dioburger.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para representar un item de pedido en las respuestas de la API.
 * Incluye información completa del producto y extras seleccionados.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoItemResponseDTO {

    /**
     * ID del item.
     */
    private Long id;

    /**
     * ID del producto.
     */
    private Long productoId;

    /**
     * Nombre del producto.
     */
    private String productoNombre;

    /**
     * Cantidad del producto.
     */
    private Integer cantidad;

    /**
     * Precio unitario del producto base (sin extras).
     */
    private BigDecimal precioUnitarioBase;

    /**
     * Precio unitario incluyendo extras.
     */
    private BigDecimal precioUnitario;

    /**
     * Subtotal del item (precioUnitario * cantidad).
     */
    private BigDecimal subtotal;

    /**
     * Observaciones del item.
     */
    private String observaciones;

    /**
     * Lista de extras seleccionados para este item.
     */
    @Builder.Default
    private List<ExtraSeleccionadoDTO> extrasSeleccionados = new ArrayList<>();

    /**
     * DTO anidado para representar un extra seleccionado.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExtraSeleccionadoDTO {
        private Long extraId;
        private String extraNombre;
        private BigDecimal precioAdicional;
    }
}
