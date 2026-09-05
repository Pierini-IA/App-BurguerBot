package com.dioburger.exception;

/**
 * Excepción lanzada cuando se intenta crear una reserva con un requestId duplicado.
 * Garantiza la idempotencia del sistema de reservas.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
public class ReservaDuplicadaException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje descriptivo del error
     */
    public ReservaDuplicadaException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa.
     * 
     * @param message Mensaje descriptivo del error
     * @param cause Causa raíz de la excepción
     */
    public ReservaDuplicadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
