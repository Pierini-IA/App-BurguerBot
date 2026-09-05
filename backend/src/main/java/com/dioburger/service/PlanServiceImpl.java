package com.dioburger.service;

import com.dioburger.exception.FeatureNotAvailableException;
import com.dioburger.model.entity.Local;
import com.dioburger.model.enums.Feature;
import com.dioburger.model.enums.PlanFeatureMatrix;
import com.dioburger.model.enums.PlanSuscripcion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

/**
 * Implementación del servicio de gestión de planes de suscripción.
 * 
 * Responsabilidades:
 * - Validar acceso a features según el plan
 * - Verificar estado de suscripción
 * - Proveer información sobre planes y features
 * 
 * @author Dio Burger Team
 * @version 2.2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanServiceImpl implements PlanService {
    
    @Override
    public void validarAccesoFeature(Local local, Feature feature) {
        log.debug("Validando acceso a feature {} para local {}", feature, local.getId());
        
        // Validar que el plan esté activo
        validarPlanActivo(local);
        
        // Verificar si tiene acceso a la feature
        if (!tieneAccesoFeature(local, feature)) {
            PlanSuscripcion planRequerido = getPlanMinimoParaFeature(feature);
            
            log.warn("⛔ Local {} (Plan: {}) intentó acceder a feature {} (Requiere: {})",
                local.getId(), local.getPlanSuscripcion(), feature, planRequerido);
            
            throw new FeatureNotAvailableException(
                feature,
                local.getPlanSuscripcion(),
                planRequerido
            );
        }
        
        log.debug("✅ Acceso concedido a feature {} para local {}", feature, local.getId());
    }
    
    @Override
    public void validarAccesoFeature(Local local, Feature feature, String customMessage) {
        log.debug("Validando acceso a feature {} para local {} con mensaje personalizado", feature, local.getId());
        
        // Validar que el plan esté activo
        validarPlanActivo(local);
        
        // Verificar si tiene acceso a la feature
        if (!tieneAccesoFeature(local, feature)) {
            PlanSuscripcion planRequerido = getPlanMinimoParaFeature(feature);
            
            log.warn("⛔ Local {} (Plan: {}) intentó acceder a feature {} (Requiere: {})",
                local.getId(), local.getPlanSuscripcion(), feature, planRequerido);
            
            throw new FeatureNotAvailableException(
                customMessage,
                feature,
                local.getPlanSuscripcion(),
                planRequerido
            );
        }
        
        log.debug("✅ Acceso concedido a feature {} para local {}", feature, local.getId());
    }
    
    @Override
    public boolean tieneAccesoFeature(Local local, Feature feature) {
        if (local == null || local.getPlanSuscripcion() == null) {
            log.warn("⚠️ Local o plan nulo al verificar acceso a feature {}", feature);
            return false;
        }
        
        if (!isPlanActivo(local)) {
            log.warn("⚠️ Plan inactivo para local {} al verificar feature {}", local.getId(), feature);
            return false;
        }
        
        boolean tieneAcceso = PlanFeatureMatrix.hasFeature(local.getPlanSuscripcion(), feature);
        
        log.debug("Local {} (Plan: {}) {} acceso a feature {}",
            local.getId(),
            local.getPlanSuscripcion(),
            tieneAcceso ? "TIENE" : "NO TIENE",
            feature
        );
        
        return tieneAcceso;
    }
    
    @Override
    public Set<Feature> getFeaturesDisponibles(Local local) {
        if (local == null || local.getPlanSuscripcion() == null) {
            log.warn("⚠️ Local o plan nulo al obtener features disponibles");
            return Set.of();
        }
        
        return PlanFeatureMatrix.getFeaturesForPlan(local.getPlanSuscripcion());
    }
    
    @Override
    public PlanSuscripcion getPlanMinimoParaFeature(Feature feature) {
        return PlanFeatureMatrix.getMinimumPlanForFeature(feature);
    }
    
    @Override
    public boolean isPlanActivo(Local local) {
        if (local == null) {
            return false;
        }
        
        // Verificar flag de activo
        if (local.getPlanActivo() == null || !local.getPlanActivo()) {
            log.debug("Plan inactivo para local {}", local.getId());
            return false;
        }
        
        // Verificar fecha de vencimiento (si existe)
        if (local.getFechaFinPlan() != null) {
            LocalDate hoy = LocalDate.now();
            boolean vigente = !hoy.isAfter(local.getFechaFinPlan());
            
            if (!vigente) {
                log.warn("⚠️ Plan vencido para local {}. Fecha fin: {}", local.getId(), local.getFechaFinPlan());
            }
            
            return vigente;
        }
        
        // Si no tiene fecha fin, el plan es permanente mientras esté activo
        return true;
    }
    
    @Override
    public void validarPlanActivo(Local local) {
        if (!isPlanActivo(local)) {
            String mensaje;
            
            if (local.getPlanActivo() == null || !local.getPlanActivo()) {
                mensaje = "Tu plan de suscripción está inactivo. Contacta al administrador para reactivarlo.";
            } else if (local.getFechaFinPlan() != null && LocalDate.now().isAfter(local.getFechaFinPlan())) {
                mensaje = String.format(
                    "Tu plan de suscripción venció el %s. Renueva tu plan para continuar usando el servicio.",
                    local.getFechaFinPlan()
                );
            } else {
                mensaje = "Tu plan de suscripción no está activo.";
            }
            
            log.warn("⛔ Plan inactivo para local {}: {}", local.getId(), mensaje);
            throw new IllegalStateException(mensaje);
        }
    }
}
