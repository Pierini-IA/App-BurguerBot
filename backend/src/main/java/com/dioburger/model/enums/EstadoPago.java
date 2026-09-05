package com.dioburger.model.enums;

/**
 * Enum que representa el estado del pago de un pedido.
 * Define si el pago ha sido procesado exitosamente o no.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
public enum EstadoPago {
    /**
     * Pago pendiente de procesamiento.
     */
    PENDIENTE,
    
    /**
     * Pago procesado exitosamente.
     */
    PAGADO,
    
    /**
     * Pago rechazado.
     */
    RECHAZADO
}
