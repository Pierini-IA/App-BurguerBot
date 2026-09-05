package com.dioburger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para PingController.
 * Valida los endpoints de health check y ping.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests de PingController")
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/ping - Retorna pong")
    void testPing() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    @DisplayName("GET /api/health - Retorna información del sistema")
    void testHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("Dio Burger API"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/ping - Verifica Content-Type")
    void testPingContentType() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"));
    }

    @Test
    @DisplayName("GET /api/health - Verifica Content-Type JSON")
    void testHealthContentType() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    @DisplayName("GET /api/ping - Múltiples requests")
    void testPingMultiplesRequests() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/ping"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("pong"));
        }
    }

    @Test
    @DisplayName("GET /api/health - Verifica estructura completa del response")
    void testHealthEstructuraCompleta() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").isString())
                .andExpect(jsonPath("$.application").isString())
                .andExpect(jsonPath("$.version").isString())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
