package com.dioburger.aspect;

import com.dioburger.exception.FeatureNotAvailableException;
import com.dioburger.model.entity.Local;
import com.dioburger.model.enums.Feature;
import com.dioburger.model.enums.PlanSuscripcion;
import com.dioburger.security.PlanValidationAspect;
import com.dioburger.security.RequiresFeature;
import com.dioburger.service.PlanService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PlanValidationAspect.
 * Valida la interceptación AOP de métodos anotados con @RequiresFeature.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlanValidationAspect - Tests Unitarios AOP")
class PlanValidationAspectTest {

    @Mock
    private PlanService planService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private PlanValidationAspect aspect;

    private Local localPremium;
    private Local localBasico;

    @BeforeEach
    void setUp() {
        localPremium = new Local();
        localPremium.setId(1L);
        localPremium.setNombre("Local Premium");
        localPremium.setTelefono("+5491187654321");
        localPremium.setPlanSuscripcion(PlanSuscripcion.PREMIUM);

        localBasico = new Local();
        localBasico.setId(2L);
        localBasico.setNombre("Local Basico");
        localBasico.setTelefono("+5491187654322");
        localBasico.setPlanSuscripcion(PlanSuscripcion.BASICO);
    }

    // ==================== TESTS DE ACCESO PERMITIDO ====================

    @Test
    @DisplayName("Acceso permitido - Debe ejecutar el método normalmente")
    void accesoPermitido_ejecutaMetodo() throws Throwable {
        // Given
        Method method = getTestMethod("metodoConFeatureRequerida");
        Object[] args = new Object[]{ localPremium, "parametro2" };
        Object expectedResult = "resultado exitoso";

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // validarAccesoFeature no lanza excepción = tiene acceso
        doNothing().when(planService).validarAccesoFeature(localPremium, Feature.BOT_WHATSAPP);

        // When
        Object result = aspect.validateFeatureAccess(joinPoint);

        // Then
        assertEquals(expectedResult, result, "Debe retornar el resultado del método");
        verify(planService, times(1)).validarAccesoFeature(localPremium, Feature.BOT_WHATSAPP);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("Acceso permitido con mensaje custom - Debe ejecutar método")
    void accesoPermitidoConMensajeCustom_ejecutaMetodo() throws Throwable {
        // Given
        Method method = getTestMethod("metodoConMensajePersonalizado");
        Object[] args = new Object[]{ localPremium };
        Object expectedResult = "resultado custom";

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(expectedResult);

        doNothing().when(planService).validarAccesoFeature(
            eq(localPremium), 
            eq(Feature.REPORTES_AVANZADOS),
            eq("Necesitas plan Premium para ver reportes avanzados")
        );

        // When
        Object result = aspect.validateFeatureAccess(joinPoint);

        // Then
        assertEquals(expectedResult, result);
        verify(planService, times(1)).validarAccesoFeature(
            localPremium, 
            Feature.REPORTES_AVANZADOS,
            "Necesitas plan Premium para ver reportes avanzados"
        );
        verify(joinPoint, times(1)).proceed();
    }

    // ==================== TESTS DE ACCESO DENEGADO ====================

    @Test
    @DisplayName("Acceso denegado - Debe lanzar FeatureNotAvailableException")
    void accesoDenegado_lanzaExcepcion() throws Throwable {
        // Given
        Method method = getTestMethod("metodoConFeatureRequerida");
        Object[] args = new Object[]{ localBasico, "parametro2" };

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);

        FeatureNotAvailableException exception = new FeatureNotAvailableException(
            Feature.BOT_WHATSAPP,
            PlanSuscripcion.BASICO,
            PlanSuscripcion.ESTANDAR
        );

        doThrow(exception).when(planService).validarAccesoFeature(localBasico, Feature.BOT_WHATSAPP);

        // When & Then
        FeatureNotAvailableException thrown = assertThrows(
            FeatureNotAvailableException.class,
            () -> aspect.validateFeatureAccess(joinPoint)
        );

        assertEquals(Feature.BOT_WHATSAPP, thrown.getFeature());
        assertEquals(PlanSuscripcion.BASICO, thrown.getPlanActual());
        assertEquals(PlanSuscripcion.ESTANDAR, thrown.getPlanRequerido());
        verify(joinPoint, never()).proceed(); // NO debe ejecutar el método
    }

    @Test
    @DisplayName("Acceso denegado con mensaje custom - Lanza excepción personalizada")
    void accesoDenegadoConMensajeCustom_lanzaExcepcionPersonalizada() throws Throwable {
        // Given
        Method method = getTestMethod("metodoConMensajePersonalizado");
        Object[] args = new Object[]{ localBasico };

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);

        String customMessage = "Necesitas plan Premium para ver reportes avanzados";
        FeatureNotAvailableException exception = new FeatureNotAvailableException(
            customMessage,
            Feature.REPORTES_AVANZADOS,
            PlanSuscripcion.BASICO,
            PlanSuscripcion.PREMIUM
        );

        doThrow(exception).when(planService).validarAccesoFeature(
            localBasico, 
            Feature.REPORTES_AVANZADOS,
            customMessage
        );

        // When & Then
        FeatureNotAvailableException thrown = assertThrows(
            FeatureNotAvailableException.class,
            () -> aspect.validateFeatureAccess(joinPoint)
        );

        assertEquals(customMessage, thrown.getMessage());
        assertEquals(Feature.REPORTES_AVANZADOS, thrown.getFeature());
        verify(joinPoint, never()).proceed();
    }

    // ==================== TESTS DE CASOS EDGE ====================

    @Test
    @DisplayName("Método sin parámetro Local - Debe lanzar IllegalStateException")
    void metodoSinParametroLocal_lanzaIllegalStateException() throws Throwable {
        // Given
        Method method = getTestMethod("metodoSinLocal");
        Object[] args = new Object[]{ "parametro1", 123 }; // Sin Local

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);

        // When & Then
        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> aspect.validateFeatureAccess(joinPoint)
        );

        assertTrue(thrown.getMessage().contains("deben recibir un parámetro de tipo Local"));
        assertTrue(thrown.getMessage().contains("metodoSinLocal"));
        verify(planService, never()).validarAccesoFeature(any(), any());
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Método con Local null - Debe lanzar IllegalStateException")
    void metodoConLocalNull_lanzaIllegalStateException() throws Throwable {
        // Given
        Method method = getTestMethod("metodoConFeatureRequerida");
        Object[] args = new Object[]{ null, "parametro2" }; // Local = null

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);

        // When & Then
        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> aspect.validateFeatureAccess(joinPoint)
        );

        assertTrue(thrown.getMessage().contains("deben recibir un parámetro de tipo Local"));
        verify(planService, never()).validarAccesoFeature(any(), any());
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Método con múltiples parámetros - Debe encontrar el Local correctamente")
    void metodoConMultiplesParametros_encuentraLocal() throws Throwable {
        // Given
        Method method = getTestMethod("metodoConMultiplesParametros");
        Object[] args = new Object[]{ "string", 123, localPremium, 45.6 };
        Object expectedResult = "success";

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(expectedResult);

        doNothing().when(planService).validarAccesoFeature(localPremium, Feature.SISTEMA_RESERVAS);

        // When
        Object result = aspect.validateFeatureAccess(joinPoint);

        // Then
        assertEquals(expectedResult, result);
        verify(planService, times(1)).validarAccesoFeature(localPremium, Feature.SISTEMA_RESERVAS);
        verify(joinPoint, times(1)).proceed();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtiene un método de la clase TestClass por nombre.
     * Usado para simular métodos con anotación @RequiresFeature.
     */
    private Method getTestMethod(String methodName) throws NoSuchMethodException {
        return TestClass.class.getMethod(methodName);
    }

    /**
     * Clase auxiliar con métodos anotados para testing.
     * Simula diferentes escenarios de uso de @RequiresFeature.
     */
    public static class TestClass {
        
        @RequiresFeature(Feature.BOT_WHATSAPP)
        public void metodoConFeatureRequerida() {
            // Método de prueba
        }

        @RequiresFeature(value = Feature.REPORTES_AVANZADOS, message = "Necesitas plan Premium para ver reportes avanzados")
        public void metodoConMensajePersonalizado() {
            // Método de prueba
        }

        @RequiresFeature(Feature.BOT_WHATSAPP)
        public void metodoSinLocal() {
            // Método de prueba (sin parámetro Local)
        }

        @RequiresFeature(Feature.SISTEMA_RESERVAS)
        public void metodoConMultiplesParametros() {
            // Método de prueba
        }
    }
}
