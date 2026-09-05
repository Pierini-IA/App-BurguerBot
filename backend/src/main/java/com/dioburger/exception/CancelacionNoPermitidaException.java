package com.dioburger.exception;

/**
 * Excepción lanzada cuando se intenta cancelar un pedido sin la anticipación requerida.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
public class CancelacionNoPermitidaException extends RuntimeException {
    
    public CancelacionNoPermitidaException(String message) {
        super(message);
    }
    
    public CancelacionNoPermitidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
