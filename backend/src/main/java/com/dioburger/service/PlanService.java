package com.dioburger.service;

import com.dioburger.model.entity.Local;
import com.dioburger.model.enums.Feature;
import com.dioburger.model.enums.PlanSuscripcion;

import java.util.Set;

/**
 * Servicio para gestionar la lógica de planes de suscripción y validación de features.
 * 
 * Principios SOLID aplicados:
 * - Interface Segregation: Interface específica para gestión de planes
 * - Dependency Inversion: Los controllers dependen de esta abstracción
 * 
 * @author Dio Burger Team
 * @version 2.2.0
 */
public interface PlanService {
    
    /**
     * Valida si un local tiene acceso a una feature específica.
     * Lanza excepción si no tiene acceso.
     * 
     * @param local local a validar
     * @param feature feature requerida
     * @throws com.dioburger.exception.FeatureNotAvailableException si no tiene acceso
     */
    void validarAccesoFeature(Local local, Feature feature);
    
    /**
     * Valida si un local tiene acceso a una feature con mensaje personalizado.
     * 
     * @param local local a validar
     * @param feature feature requerida
     * @param customMessage mensaje personalizado de error
     * @throws com.dioburger.exception.FeatureNotAvailableException si no tiene acceso
     */
    void validarAccesoFeature(Local local, Feature feature, String customMessage);
    
    /**
     * Verifica si un local tiene acceso a una feature (sin lanzar excepción).
     * 
     * @param local local a verificar
     * @param feature feature a verificar
     * @return true si tiene acceso, false en caso contrario
     */
    boolean tieneAccesoFeature(Local local, Feature feature);
    
    /**
     * Obtiene todas las features disponibles para el plan de un local.
     * 
     * @param local local
     * @return conjunto de features disponibles
     */
    Set<Feature> getFeaturesDisponibles(Local local);
    
    /**
     * Obtiene el plan mínimo requerido para acceder a una feature.
     * 
     * @param feature feature deseada
     * @return plan mínimo requerido
     */
    PlanSuscripcion getPlanMinimoParaFeature(Feature feature);
    
    /**
     * Verifica si el plan de un local está activo y vigente.
     * 
     * @param local local a verificar
     * @return true si el plan está activo
     */
    boolean isPlanActivo(Local local);
    
    /**
     * Valida si el plan de un local está activo.
     * Lanza excepción si el plan está inactivo o vencido.
     * 
     * @param local local a validar
     * @throws IllegalStateException si el plan está inactivo
     */
    void validarPlanActivo(Local local);
}
