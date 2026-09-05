package com.dioburger.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
public class NotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje Descripción del error
     */
    public NotFoundException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param mensaje Descripción del error
     * @param causa Excepción que causó este error
     */
    public NotFoundException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
