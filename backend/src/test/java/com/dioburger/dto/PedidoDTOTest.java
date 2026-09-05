package com.dioburger.dto;

import com.dioburger.model.dto.ClienteDTO;
import com.dioburger.model.dto.PedidoDTO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de validación para PedidoDTO.
 * Valida las restricciones de Bean Validation API.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoDTO - Validaciones")
class PedidoDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== HELPER METHODS ====================

    private PedidoDTO buildValidPedidoDTO() {
        ClienteDTO cliente = ClienteDTO.builder()
                .nombre("Juan Pérez")
                .telefono("+549123456789")
                .build();

        PedidoItemDTO item = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .build();

        return PedidoDTO.builder()
                .requestId("REQ-12345")
                .cliente(cliente)
                .modalidad("DELIVERY")
                .medioPago("EFECTIVO")
                .items(List.of(item))
                .direccionEnvio("Calle Falsa 123")
                .build();
    }

    // ==================== TESTS - RequestId ====================

    @Test
    @DisplayName("RequestId null viola @NotBlank")
    void requestIdNull_violatesNotBlank() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setRequestId(null);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("requestId")));
    }

    @Test
    @DisplayName("RequestId vacío viola @NotBlank")
    void requestIdEmpty_violatesNotBlank() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setRequestId("");

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("requestId")));
    }

    @Test
    @DisplayName("RequestId solo espacios viola @NotBlank")
    void requestIdBlank_violatesNotBlank() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setRequestId("   ");

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("requestId")));
    }

    @Test
    @DisplayName("RequestId válido - UUID format")
    void requestIdValidUUID_noViolations() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setRequestId("550e8400-e29b-41d4-a716-446655440000");

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - Cliente ====================

    @Test
    @DisplayName("Cliente null viola @NotNull")
    void clienteNull_violatesNotNull() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setCliente(null);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("cliente")));
    }

    @Test
    @DisplayName("Cliente con nombre null viola validación anidada @Valid")
    void clienteNombreNull_violatesNestedValidation() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.getCliente().setNombre(null);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("cliente.nombre")));
    }

    @Test
    @DisplayName("Cliente con teléfono null viola validación anidada @Valid")
    void clienteTelefonoNull_violatesNestedValidation() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.getCliente().setTelefono(null);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("cliente.telefono")));
    }

    // ==================== TESTS - Modalidad ====================

    @Test
    @DisplayName("Modalidad null viola @NotBlank")
    void modalidadNull_violatesNotBlank() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setModalidad(null);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("modalidad")));
    }

    @Test
    @DisplayName("Modalidad vacía viola @NotBlank")
    void modalidadEmpty_violatesNotBlank() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setModalidad("");

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("modalidad")));
    }

    @Test
    @DisplayName("Modalidad DELIVERY es válida")
    void modalidadDelivery_noViolations() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setModalidad("DELIVERY");

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Modalidad RETIRAR es válida")
    void modalidadRetirar_noViolations() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setModalidad("RETIRAR");
        dto.setDireccionEnvio(null); // No necesita dirección si es RETIRAR

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - MedioPago ====================

    @Test
    @DisplayName("MedioPago null viola @NotBlank")
    void medioPagoNull_violatesNotBlank() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setMedioPago(null);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("medioPago")));
    }

    @Test
    @DisplayName("MedioPago vacío viola @NotBlank")
    void medioPagoEmpty_violatesNotBlank() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setMedioPago("");

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("medioPago")));
    }

    @Test
    @DisplayName("MedioPago EFECTIVO es válido")
    void medioPagoEfectivo_noViolations() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setMedioPago("EFECTIVO");

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("MedioPago TRANSFERENCIA es válido")
    void medioPagoTransferencia_noViolations() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setMedioPago("TRANSFERENCIA");

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - Items ====================

    @Test
    @DisplayName("Items null viola @NotEmpty")
    void itemsNull_violatesNotEmpty() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setItems(null);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("items")));
    }

    @Test
    @DisplayName("Items vacío viola @NotEmpty")
    void itemsEmpty_violatesNotEmpty() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setItems(new ArrayList<>());

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("items")));
    }

    @Test
    @DisplayName("Item con productoId null viola validación anidada @Valid")
    void itemProductoIdNull_violatesNestedValidation() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.getItems().get(0).setProductoId(null);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("items")
                        && v.getPropertyPath().toString().contains("productoId")));
    }

    @Test
    @DisplayName("Item con cantidad null viola validación anidada @Valid")
    void itemCantidadNull_violatesNestedValidation() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.getItems().get(0).setCantidad(null);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("items")
                        && v.getPropertyPath().toString().contains("cantidad")));
    }

    @Test
    @DisplayName("Item con cantidad 0 viola @Min(1)")
    void itemCantidadZero_violatesMin() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.getItems().get(0).setCantidad(0);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("items")
                        && v.getPropertyPath().toString().contains("cantidad")));
    }

    @Test
    @DisplayName("Item con cantidad negativa viola @Min(1)")
    void itemCantidadNegativa_violatesMin() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.getItems().get(0).setCantidad(-5);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("items")
                        && v.getPropertyPath().toString().contains("cantidad")));
    }

    @Test
    @DisplayName("Múltiples items válidos - no viola restricciones")
    void multipleItemsValid_noViolations() {
        PedidoDTO dto = buildValidPedidoDTO();
        
        PedidoItemDTO item2 = PedidoItemDTO.builder()
                .productoId(2L)
                .cantidad(3)
                .observaciones("Sin cebolla")
                .build();

        PedidoItemDTO item3 = PedidoItemDTO.builder()
                .productoId(3L)
                .cantidad(1)
                .extrasIds(List.of(1L, 2L))
                .build();

        dto.setItems(List.of(dto.getItems().get(0), item2, item3));

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - Happy Path ====================

    @Test
    @DisplayName("PedidoDTO completamente válido - DELIVERY con dirección")
    void validPedidoDTODelivery_noViolations() {
        PedidoDTO dto = buildValidPedidoDTO();

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("PedidoDTO completamente válido - RETIRAR sin dirección")
    void validPedidoDTORetirar_noViolations() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setModalidad("RETIRAR");
        dto.setDireccionEnvio(null);

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("PedidoDTO válido con horaPedido opcional")
    void validPedidoDTOWithHoraPedido_noViolations() {
        PedidoDTO dto = buildValidPedidoDTO();
        dto.setHoraPedido("20:30");

        Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("PedidoDTO válido con todos los medios de pago")
    void validPedidoDTOAllMediosPago_noViolations() {
        String[] mediosPago = {"EFECTIVO", "TRANSFERENCIA", "TARJETA_DEBITO", "TARJETA_CREDITO", "QR"};

        for (String medio : mediosPago) {
            PedidoDTO dto = buildValidPedidoDTO();
            dto.setMedioPago(medio);

            Set<ConstraintViolation<PedidoDTO>> violations = validator.validate(dto);

            assertTrue(violations.isEmpty(), 
                    "MedioPago " + medio + " debería ser válido pero tuvo violaciones");
        }
    }
}
