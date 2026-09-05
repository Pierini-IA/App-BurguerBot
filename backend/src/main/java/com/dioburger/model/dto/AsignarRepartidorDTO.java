package com.dioburger.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para asignar un repartidor a un pedido DELIVERY.
 * Utilizado cuando n8n confirma la asignación de un repartidor.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarRepartidorDTO {

    /**
     * ID del repartidor en el sistema de n8n.
     */
    @NotBlank(message = "El ID del repartidor es obligatorio")
    private String repartidorId;

    /**
     * Nombre completo del repartidor.
     */
    @NotBlank(message = "El nombre del repartidor es obligatorio")
    private String repartidorNombre;

    /**
     * Teléfono del repartidor para contacto.
     */
    @NotBlank(message = "El teléfono del repartidor es obligatorio")
    private String repartidorTelefono;

    /**
     * URL para trackear el pedido en tiempo real (opcional).
     */
    private String urlTracking;
}
