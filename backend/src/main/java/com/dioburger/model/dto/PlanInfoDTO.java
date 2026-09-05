package com.dioburger.model.dto;

import com.dioburger.model.enums.Feature;
import com.dioburger.model.enums.PlanSuscripcion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO con información completa del plan de suscripción de un local.
 * Incluye el plan, estado, fechas y features disponibles.
 * 
 * @author Dio Burger Team
 * @version 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información completa del plan de suscripción de un local")
public class PlanInfoDTO {

    @Schema(description = "ID del local", example = "1")
    private Long localId;

    @Schema(description = "Nombre del local", example = "Dio Burger Centro")
    private String nombreLocal;

    @Schema(
        description = "Plan de suscripción actual",
        example = "PREMIUM",
        allowableValues = {"BASICO", "ESTANDAR", "PREMIUM"}
    )
    private PlanSuscripcion planSuscripcion;

    @Schema(description = "Nombre del plan en formato legible", example = "Plan Premium")
    private String nombrePlan;

    @Schema(description = "Descripción corta del plan", example = "Franquicias o locales con IA + impresión automática")
    private String descripcionPlan;

    @Schema(description = "Precio de setup inicial del plan en ARS", example = "250000")
    private Double precioSetupInicial;

    @Schema(description = "Precio mensual del plan en ARS", example = "125000")
    private Double precioMensual;

    @Schema(description = "Indica si el plan está activo", example = "true")
    private Boolean planActivo;

    @Schema(description = "Fecha de inicio del plan", example = "2025-01-01")
    private LocalDate fechaInicioPlan;

    @Schema(description = "Fecha de fin del plan (vencimiento). Null = indefinido", example = "2025-12-31")
    private LocalDate fechaFinPlan;

    @Schema(description = "Días restantes hasta el vencimiento. Null = indefinido", example = "245")
    private Long diasRestantes;

    @Schema(description = "Indica si el plan está próximo a vencer (menos de 30 días)", example = "false")
    private Boolean proximoAVencer;

    @Schema(description = "Indica si el plan ya venció", example = "false")
    private Boolean vencido;

    @Schema(description = "Lista de features disponibles en el plan actual")
    private Set<Feature> featuresDisponibles;

    @Schema(description = "Cantidad de features disponibles", example = "21")
    private Integer totalFeatures;

    /**
     * Factory method para crear desde un Local.
     */
    public static PlanInfoDTO fromLocal(com.dioburger.model.entity.Local local, Set<Feature> features) {
        LocalDate now = LocalDate.now();
        Long diasRestantes = null;
        Boolean proximoAVencer = false;
        Boolean vencido = false;

        if (local.getFechaFinPlan() != null) {
            diasRestantes = (long) now.until(local.getFechaFinPlan()).getDays();
            proximoAVencer = diasRestantes <= 30 && diasRestantes > 0;
            vencido = diasRestantes < 0;
        }

        return PlanInfoDTO.builder()
            .localId(local.getId())
            .nombreLocal(local.getNombre())
            .planSuscripcion(local.getPlanSuscripcion())
            .nombrePlan(local.getPlanSuscripcion().getNombre())
            .descripcionPlan(local.getPlanSuscripcion().getDescripcionCorta())
            .precioSetupInicial(local.getPlanSuscripcion().getPrecioSetupInicial())
            .precioMensual(local.getPlanSuscripcion().getPrecioMensual())
            .planActivo(local.getPlanActivo())
            .fechaInicioPlan(local.getFechaInicioPlan())
            .fechaFinPlan(local.getFechaFinPlan())
            .diasRestantes(diasRestantes)
            .proximoAVencer(proximoAVencer)
            .vencido(vencido)
            .featuresDisponibles(features)
            .totalFeatures(features.size())
            .build();
    }
}
