package com.dioburger.model.enums;

/**
 * Enum que representa el origen de un pedido.
 * Identifica desde qué canal se realizó el pedido.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
public enum OrigenPedido {
    /**
     * Pedido realizado desde el bot de n8n/WhatsApp.
     */
    BOT,
    
    /**
     * Pedido realizado desde el panel web del local.
     */
    LOCAL
}
