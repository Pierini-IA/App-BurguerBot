package com.dioburger.model.enums;

/**
 * Enum que representa los medios de pago aceptados.
 * Define las formas en que un cliente puede pagar su pedido.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
public enum MedioPago {
    /**
     * Pago en efectivo.
     */
    EFECTIVO,
    
    /**
     * Pago por transferencia bancaria.
     */
    TRANSFERENCIA,
    
    /**
     * Pago con tarjeta de débito.
     */
    TARJETA_DEBITO,
    
    /**
     * Pago con tarjeta de crédito.
     */
    TARJETA_CREDITO,
    
    /**
     * Pago mediante código QR.
     */
    QR
}
