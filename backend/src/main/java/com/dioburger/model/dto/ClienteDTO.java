package com.dioburger.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar los datos de un cliente en un pedido o reserva.
 * Usado para crear/actualizar clientes desde el bot o panel web.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    /**
     * Nombre completo del cliente.
     */
    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombre;

    /**
     * Teléfono del cliente (único).
     * Formato esperado: +5491123456789
     */
    @NotBlank(message = "El teléfono del cliente es obligatorio")
    private String telefono;
}
