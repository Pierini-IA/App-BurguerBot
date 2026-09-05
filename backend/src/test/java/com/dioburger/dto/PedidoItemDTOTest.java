package com.dioburger.dto;

import com.dioburger.model.dto.PedidoItemDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de validación para PedidoItemDTO.
 * Valida las restricciones de Bean Validation API.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoItemDTO - Validaciones")
class PedidoItemDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== TESTS - ProductoId ====================

    @Test
    @DisplayName("ProductoId null viola @NotNull")
    void productoIdNull_violatesNotNull() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(null)
                .cantidad(2)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("productoId")));
    }

    @Test
    @DisplayName("ProductoId válido con número positivo")
    void productoIdPositivo_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("ProductoId válido con número grande")
    void productoIdGrande_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(999999L)
                .cantidad(2)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - Cantidad ====================

    @Test
    @DisplayName("Cantidad null viola @NotNull")
    void cantidadNull_violatesNotNull() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(null)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("cantidad")));
    }

    @Test
    @DisplayName("Cantidad 0 viola @Min(1)")
    void cantidadZero_violatesMin() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(0)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("cantidad")
                        && v.getMessage().contains("al menos 1")));
    }

    @Test
    @DisplayName("Cantidad negativa viola @Min(1)")
    void cantidadNegativa_violatesMin() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(-5)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("cantidad")));
    }

    @Test
    @DisplayName("Cantidad 1 es válida (mínimo)")
    void cantidadUno_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(1)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Cantidad 2 es válida")
    void cantidadDos_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Cantidad grande es válida")
    void cantidadGrande_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(100)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - Observaciones (Opcional) ====================

    @Test
    @DisplayName("Observaciones null es válido (campo opcional)")
    void observacionesNull_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .observaciones(null)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Observaciones vacío es válido")
    void observacionesEmpty_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .observaciones("")
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Observaciones con texto es válido")
    void observacionesConTexto_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .observaciones("Sin lechuga, por favor")
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Observaciones largas son válidas")
    void observacionesLargas_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .observaciones("Sin lechuga, sin tomate, sin cebolla, bien cocida, " +
                        "con extra de queso y salsa barbacoa")
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - ExtrasIds (Opcional) ====================

    @Test
    @DisplayName("ExtrasIds null es válido (se inicializa con lista vacía)")
    void extrasIdsNull_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .extrasIds(null)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("ExtrasIds vacío es válido")
    void extrasIdsEmpty_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .extrasIds(List.of())
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("ExtrasIds con un extra es válido")
    void extrasIdsConUnExtra_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .extrasIds(List.of(5L))
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("ExtrasIds con múltiples extras es válido")
    void extrasIdsConMultiples_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .extrasIds(List.of(1L, 3L, 5L, 7L))
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - Happy Path ====================

    @Test
    @DisplayName("PedidoItemDTO válido mínimo (solo campos obligatorios)")
    void validPedidoItemDTOMinimo_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(1)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("PedidoItemDTO válido completo con observaciones")
    void validPedidoItemDTOConObservaciones_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(10L)
                .cantidad(3)
                .observaciones("Bien cocida, sin cebolla")
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("PedidoItemDTO válido completo con extras")
    void validPedidoItemDTOConExtras_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(5L)
                .cantidad(2)
                .extrasIds(List.of(1L, 2L, 3L))
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("PedidoItemDTO válido completo con observaciones y extras")
    void validPedidoItemDTOCompleto_noViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(7L)
                .cantidad(4)
                .observaciones("Sin lechuga")
                .extrasIds(List.of(1L, 5L))
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - Edge Cases ====================

    @Test
    @DisplayName("ProductoId y Cantidad null - múltiples violaciones")
    void productoIdYCantidadNull_multipleViolations() {
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(null)
                .cantidad(null)
                .build();

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);

        assertEquals(2, violations.size());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("productoId")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("cantidad")));
    }

    @Test
    @DisplayName("Builder con valores por defecto funciona correctamente")
    void builderDefaultValues_noViolations() {
        // @Builder.Default en extrasIds inicializa con ArrayList vacío
        PedidoItemDTO dto = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(1)
                .build();

        assertNotNull(dto.getExtrasIds());
        assertTrue(dto.getExtrasIds().isEmpty());

        Set<ConstraintViolation<PedidoItemDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}
