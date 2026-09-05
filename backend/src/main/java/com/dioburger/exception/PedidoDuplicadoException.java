package com.dioburger.exception;

/**
 * Excepción lanzada cuando se intenta crear un pedido duplicado.
 * Se usa para garantizar idempotencia verificando el requestId.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
public class PedidoDuplicadoException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje Descripción del error
     */
    public PedidoDuplicadoException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param mensaje Descripción del error
     * @param causa Excepción que causó este error
     */
    public PedidoDuplicadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
