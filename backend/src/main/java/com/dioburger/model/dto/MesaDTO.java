package com.dioburger.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una mesa en las respuestas.
 * Contiene información básica de la mesa.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesaDTO {

    /**
     * ID único de la mesa.
     */
    private Long id;

    /**
     * Número identificador de la mesa en el local.
     */
    private Integer numero;

    /**
     * Capacidad máxima de personas.
     */
    private Integer capacidad;

    /**
     * Indica si la mesa está disponible actualmente.
     */
    private Boolean disponible;
}
