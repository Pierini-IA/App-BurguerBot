package com.dioburger.exception;

import com.dioburger.model.enums.Feature;
import com.dioburger.model.enums.PlanSuscripcion;

/**
 * Excepción lanzada cuando un local intenta acceder a una funcionalidad
 * que no está incluida en su plan de suscripción.
 * 
 * @author Dio Burger Team
 * @version 2.2.0
 */
public class FeatureNotAvailableException extends RuntimeException {
    
    private final Feature feature;
    private final PlanSuscripcion planActual;
    private final PlanSuscripcion planRequerido;
    
    /**
     * Constructor con feature y planes.
     * 
     * @param feature feature no disponible
     * @param planActual plan actual del local
     * @param planRequerido plan mínimo requerido
     */
    public FeatureNotAvailableException(Feature feature, PlanSuscripcion planActual, PlanSuscripcion planRequerido) {
        super(String.format(
            "La funcionalidad '%s' no está disponible en tu plan %s. " +
            "Actualiza al plan %s para acceder a esta característica.",
            feature.getDescripcion(),
            planActual.getNombre(),
            planRequerido.getNombre()
        ));
        this.feature = feature;
        this.planActual = planActual;
        this.planRequerido = planRequerido;
    }
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message mensaje personalizado
     * @param feature feature no disponible
     * @param planActual plan actual
     * @param planRequerido plan requerido
     */
    public FeatureNotAvailableException(String message, Feature feature, PlanSuscripcion planActual, PlanSuscripcion planRequerido) {
        super(message);
        this.feature = feature;
        this.planActual = planActual;
        this.planRequerido = planRequerido;
    }
    
    public Feature getFeature() {
        return feature;
    }
    
    public PlanSuscripcion getPlanActual() {
        return planActual;
    }
    
    public PlanSuscripcion getPlanRequerido() {
        return planRequerido;
    }
}
