package com.dioburger.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta al crear un pedido exitosamente.
 * Devuelve información básica del pedido creado.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    /**
     * ID único del pedido en la base de datos.
     */
    private Long id;

    /**
     * Request ID usado para garantizar idempotencia.
     */
    private String requestId;

    /**
     * Estado actual del pedido.
     */
    private String estado;

    /**
     * Total a pagar en pesos argentinos.
     */
    private BigDecimal total;

    /**
     * Hora en que se registró el pedido.
     */
    private LocalDateTime horaPedido;

    /**
     * Modalidad del pedido (DELIVERY o RETIRAR).
     */
    private String modalidad;

    /**
     * Medio de pago seleccionado.
     */
    private String medioPago;

    /**
     * Estado del pago (PENDIENTE, PAGADO, RECHAZADO).
     */
    private String estadoPago;
}
