package com.dioburger.controller;

import com.dioburger.model.dto.JwtResponseDTO;
import com.dioburger.model.dto.LoginDTO;
import com.dioburger.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación.
 * Proporciona endpoints para login con JWT.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para autenticar usuarios y obtener un token JWT.
     * 
     * Endpoint: POST /api/auth/login
     * 
     * @param loginDTO Credenciales del usuario
     * @return JwtResponseDTO con el token y datos del usuario
     * 
     * @apiNote Ejemplo de uso:
     * POST /api/auth/login
     * 
     * Request:
     * {
     *   "username": "admin",
     *   "password": "password123"
     * }
     * 
     * Response exitosa (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "username": "admin",
     *   "rol": "ROLE_ADMIN",
     *   "telefonoLocal": "+5491187654321"
     * }
     * 
     * Response fallida (401 Unauthorized):
     * {
     *   "timestamp": "2025-10-21T10:30:00",
     *   "status": 401,
     *   "error": "Unauthorized",
     *   "message": "Credenciales inválidas"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("🔐 Petición de login recibida para usuario: {}", loginDTO.getUsername());

        try {
            JwtResponseDTO response = authService.login(loginDTO);

            log.info("✅ Login exitoso para usuario: {}", loginDTO.getUsername());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("❌ Login fallido para usuario: {} - {}", 
                    loginDTO.getUsername(), e.getMessage());

            return ResponseEntity.status(401).body(
                    new ErrorResponse("Credenciales inválidas")
            );
        } catch (Exception e) {
            log.error("❌ Error inesperado durante login: {}", e.getMessage(), e);

            return ResponseEntity.status(500).body(
                    new ErrorResponse("Error interno del servidor")
            );
        }
    }

    /**
     * Clase interna para respuestas de error.
     */
    private record ErrorResponse(String message) {}
}
