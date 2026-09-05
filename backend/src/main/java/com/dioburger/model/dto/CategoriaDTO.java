package com.dioburger.model.dto;

import lombok.*;

/**
 * DTO para la entidad Categoria.
 * Utilizado para transferir información de categorías sin exponer la entidad completa.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaDTO {

    /**
     * ID de la categoría.
     */
    private Long id;

    /**
     * Nombre de la categoría.
     */
    private String nombre;

    /**
     * Descripción de la categoría.
     */
    private String descripcion;

    /**
     * Orden de visualización.
     */
    private Integer orden;

    /**
     * Estado activo/inactivo.
     */
    private Boolean activo;

    /**
     * ID del local al que pertenece.
     */
    private Long localId;

    /**
     * Nombre del local (opcional, para mostrar en listados).
     */
    private String localNombre;

    /**
     * Cantidad de productos en esta categoría (opcional).
     */
    private Integer cantidadProductos;

    /**
     * Cantidad de extras en esta categoría (opcional).
     */
    private Integer cantidadExtras;
}
