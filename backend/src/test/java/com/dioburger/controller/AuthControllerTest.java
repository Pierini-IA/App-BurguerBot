package com.dioburger.controller;

import com.dioburger.model.dto.JwtResponseDTO;
import com.dioburger.model.dto.LoginDTO;
import com.dioburger.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para AuthController.
 * Valida el endpoint de autenticación y generación de JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests de AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private LoginDTO loginDTO;
    private JwtResponseDTO jwtResponse;

    @BeforeEach
    void setUp() {
        // Datos de login válidos
        loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("password123");

        // Respuesta JWT simulada
        jwtResponse = new JwtResponseDTO();
        jwtResponse.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token");
        jwtResponse.setType("Bearer");
        jwtResponse.setUsername("admin");
        jwtResponse.setRol("ROLE_ADMIN");
        jwtResponse.setTelefonoLocal("+5491187654321");
    }

    @Test
    @DisplayName("POST /api/auth/login - Login exitoso")
    void testLoginExitoso() throws Exception {
        // Arrange
        when(authService.login(any(LoginDTO.class))).thenReturn(jwtResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtResponse.getToken()))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.rol").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.telefonoLocal").value("+5491187654321"));

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Credenciales inválidas")
    void testLoginCredencialesInvalidas() throws Exception {
        // Arrange
        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Username vacío")
    void testLoginUsernameVacio() throws Exception {
        // Arrange
        loginDTO.setUsername("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Password vacío")
    void testLoginPasswordVacio() throws Exception {
        // Arrange
        loginDTO.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Request sin body")
    void testLoginSinBody() throws Exception {
        // Act & Assert - Spring devuelve 500 cuando no puede parsear el body
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(authService, never()).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Request con JSON inválido")
    void testLoginJsonInvalido() throws Exception {
        // Act & Assert - Spring devuelve 500 cuando el JSON es inválido
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().is5xxServerError());

        verify(authService, never()).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Error interno del servidor")
    void testLoginErrorInterno() throws Exception {
        // Arrange
        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new RuntimeException("Error interno"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().is5xxServerError());

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Username con espacios en blanco")
    void testLoginUsernameConEspacios() throws Exception {
        // Arrange
        loginDTO.setUsername("   ");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Content-Type incorrecto")
    void testLoginContentTypeIncorrecto() throws Exception {
        // Act & Assert - Spring devuelve 500 cuando Content-Type no es JSON
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().is5xxServerError());

        verify(authService, never()).login(any(LoginDTO.class));
    }
}
