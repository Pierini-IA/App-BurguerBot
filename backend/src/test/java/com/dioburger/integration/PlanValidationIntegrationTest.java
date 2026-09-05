package com.dioburger.integration;

import com.dioburger.model.entity.Local;
import com.dioburger.model.enums.Feature;
import com.dioburger.model.enums.PlanSuscripcion;
import com.dioburger.repository.LocalRepository;
import com.dioburger.security.RequiresFeature;
import com.dioburger.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para validación de planes con Spring Context completo.
 * Prueba el flujo completo: AOP Aspect → Service → Repository.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(PlanValidationIntegrationTest.TestControllerForIntegration.class)
@DisplayName("PlanValidation - Tests de Integración")
class PlanValidationIntegrationTest {

    @Autowired
    private LocalRepository localRepository;

    @Autowired
    private PlanService planService;

    @Autowired
    private TestControllerForIntegration testController;

    private Local localPremium;
    private Local localBasico;
    private Local localEstandar;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        localRepository.deleteAll();

        // Crear locales de prueba
        localPremium = new Local();
        localPremium.setNombre("Local Premium Test");
        localPremium.setDireccion("Calle Premium 123");
        localPremium.setTelefono("+5491111111111");
        localPremium.setPlanSuscripcion(PlanSuscripcion.PREMIUM);
        localPremium = localRepository.save(localPremium);

        localBasico = new Local();
        localBasico.setNombre("Local Basico Test");
        localBasico.setDireccion("Calle Basico 456");
        localBasico.setTelefono("+5492222222222");
        localBasico.setPlanSuscripcion(PlanSuscripcion.BASICO);
        localBasico = localRepository.save(localBasico);

        localEstandar = new Local();
        localEstandar.setNombre("Local Estandar Test");
        localEstandar.setDireccion("Calle Estandar 789");
        localEstandar.setTelefono("+5493333333333");
        localEstandar.setPlanSuscripcion(PlanSuscripcion.ESTANDAR);
        localEstandar = localRepository.save(localEstandar);
    }

    // ==================== TESTS DE AOP CON SPRING CONTEXT ====================

    @Test
    @DisplayName("AOP: Local PREMIUM accede a feature PREMIUM - Debe permitir")
    void aop_localPremiumAccedeFeaturePremium_permitido() {
        // When - Llamar método anotado con @RequiresFeature
        ResponseEntity<String> response = testController.metodoConFeaturePremium(localPremium);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Acceso permitido a REPORTES_AVANZADOS", response.getBody());
    }

    @Test
    @DisplayName("AOP: Local BASICO accede a feature PREMIUM - Debe denegar")
    void aop_localBasicoAccedeFeaturePremium_denegado() {
        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            testController.metodoConFeaturePremium(localBasico);
        });

        assertTrue(exception.getMessage().contains("no está disponible") 
                || exception.getCause().getMessage().contains("no está disponible"));
    }

    @Test
    @DisplayName("AOP: Local ESTANDAR accede a feature ESTANDAR - Debe permitir")
    void aop_localEstandarAccedeFeatureEstandar_permitido() {
        // When
        ResponseEntity<String> response = testController.metodoConFeatureEstandar(localEstandar);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Acceso permitido a BOT_WHATSAPP", response.getBody());
    }

    @Test
    @DisplayName("AOP: Local BASICO accede a feature ESTANDAR - Debe denegar")
    void aop_localBasicoAccedeFeatureEstandar_denegado() {
        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            testController.metodoConFeatureEstandar(localBasico);
        });

        assertTrue(exception.getMessage().contains("no está disponible") 
                || exception.getCause().getMessage().contains("no está disponible"));
    }

    @Test
    @DisplayName("AOP: Local PREMIUM accede a feature BASICA - Debe permitir")
    void aop_localPremiumAccedeFeatureBasica_permitido() {
        // When
        ResponseEntity<String> response = testController.metodoConFeatureBasica(localPremium);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Acceso permitido a PANEL_WEB", response.getBody());
    }

    @Test
    @DisplayName("AOP: Mensaje personalizado en error - Debe usar mensaje custom")
    void aop_mensajePersonalizadoEnError_usaMensajeCustom() {
        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            testController.metodoConMensajePersonalizado(localBasico);
        });

        String message = exception.getCause() != null 
            ? exception.getCause().getMessage() 
            : exception.getMessage();

        assertTrue(message.contains("Actualiza a Premium"));
    }

    // ==================== TESTS DE SERVICIO CON REPOSITORY REAL ====================

    @Test
    @DisplayName("Service: tieneAccesoFeature con BD real - PREMIUM tiene todo")
    void service_tieneAccesoFeatureConBD_premiumTieneTodo() {
        // When & Then
        for (Feature feature : Feature.values()) {
            boolean tieneAcceso = planService.tieneAccesoFeature(localPremium, feature);
            assertTrue(tieneAcceso, 
                "Local PREMIUM debe tener acceso a " + feature.name());
        }
    }

    @Test
    @DisplayName("Service: tieneAccesoFeature con BD real - BASICO limitado")
    void service_tieneAccesoFeatureConBD_basicoLimitado() {
        // When
        boolean tienePanelWeb = planService.tieneAccesoFeature(localBasico, Feature.PANEL_WEB);
        boolean tieneBot = planService.tieneAccesoFeature(localBasico, Feature.BOT_WHATSAPP);
        boolean tieneReportes = planService.tieneAccesoFeature(localBasico, Feature.REPORTES_AVANZADOS);

        // Then
        assertTrue(tienePanelWeb, "BASICO debe tener PANEL_WEB");
        assertFalse(tieneBot, "BASICO NO debe tener BOT_WHATSAPP");
        assertFalse(tieneReportes, "BASICO NO debe tener REPORTES_AVANZADOS");
    }

    @Test
    @DisplayName("Service: tieneAccesoFeature con BD real - ESTANDAR intermedio")
    void service_tieneAccesoFeatureConBD_estandarIntermedio() {
        // When
        boolean tieneBot = planService.tieneAccesoFeature(localEstandar, Feature.BOT_WHATSAPP);
        boolean tieneReservas = planService.tieneAccesoFeature(localEstandar, Feature.SISTEMA_RESERVAS);
        boolean tieneReportes = planService.tieneAccesoFeature(localEstandar, Feature.REPORTES_AVANZADOS);

        // Then
        assertTrue(tieneBot, "ESTANDAR debe tener BOT_WHATSAPP");
        assertTrue(tieneReservas, "ESTANDAR debe tener SISTEMA_RESERVAS");
        assertFalse(tieneReportes, "ESTANDAR NO debe tener REPORTES_AVANZADOS");
    }

    @Test
    @DisplayName("Repository: Local guardado mantiene el plan correctamente")
    void repository_localGuardadoMantienePlan() {
        // When
        Local localRecuperado = localRepository.findById(localPremium.getId()).orElse(null);

        // Then
        assertNotNull(localRecuperado);
        assertEquals(PlanSuscripcion.PREMIUM, localRecuperado.getPlanSuscripcion());
        assertEquals(localPremium.getNombre(), localRecuperado.getNombre());
    }

    @Test
    @DisplayName("Repository: Actualizar plan de local persiste correctamente")
    void repository_actualizarPlanPersiste() {
        // Given
        localBasico.setPlanSuscripcion(PlanSuscripcion.PREMIUM);

        // When
        Local localActualizado = localRepository.save(localBasico);
        Local localRecuperado = localRepository.findById(localBasico.getId()).orElse(null);

        // Then
        assertNotNull(localRecuperado);
        assertEquals(PlanSuscripcion.PREMIUM, localRecuperado.getPlanSuscripcion());
        assertEquals(PlanSuscripcion.PREMIUM, localActualizado.getPlanSuscripcion());
    }

    // ==================== CONTROLLER DE PRUEBA ====================

    /**
     * Controller de prueba para validar AOP en contexto Spring real.
     * Simula endpoints reales con @RequiresFeature.
     */
    @Service
    public static class TestControllerForIntegration {

        @RequiresFeature(Feature.REPORTES_AVANZADOS)
        public ResponseEntity<String> metodoConFeaturePremium(Local local) {
            return ResponseEntity.ok("Acceso permitido a REPORTES_AVANZADOS");
        }

        @RequiresFeature(Feature.BOT_WHATSAPP)
        public ResponseEntity<String> metodoConFeatureEstandar(Local local) {
            return ResponseEntity.ok("Acceso permitido a BOT_WHATSAPP");
        }

        @RequiresFeature(Feature.PANEL_WEB)
        public ResponseEntity<String> metodoConFeatureBasica(Local local) {
            return ResponseEntity.ok("Acceso permitido a PANEL_WEB");
        }

        @RequiresFeature(value = Feature.REPORTES_AVANZADOS, message = "Actualiza a Premium")
        public ResponseEntity<String> metodoConMensajePersonalizado(Local local) {
            return ResponseEntity.ok("Acceso permitido con mensaje custom");
        }
    }
}
