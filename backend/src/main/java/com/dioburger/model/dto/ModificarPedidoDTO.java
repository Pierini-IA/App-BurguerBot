package com.dioburger.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para modificar un pedido existente desde el bot de n8n.
 * Permite cambiar items, horario, dirección, etc.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModificarPedidoDTO {

    /**
     * Request ID para idempotencia de la modificación.
     * Debe ser único para cada intento de modificación.
     */
    @NotBlank(message = "El requestId es obligatorio para idempotencia")
    private String requestId;

    /**
     * Nuevos items del pedido.
     * Reemplaza completamente los items anteriores.
     */
    @Valid
    @NotEmpty(message = "Debe haber al menos un item en el pedido")
    private List<PedidoItemDTO> items;

    /**
     * Nueva hora de entrega (opcional).
     * Formato: "HH:mm" (ej: "20:30")
     */
    private String horaPedido;

    /**
     * Nueva dirección de envío (solo para DELIVERY).
     */
    private String direccionEnvio;

    /**
     * Nuevas observaciones del pedido.
     */
    private String observaciones;
}
