package com.dioburger.exception;

/**
 * Excepción lanzada cuando el horario solicitado no está disponible.
 * Puede ser porque el slot está lleno o fuera del horario de atención.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
public class HorarioNoDisponibleException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje Descripción del error
     */
    public HorarioNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param mensaje Descripción del error
     * @param causa Excepción que causó este error
     */
    public HorarioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
