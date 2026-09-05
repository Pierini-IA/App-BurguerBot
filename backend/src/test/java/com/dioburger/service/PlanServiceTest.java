package com.dioburger.service;

import com.dioburger.model.entity.Local;
import com.dioburger.model.enums.Feature;
import com.dioburger.model.enums.PlanSuscripcion;
import com.dioburger.service.PlanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PlanService.
 * Valida la lógica de verificación de features según el plan de suscripción.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlanService - Tests Unitarios")
class PlanServiceTest {

    @InjectMocks
    private PlanServiceImpl planService;

    private Local localBasico;
    private Local localEstandar;
    private Local localPremium;

    @BeforeEach
    void setUp() {
        // Local con Plan BASICO
        localBasico = new Local();
        localBasico.setId(1L);
        localBasico.setNombre("Local Basico");
        localBasico.setTelefono("+5491187654321");
        localBasico.setPlanSuscripcion(PlanSuscripcion.BASICO);

        // Local con Plan ESTANDAR
        localEstandar = new Local();
        localEstandar.setId(2L);
        localEstandar.setNombre("Local Estandar");
        localEstandar.setTelefono("+5491187654322");
        localEstandar.setPlanSuscripcion(PlanSuscripcion.ESTANDAR);

        // Local con Plan PREMIUM
        localPremium = new Local();
        localPremium.setId(3L);
        localPremium.setNombre("Local Premium");
        localPremium.setTelefono("+5491187654323");
        localPremium.setPlanSuscripcion(PlanSuscripcion.PREMIUM);
    }

    // ==================== PLAN BASICO ====================

    @Test
    @DisplayName("BASICO: Tiene acceso a PANEL_WEB")
    void basico_tieneAcceso_panelWeb() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localBasico, Feature.PANEL_WEB);

        // Then
        assertTrue(tieneAcceso, "Plan BASICO debe tener PANEL_WEB");
    }

    @Test
    @DisplayName("BASICO: Tiene acceso a GESTION_PRODUCTOS")
    void basico_tieneAcceso_gestionProductos() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localBasico, Feature.GESTION_PRODUCTOS);

        // Then
        assertTrue(tieneAcceso, "Plan BASICO debe tener GESTION_PRODUCTOS");
    }

    @Test
    @DisplayName("BASICO: NO tiene acceso a BOT_WHATSAPP")
    void basico_noTieneAcceso_botWhatsapp() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localBasico, Feature.BOT_WHATSAPP);

        // Then
        assertFalse(tieneAcceso, "Plan BASICO NO debe tener BOT_WHATSAPP");
    }

    @Test
    @DisplayName("BASICO: NO tiene acceso a REPORTES_AVANZADOS")
    void basico_noTieneAcceso_reportesAvanzados() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localBasico, Feature.REPORTES_AVANZADOS);

        // Then
        assertFalse(tieneAcceso, "Plan BASICO NO debe tener REPORTES_AVANZADOS");
    }

    // ==================== PLAN ESTANDAR ====================

    @Test
    @DisplayName("ESTANDAR: Tiene acceso a BOT_WHATSAPP")
    void estandar_tieneAcceso_botWhatsapp() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localEstandar, Feature.BOT_WHATSAPP);

        // Then
        assertTrue(tieneAcceso, "Plan ESTANDAR debe tener BOT_WHATSAPP");
    }

    @Test
    @DisplayName("ESTANDAR: Tiene acceso a SISTEMA_RESERVAS")
    void estandar_tieneAcceso_sistemaReservas() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localEstandar, Feature.SISTEMA_RESERVAS);

        // Then
        assertTrue(tieneAcceso, "Plan ESTANDAR debe tener SISTEMA_RESERVAS");
    }

    @Test
    @DisplayName("ESTANDAR: NO tiene acceso a REPORTES_AVANZADOS")
    void estandar_noTieneAcceso_reportesAvanzados() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localEstandar, Feature.REPORTES_AVANZADOS);

        // Then
        assertFalse(tieneAcceso, "Plan ESTANDAR NO debe tener REPORTES_AVANZADOS");
    }

    // ==================== PLAN PREMIUM ====================

    @Test
    @DisplayName("PREMIUM: Tiene acceso a todas las features")
    void premium_tieneAcceso_todasLasFeatures() {
        // When & Then - Validar todas las features
        for (Feature feature : Feature.values()) {
            boolean tieneAcceso = planService.tieneAccesoFeature(localPremium, feature);
            assertTrue(tieneAcceso, 
                    "Plan PREMIUM debe tener acceso a " + feature.name());
        }
    }

    @Test
    @DisplayName("PREMIUM: Tiene acceso a REPORTES_AVANZADOS")
    void premium_tieneAcceso_reportesAvanzados() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localPremium, Feature.REPORTES_AVANZADOS);

        // Then
        assertTrue(tieneAcceso, "Plan PREMIUM debe tener REPORTES_AVANZADOS");
    }

    @Test
    @DisplayName("PREMIUM: Tiene acceso a ASIGNACION_REPARTIDORES")
    void premium_tieneAcceso_asignacionRepartidores() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localPremium, Feature.ASIGNACION_REPARTIDORES);

        // Then
        assertTrue(tieneAcceso, "Plan PREMIUM debe tener ASIGNACION_REPARTIDORES");
    }

    // ==================== CASOS EDGE ====================

    @Test
    @DisplayName("Local null - Debe retornar false")
    void localNull_retornaFalse() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(null, Feature.BOT_WHATSAPP);

        // Then
        assertFalse(tieneAcceso, "Local null debe retornar false");
    }

    @Test
    @DisplayName("Local sin plan asignado - Debe retornar false")
    void localSinPlan_retornaFalse() {
        // Given
        Local localSinPlan = new Local();
        localSinPlan.setId(4L);
        localSinPlan.setTelefono("+5491187654324");
        localSinPlan.setPlanSuscripcion(null); // Sin plan

        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localSinPlan, Feature.BOT_WHATSAPP);

        // Then
        assertFalse(tieneAcceso, "Local sin plan debe retornar false");
    }

    @Test
    @DisplayName("Feature null - Debe retornar false")
    void featureNull_retornaFalse() {
        // When
        boolean tieneAcceso = planService.tieneAccesoFeature(localBasico, null);

        // Then
        assertFalse(tieneAcceso, "Feature null debe retornar false");
    }
}
