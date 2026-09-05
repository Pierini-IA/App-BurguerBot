package com.dioburger.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para crear un nuevo pedido desde el bot o panel web.
 * Contiene toda la información necesaria para procesar el pedido.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

    /**
     * ID único de la petición para garantizar idempotencia.
     * Debe ser generado por el cliente (bot o panel web).
     * Formato sugerido: UUID
     */
    @NotBlank(message = "El requestId es obligatorio para garantizar idempotencia")
    private String requestId;

    /**
     * Datos del cliente que realiza el pedido.
     */
    @NotNull(message = "Los datos del cliente son obligatorios")
    @Valid
    private ClienteDTO cliente;

    /**
     * Modalidad del pedido: DELIVERY o RETIRAR.
     */
    @NotBlank(message = "La modalidad es obligatoria")
    private String modalidad;

    /**
     * Dirección de envío (obligatoria solo si modalidad = DELIVERY).
     */
    private String direccionEnvio;

    /**
     * Medio de pago: EFECTIVO, TRANSFERENCIA, TARJETA_DEBITO, TARJETA_CREDITO, QR.
     */
    @NotBlank(message = "El medio de pago es obligatorio")
    private String medioPago;

    /**
     * Lista de items del pedido (productos + cantidades).
     */
    @NotEmpty(message = "El pedido debe tener al menos un item")
    @Valid
    private List<PedidoItemDTO> items;

    /**
     * Hora deseada para el pedido (formato: "HH:mm").
     * Ejemplo: "20:15"
     */
    private String horaPedido;
}
