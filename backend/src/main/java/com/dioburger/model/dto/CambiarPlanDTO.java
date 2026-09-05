package com.dioburger.model.dto;

import com.dioburger.model.enums.PlanSuscripcion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para cambiar el plan de suscripción de un local.
 * Usado por SuperAdmin para gestionar planes.
 * 
 * @author Dio Burger Team
 * @version 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para cambiar el plan de suscripción de un local")
public class CambiarPlanDTO {

    @NotNull(message = "El plan de suscripción es obligatorio")
    @Schema(
        description = "Nuevo plan de suscripción",
        example = "PREMIUM",
        allowableValues = {"BASICO", "ESTANDAR", "PREMIUM"}
    )
    private PlanSuscripcion planSuscripcion;

    @Schema(
        description = "Indica si el plan debe estar activo. Por defecto es true",
        example = "true",
        defaultValue = "true"
    )
    @Builder.Default
    private Boolean planActivo = true;

    @Schema(
        description = "Fecha de inicio del plan. Si es null, se usa la fecha actual",
        example = "2025-01-15"
    )
    private LocalDate fechaInicioPlan;

    @Schema(
        description = "Fecha de fin del plan (vencimiento). Si es null, el plan es indefinido",
        example = "2025-12-31"
    )
    private LocalDate fechaFinPlan;

    @Schema(
        description = "Motivo del cambio de plan (opcional, para auditoría)",
        example = "Cliente solicitó upgrade a PREMIUM por temporada alta"
    )
    private String motivoCambio;
}
