package com.dioburger.model.dto;

import com.dioburger.model.enums.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO de respuesta para una reserva creada o consultada.
 * Incluye toda la información relevante para el cliente y el sistema.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponseDTO {

    /**
     * ID único de la reserva.
     */
    private Long id;

    /**
     * Información del cliente.
     */
    private ClienteDTO cliente;

    /**
     * Fecha y hora de la reserva.
     */
    private LocalDateTime horaReserva;

    /**
     * Número de personas confirmadas.
     */
    private Integer numeroPersonas;

    /**
     * Mesas asignadas a esta reserva.
     */
    private Set<MesaDTO> mesas;

    /**
     * Estado actual de la reserva.
     */
    private EstadoReserva estado;

    /**
     * Gasto total registrado (cuando la reserva finaliza).
     */
    private BigDecimal gastoTotal;

    /**
     * Observaciones adicionales.
     */
    private String observaciones;

    /**
     * Fecha y hora de creación de la reserva.
     */
    private LocalDateTime fechaCreacion;
}
