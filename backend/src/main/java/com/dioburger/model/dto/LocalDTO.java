package com.dioburger.model.dto;

import com.dioburger.model.enums.PlanSuscripcion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para la creación de un nuevo local.
 * Solo accesible por SUPERADMIN.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalDTO {

    /**
     * ID del local (solo para respuestas, no para creación).
     */
    private Long id;

    /**
     * Nombre del local.
     */
    @NotBlank(message = "El nombre del local es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    /**
     * Dirección física del local.
     */
    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
    private String direccion;

    /**
     * Teléfono único del local (identificador multi-tenancy).
     * Formato internacional requerido.
     */
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "El teléfono debe estar en formato internacional (ej: +543515123456)"
    )
    private String telefono;

    // =============================================
    // CAMPOS DE PLAN DE SUSCRIPCIÓN (v2.2.0)
    // =============================================

    /**
     * Plan de suscripción del local.
     * Por defecto PREMIUM para compatibilidad.
     */
    private PlanSuscripcion planSuscripcion;

    /**
     * Indica si el plan está activo.
     */
    private Boolean planActivo;

    /**
     * Fecha de inicio del plan.
     */
    private LocalDate fechaInicioPlan;

    /**
     * Fecha de fin del plan (vencimiento). Null = indefinido.
     */
    private LocalDate fechaFinPlan;
}
