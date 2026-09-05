package com.dioburger.exception;

/**
 * Excepción lanzada cuando no hay stock suficiente de ingredientes
 * para completar un pedido.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
public class StockInsuficienteException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje Descripción del error
     */
    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param mensaje Descripción del error
     * @param causa Excepción que causó este error
     */
    public StockInsuficienteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
