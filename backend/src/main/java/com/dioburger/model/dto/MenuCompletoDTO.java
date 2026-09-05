package com.dioburger.model.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para el menú completo agrupado por categorías.
 * Utilizado por el endpoint público /api/menu/{telefonoLocal}
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCompletoDTO {

    /**
     * Información básica del local.
     */
    private LocalInfoDTO local;

    /**
     * Lista de categorías con sus productos.
     */
    @Builder.Default
    private List<CategoriaConProductosDTO> categorias = new ArrayList<>();

    /**
     * Horarios sugeridos para pedidos (formato: "HH:mm").
     */
    @Builder.Default
    private List<String> horariosSugeridos = new ArrayList<>();

    /**
     * Modalidades permitidas ("DELIVERY", "RETIRAR").
     */
    @Builder.Default
    private List<String> modalidadesPermitidas = new ArrayList<>();

    /**
     * Indica si el local acepta reservas.
     */
    private Boolean permiteReservas;

    /**
     * Configuración de horarios del local.
     */
    private ConfiguracionHorariosDTO configuracion;

    /**
     * DTO anidado para información del local.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocalInfoDTO {
        private String nombre;
        private String direccion;
        private String telefono;
    }

    /**
     * DTO anidado para categoría con sus productos.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoriaConProductosDTO {
        private Long id;
        private String nombre;
        private String descripcion;
        private Integer orden;
        
        @Builder.Default
        private List<ProductoDTO> productos = new ArrayList<>();
    }

    /**
     * DTO anidado para configuración de horarios.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfiguracionHorariosDTO {
        private String horaApertura;
        private String horaCierre;
    }
}
