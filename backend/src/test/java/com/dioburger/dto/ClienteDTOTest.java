package com.dioburger.dto;

import com.dioburger.model.dto.ClienteDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de validación para ClienteDTO.
 * Valida las restricciones de Bean Validation API.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteDTO - Validaciones")
class ClienteDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== TESTS - Nombre ====================

    @Test
    @DisplayName("Nombre null viola @NotBlank")
    void nombreNull_violatesNotBlank() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre(null)
                .telefono("+549123456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));
    }

    @Test
    @DisplayName("Nombre vacío viola @NotBlank")
    void nombreEmpty_violatesNotBlank() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("")
                .telefono("+549123456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));
    }

    @Test
    @DisplayName("Nombre solo espacios viola @NotBlank")
    void nombreBlank_violatesNotBlank() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("   ")
                .telefono("+549123456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));
    }

    @Test
    @DisplayName("Nombre válido con una palabra")
    void nombreOneWord_noViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("Juan")
                .telefono("+549123456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Nombre válido con nombre completo")
    void nombreCompleto_noViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("Juan Carlos Pérez")
                .telefono("+549123456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Nombre válido con caracteres especiales (acentos, ñ)")
    void nombreConAcentos_noViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("José María Núñez")
                .telefono("+549123456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - Teléfono ====================

    @Test
    @DisplayName("Teléfono null viola @NotBlank")
    void telefonoNull_violatesNotBlank() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("Juan Pérez")
                .telefono(null)
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("telefono")));
    }

    @Test
    @DisplayName("Teléfono vacío viola @NotBlank")
    void telefonoEmpty_violatesNotBlank() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("Juan Pérez")
                .telefono("")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("telefono")));
    }

    @Test
    @DisplayName("Teléfono solo espacios viola @NotBlank")
    void telefonoBlank_violatesNotBlank() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("Juan Pérez")
                .telefono("   ")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("telefono")));
    }

    @Test
    @DisplayName("Teléfono válido formato internacional Argentina")
    void telefonoInternacionalArgentina_noViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("Juan Pérez")
                .telefono("+5491123456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Teléfono válido formato corto")
    void telefonoFormatoCorto_noViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("Juan Pérez")
                .telefono("+549123456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Teléfono válido sin prefijo + también funciona (validación laxa)")
    void telefonoSinPlus_noViolations() {
        // Nota: La validación actual solo verifica @NotBlank, no el formato
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("Juan Pérez")
                .telefono("549123456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - Happy Path ====================

    @Test
    @DisplayName("ClienteDTO completamente válido")
    void validClienteDTO_noViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("María González")
                .telefono("+549349366512")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("ClienteDTO válido con nombre corto")
    void validClienteDTONombreCorto_noViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("Ana")
                .telefono("+541112345678")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("ClienteDTO válido con nombre largo")
    void validClienteDTONombreLargo_noViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("María José Del Carmen Fernández López")
                .telefono("+549223456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // ==================== TESTS - Edge Cases ====================

    @Test
    @DisplayName("Nombre con números también es válido (sin validación de formato)")
    void nombreConNumeros_noViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("Juan 2")
                .telefono("+549123456789")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Ambos campos null - múltiples violaciones")
    void ambosCamposNull_multipleViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre(null)
                .telefono(null)
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertEquals(2, violations.size());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("telefono")));
    }

    @Test
    @DisplayName("Ambos campos vacíos - múltiples violaciones")
    void ambosCamposEmpty_multipleViolations() {
        ClienteDTO dto = ClienteDTO.builder()
                .nombre("")
                .telefono("")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(dto);

        assertEquals(2, violations.size());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("telefono")));
    }
}
