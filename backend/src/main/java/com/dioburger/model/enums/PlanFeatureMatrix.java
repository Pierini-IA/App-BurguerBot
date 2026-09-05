package com.dioburger.model.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Matriz que define qué funcionalidades (Features) están disponibles en cada Plan.
 * Esta clase centraliza toda la lógica de permisos por plan.
 * 
 * Principios aplicados:
 * - Single Responsibility: Solo maneja la relación Plan-Features
 * - Open/Closed: Extensible sin modificar código existente
 * - DRY: Lógica centralizada en un solo lugar
 * 
 * @author Dio Burger Team
 * @version 2.2.0
 */
public class PlanFeatureMatrix {
    
    /**
     * Mapa inmutable que asocia cada plan con sus features disponibles.
     */
    private static final Map<PlanSuscripcion, Set<Feature>> PLAN_FEATURES_MAP = Map.of(
        
        // ==================== PLAN BÁSICO ====================
        PlanSuscripcion.BASICO, EnumSet.of(
            // Gestión básica del local
            Feature.PANEL_WEB,
            Feature.GESTION_PRODUCTOS,
            Feature.GESTION_INGREDIENTES,
            Feature.GESTION_CATEGORIAS,
            Feature.GESTION_EXTRAS,
            Feature.GESTION_MESAS,
            Feature.PEDIDOS_PANEL,
            Feature.PANEL_COCINA,
            Feature.ESTADISTICAS_BASICAS,
            Feature.WEBSOCKET_TIEMPO_REAL  // WebSocket incluido en básico
        ),
        
        // ==================== PLAN ESTÁNDAR ====================
        PlanSuscripcion.ESTANDAR, EnumSet.of(
            // Todo lo de BÁSICO +
            Feature.PANEL_WEB,
            Feature.GESTION_PRODUCTOS,
            Feature.GESTION_INGREDIENTES,
            Feature.GESTION_CATEGORIAS,
            Feature.GESTION_EXTRAS,
            Feature.GESTION_MESAS,
            Feature.PEDIDOS_PANEL,
            Feature.PANEL_COCINA,
            Feature.ESTADISTICAS_BASICAS,
            Feature.WEBSOCKET_TIEMPO_REAL,
            
            // Features exclusivas de ESTÁNDAR
            Feature.BOT_WHATSAPP,
            Feature.SISTEMA_RESERVAS,
            Feature.BOT_COMMENTS_META
        ),
        
        // ==================== PLAN PREMIUM ====================
        PlanSuscripcion.PREMIUM, EnumSet.allOf(Feature.class)  // Todas las features
    );
    
    /**
     * Obtiene el conjunto de features disponibles para un plan específico.
     * 
     * @param plan plan de suscripción
     * @return conjunto inmutable de features disponibles
     */
    public static Set<Feature> getFeaturesForPlan(PlanSuscripcion plan) {
        return EnumSet.copyOf(PLAN_FEATURES_MAP.get(plan));
    }
    
    /**
     * Verifica si un plan específico tiene acceso a una feature.
     * 
     * @param plan plan de suscripción
     * @param feature funcionalidad a verificar
     * @return true si el plan incluye la feature
     */
    public static boolean hasFeature(PlanSuscripcion plan, Feature feature) {
        return PLAN_FEATURES_MAP.get(plan).contains(feature);
    }
    
    /**
     * Obtiene el plan mínimo requerido para acceder a una feature.
     * 
     * @param feature funcionalidad deseada
     * @return plan mínimo que incluye esa feature
     */
    public static PlanSuscripcion getMinimumPlanForFeature(Feature feature) {
        for (PlanSuscripcion plan : PlanSuscripcion.values()) {
            if (hasFeature(plan, feature)) {
                return plan;
            }
        }
        throw new IllegalStateException("Feature no asignada a ningún plan: " + feature);
    }
    
    /**
     * Retorna las features exclusivas de un plan (que no están en el plan anterior).
     * 
     * @param plan plan de suscripción
     * @return conjunto de features exclusivas del plan
     */
    public static Set<Feature> getExclusiveFeatures(PlanSuscripcion plan) {
        Set<Feature> currentFeatures = getFeaturesForPlan(plan);
        
        if (plan == PlanSuscripcion.BASICO) {
            return currentFeatures;
        }
        
        // Obtener el plan anterior
        PlanSuscripcion planAnterior = PlanSuscripcion.values()[plan.ordinal() - 1];
        Set<Feature> featuresAnterior = getFeaturesForPlan(planAnterior);
        
        // Calcular diferencia
        Set<Feature> exclusivas = EnumSet.copyOf(currentFeatures);
        exclusivas.removeAll(featuresAnterior);
        
        return exclusivas;
    }
    
    /**
     * Retorna un mensaje descriptivo de las features incluidas en un plan.
     * 
     * @param plan plan de suscripción
     * @return descripción de features
     */
    public static String getFeaturesSummary(PlanSuscripcion plan) {
        Set<Feature> features = getFeaturesForPlan(plan);
        
        StringBuilder sb = new StringBuilder();
        sb.append("Plan ").append(plan.getNombre()).append(" incluye:\n");
        
        for (Feature feature : features) {
            sb.append("  • ").append(feature.getDescripcion()).append("\n");
        }
        
        return sb.toString();
    }
}
