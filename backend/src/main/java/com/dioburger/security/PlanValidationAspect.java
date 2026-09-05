package com.dioburger.security;

import com.dioburger.model.entity.Local;
import com.dioburger.model.enums.Feature;
import com.dioburger.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspecto AOP para validar acceso a features según el plan de suscripción.
 * Intercepta métodos anotados con @RequiresFeature y valida que el local
 * tenga acceso a la funcionalidad requerida según su plan.
 * 
 * Implementa validación declarativa siguiendo principios SOLID:
 * - Single Responsibility: Solo valida permisos de features
 * - Open/Closed: Extensible sin modificar código existente
 * - Dependency Inversion: Depende de abstracción (PlanService)
 * 
 * @author Dio Burger Team
 * @version 2.2.0
 * @since 2025
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PlanValidationAspect {

    private final PlanService planService;

    /**
     * Intercepta la ejecución de métodos anotados con @RequiresFeature.
     * Valida que el local tenga acceso a la feature antes de ejecutar el método.
     * 
     * Flujo de validación:
     * 1. Extrae la anotación @RequiresFeature del método
     * 2. Busca el parámetro 'Local' en los argumentos del método
     * 3. Valida con PlanService que el local tenga acceso
     * 4. Si no tiene acceso, lanza FeatureNotAvailableException (HTTP 402)
     * 5. Si tiene acceso, ejecuta el método normalmente
     * 
     * @param joinPoint Punto de unión del método interceptado
     * @return Resultado de la ejecución del método
     * @throws Throwable Si hay error en la ejecución o validación
     */
    @Around("@annotation(com.dioburger.security.RequiresFeature)")
    public Object validateFeatureAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Obtener la anotación @RequiresFeature
        RequiresFeature requiresFeature = method.getAnnotation(RequiresFeature.class);
        Feature requiredFeature = requiresFeature.value();
        String customMessage = requiresFeature.message();

        log.debug("Validando acceso a feature: {} en método: {}", 
            requiredFeature, 
            method.getName()
        );

        // Buscar el parámetro Local en los argumentos del método
        Local local = findLocalParameter(joinPoint);
        
        if (local == null) {
            log.error("No se encontró parámetro 'Local' en método anotado con @RequiresFeature: {}", 
                method.getName()
            );
            throw new IllegalStateException(
                "Los métodos anotados con @RequiresFeature deben recibir un parámetro de tipo Local. " +
                "Método: " + method.getName()
            );
        }

        // Validar acceso a la feature
        if (customMessage != null && !customMessage.isEmpty()) {
            planService.validarAccesoFeature(local, requiredFeature, customMessage);
        } else {
            planService.validarAccesoFeature(local, requiredFeature);
        }

        log.debug("Acceso validado exitosamente. Local: {}, Plan: {}, Feature: {}", 
            local.getNombre(), 
            local.getPlanSuscripcion(),
            requiredFeature
        );

        // Si la validación pasa, ejecutar el método
        return joinPoint.proceed();
    }

    /**
     * Busca el parámetro de tipo Local en los argumentos del método.
     * Recorre todos los parámetros y retorna el primero que sea instancia de Local.
     * 
     * @param joinPoint Punto de unión del método
     * @return El objeto Local encontrado, o null si no existe
     */
    private Local findLocalParameter(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        
        for (Object arg : args) {
            if (arg instanceof Local) {
                return (Local) arg;
            }
        }
        
        return null;
    }
}
