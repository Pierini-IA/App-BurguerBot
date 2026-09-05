package com.dioburger.model.enums;

/**
 * Enum que representa los estados posibles de un pedido.
 * Define el ciclo de vida de un pedido desde su creación hasta su finalización.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
public enum EstadoPedido {
    /**
     * Pedido creado, esperando confirmación.
     */
    PENDIENTE,
    
    /**
     * Pedido confirmado por el sistema.
     */
    CONFIRMADO,
    
    /**
     * Pedido en proceso de preparación en cocina.
     */
    EN_PREPARACION,
    
    /**
     * Pedido listo para entrega o retiro.
     */
    LISTO,
    
    /**
     * Pedido en camino al cliente (solo DELIVERY).
     */
    EN_CAMINO,
    
    /**
     * Pedido entregado al cliente.
     */
    ENTREGADO,
    
    /**
     * Pedido cancelado.
     */
    CANCELADO
}
