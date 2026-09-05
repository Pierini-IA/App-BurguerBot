package com.dioburger.model.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para la entidad Extra.
 * Utilizado para transferir información de extras sin exponer la entidad completa.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtraDTO {

    /**
     * ID del extra.
     */
    private Long id;

    /**
     * Nombre del extra.
     */
    private String nombre;

    /**
     * Descripción del extra.
     */
    private String descripcion;

    /**
     * Precio adicional del extra.
     */
    private BigDecimal precioAdicional;

    /**
     * Estado activo/inactivo.
     */
    private Boolean activo;

    /**
     * ID del local al que pertenece.
     */
    private Long localId;

    /**
     * Nombre del local (opcional).
     */
    private String localNombre;

    /**
     * ID de la categoría (opcional).
     */
    private Long categoriaId;

    /**
     * Nombre de la categoría (opcional).
     */
    private String categoriaNombre;

    /**
     * Indica si el extra es obligatorio para un producto específico.
     * Este campo solo se usa cuando el extra está asociado a un producto.
     */
    private Boolean esObligatorio;
}
