package com.dioburger.security;

import com.dioburger.model.enums.Feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar endpoints que requieren una feature específica del plan.
 * 
 * Uso:
 * <pre>
 * {@code
 * @RequiresFeature(Feature.BOT_WHATSAPP)
 * @GetMapping("/bot/menu/{telefonoLocal}")
 * public ResponseEntity<?> obtenerMenu(...) {
 *     // ...
 * }
 * }
 * </pre>
 * 
 * El aspecto {@link PlanValidationAspect} interceptará esta anotación
 * y validará automáticamente si el local tiene acceso a la feature.
 * 
 * @author Dio Burger Team
 * @version 2.2.0
 * @see PlanValidationAspect
 * @see Feature
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresFeature {
    
    /**
     * Feature requerida para acceder al endpoint.
     * 
     * @return feature requerida
     */
    Feature value();
    
    /**
     * Mensaje personalizado de error cuando no se tiene acceso.
     * Si está vacío, se usa un mensaje por defecto.
     * 
     * @return mensaje de error personalizado
     */
    String message() default "";
}
