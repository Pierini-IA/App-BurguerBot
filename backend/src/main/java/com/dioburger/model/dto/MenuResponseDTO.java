package com.dioburger.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para el endpoint GET /api/bot/menu/{telefonoLocal}.
 * Contiene toda la información que el bot de n8n necesita para
 * mostrar el menú dinámico y los horarios disponibles al cliente.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponseDTO {

    /**
     * Información básica del local.
     */
    private LocalInfoDTO local;

    /**
     * Lista de productos disponibles con su estado de stock actualizado.
     */
    private List<ProductoDTO> productos;

    /**
     * Lista de horarios sugeridos para hacer el pedido (formato "HH:mm").
     * Ejemplo: ["20:00", "20:15", "20:30"]
     */
    private List<String> horariosSugeridos;

    /**
     * Modalidades de pedido permitidas por el local.
     * Ejemplo: ["DELIVERY", "RETIRAR"]
     */
    private List<String> modalidadesPermitidas;

    /**
     * Indica si el local acepta reservas de mesas.
     */
    private Boolean permiteReservas;

    /**
     * DTO interno para la información básica del local.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocalInfoDTO {
        private String nombre;
        private String direccion;
        private String telefono;
    }
}
