package com.dioburger.model.dto;

import com.dioburger.model.enums.TipoProducto;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para representar un producto en el menú.
 * Incluye información de categoría, extras y promociones.
 *
 * @author Dio Burger Team
 * @version 2.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    /**
     * ID único del producto.
     */
    private Long id;

    /**
     * Nombre del producto (ej: "Clásica", "Bacon Cheese").
     */
    private String nombre;

    /**
     * Descripción del producto con los ingredientes.
     */
    private String descripcion;

    /**
     * Precio actual del producto en pesos argentinos.
     */
    private BigDecimal precio;

    /**
     * Precio base sin descuento.
     */
    private BigDecimal precioBase;

    /**
     * Precio en promoción (si aplica).
     */
    private BigDecimal precioPromocion;

    /**
     * Indica si tiene promoción activa.
     */
    private Boolean tienePromocion;

    /**
     * Indica si el producto está disponible (true) o agotado (false).
     * Se calcula dinámicamente en base al stock de ingredientes.
     */
    private Boolean disponible;

    /**
     * Estado de stock (agotado o disponible).
     */
    private Boolean estaAgotado;

    /**
     * Tipo de producto (SIMPLE o CON_RECETA).
     */
    private TipoProducto tipoProducto;

    /**
     * Indica si es un extra/adicional.
     */
    private Boolean esExtra;

    /**
     * Indica si permite agregar extras.
     */
    private Boolean permiteExtras;

    /**
     * ID del local al que pertenece.
     */
    private Long localId;

    /**
     * Nombre del local (opcional).
     */
    private String localNombre;

    /**
     * ID de la categoría.
     */
    private Long categoriaId;

    /**
     * Nombre de la categoría.
     */
    private String categoriaNombre;

    /**
     * Lista de extras disponibles para este producto.
     * Solo se incluye si permiteExtras = true.
     */
    @Builder.Default
    private List<ExtraDTO> extrasDisponibles = new ArrayList<>();

    /**
     * Información de promoción (horario y días).
     * Solo se incluye si tienePromocion = true.
     */
    private PromocionInfoDTO promocion;

    /**
     * DTO anidado para información de promoción.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PromocionInfoDTO {
        private String horaInicio;
        private String horaFin;
        private String diasPromocion;
    }
}
