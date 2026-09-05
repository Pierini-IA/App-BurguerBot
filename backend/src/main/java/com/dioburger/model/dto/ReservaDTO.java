package com.dioburger.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO para la creación de una reserva desde el bot.
 * Incluye validaciones y campo de idempotencia.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {

    /**
     * Información del cliente que realiza la reserva.
     */
    @NotNull(message = "Los datos del cliente son obligatorios")
    @Valid
    private ClienteDTO cliente;

    /**
     * Fecha y hora de la reserva.
     */
    @NotNull(message = "La hora de reserva es obligatoria")
    private LocalDateTime horaReserva;

    /**
     * Número de personas para la reserva.
     */
    @NotNull(message = "El número de personas es obligatorio")
    @Positive(message = "El número de personas debe ser mayor a 0")
    private Integer numeroPersonas;

    /**
     * Observaciones adicionales (alergias, ocasión especial, etc.).
     */
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    /**
     * ID único para garantizar idempotencia en caso de reintentos.
     * El bot debe generar un UUID único por cada intento de reserva.
     */
    @NotNull(message = "El requestId es obligatorio para evitar duplicados")
    @Size(min = 10, max = 255, message = "El requestId debe tener entre 10 y 255 caracteres")
    private String requestId;
}
