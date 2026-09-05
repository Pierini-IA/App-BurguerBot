package com.dioburger.dto;

import com.dioburger.model.dto.UsuarioCreateDTO;
import com.dioburger.model.enums.Rol;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para validaciones de UsuarioCreateDTO.
 * Valida las restricciones @NotBlank, @NotNull, @Size, @Pattern.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@DisplayName("UsuarioCreateDTO - Validaciones")
class UsuarioCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ===============================
    // Tests Happy Path
    // ===============================

    @Test
    @DisplayName("DTO válido con todos los campos correctos - No debe tener violaciones")
    void validDTO_noViolations() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("admin_user")
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "No debe haber violaciones con datos válidos");
    }

    @Test
    @DisplayName("DTO válido para SUPERADMIN sin teléfono - No debe tener violaciones")
    void validDTOSuperAdmin_noTelefonoRequired_noViolations() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("superadmin")
                .password("SuperSecure123!")
                .rol(Rol.ROLE_SUPERADMIN)
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "SUPERADMIN no requiere teléfono local");
    }

    // ===============================
    // Tests Username - @NotBlank
    // ===============================

    @Test
    @DisplayName("Username null - Debe tener violación @NotBlank")
    void usernameNull_hasViolation() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username(null)
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "Debe haber violación cuando username es null");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("obligatorio")),
                "El mensaje debe indicar que es obligatorio");
    }

    @Test
    @DisplayName("Username vacío - Debe tener violación @NotBlank")
    void usernameEmpty_hasViolation() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("")
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Username solo espacios - Debe tener violación @NotBlank")
    void usernameBlank_hasViolation() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("   ")
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
    }

    // ===============================
    // Tests Username - @Size
    // ===============================

    @Test
    @DisplayName("Username muy corto (< 4 caracteres) - Debe tener violación @Size")
    void usernameTooShort_hasViolation() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("abc")
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("entre 4 y 50")),
                "El mensaje debe indicar el rango de caracteres");
    }

    @Test
    @DisplayName("Username muy largo (> 50 caracteres) - Debe tener violación @Size")
    void usernameTooLong_hasViolation() {
        // Given
        String longUsername = "a".repeat(51);
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username(longUsername)
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("entre 4 y 50")));
    }

    @Test
    @DisplayName("Username con 4 caracteres (mínimo válido) - No debe tener violaciones")
    void usernameMinLength_noViolations() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("user")
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Username con 50 caracteres (máximo válido) - No debe tener violaciones")
    void usernameMaxLength_noViolations() {
        // Given
        String maxUsername = "a".repeat(50);
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username(maxUsername)
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    // ===============================
    // Tests Username - @Pattern
    // ===============================

    @Test
    @DisplayName("Username con caracteres especiales inválidos - Debe tener violación @Pattern")
    void usernameInvalidChars_hasViolation() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("user@admin")
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("letras, números y guiones bajos")),
                "El mensaje debe indicar los caracteres permitidos");
    }

    @Test
    @DisplayName("Username con espacios - Debe tener violación @Pattern")
    void usernameWithSpaces_hasViolation() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("user admin")
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Username con guiones bajos válido - No debe tener violaciones")
    void usernameWithUnderscores_noViolations() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("admin_user_123")
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    // ===============================
    // Tests Password - @NotBlank
    // ===============================

    @Test
    @DisplayName("Password null - Debe tener violación @NotBlank")
    void passwordNull_hasViolation() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("admin_user")
                .password(null)
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("obligatoria")));
    }

    @Test
    @DisplayName("Password vacío - Debe tener violación @NotBlank")
    void passwordEmpty_hasViolation() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("admin_user")
                .password("")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
    }

    // ===============================
    // Tests Password - @Size
    // ===============================

    @Test
    @DisplayName("Password muy corta (< 8 caracteres) - Debe tener violación @Size")
    void passwordTooShort_hasViolation() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("admin_user")
                .password("Pass123")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("al menos 8")));
    }

    @Test
    @DisplayName("Password con 8 caracteres (mínimo válido) - No debe tener violaciones")
    void passwordMinLength_noViolations() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("admin_user")
                .password("Pass1234")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    // ===============================
    // Tests Rol - @NotNull
    // ===============================

    @Test
    @DisplayName("Rol null - Debe tener violación @NotNull")
    void rolNull_hasViolation() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("admin_user")
                .password("Password123!")
                .rol(null)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("obligatorio")));
    }

    // ===============================
    // Tests Diferentes Roles
    // ===============================

    @Test
    @DisplayName("DTO válido con rol ADMIN - No debe tener violaciones")
    void validDTOWithAdminRole_noViolations() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("admin_user")
                .password("Password123!")
                .rol(Rol.ROLE_ADMIN)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("DTO válido con rol COCINA - No debe tener violaciones")
    void validDTOWithCocinaRole_noViolations() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("cocinero_juan")
                .password("Password123!")
                .rol(Rol.ROLE_COCINA)
                .telefonoLocal("+549349366512")
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("DTO válido con rol SUPERADMIN - No debe tener violaciones")
    void validDTOWithSuperAdminRole_noViolations() {
        // Given
        UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                .username("superadmin")
                .password("SuperSecure123!")
                .rol(Rol.ROLE_SUPERADMIN)
                .build();

        // When
        Set<ConstraintViolation<UsuarioCreateDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }
}
