package com.dioburger.model.enums;

/**
 * Enum que representa los estados posibles de una reserva.
 * Define el ciclo de vida de una reserva desde su creación hasta su finalización.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
public enum EstadoReserva {
    /**
     * Reserva confirmada, esperando la llegada del cliente.
     */
    CONFIRMADA,
    
    /**
     * Cliente ha llegado y está ocupando la mesa.
     */
    OCUPADA,
    
    /**
     * Reserva finalizada, cliente se ha retirado.
     */
    FINALIZADA,
    
    /**
     * Reserva cancelada.
     */
    CANCELADA
}
