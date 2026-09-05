package com.dioburger.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cancelar un pedido desde el bot de n8n.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelarPedidoDTO {

    /**
     * Request ID para idempotencia de la cancelación.
     * Debe ser único para cada intento de cancelación.
     */
    @NotBlank(message = "El requestId es obligatorio para idempotencia")
    private String requestId;

    /**
     * Motivo de la cancelación (opcional pero recomendado).
     */
    private String motivo;
}
