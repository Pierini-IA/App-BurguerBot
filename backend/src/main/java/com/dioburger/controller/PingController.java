package com.dioburger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de prueba para verificar que la API está funcionando.
 * Proporciona endpoints simples para health checks y diagnóstico.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api")
public class PingController {

    /**
     * Endpoint de ping básico para verificar que la API responde.
     * Útil para health checks de contenedores y load balancers.
     * 
     * Endpoint: GET /api/ping
     * 
     * @return mensaje "pong"
     * 
     * @apiNote Ejemplo de uso:
     * GET /api/ping
     * 
     * Response: "pong"
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    /**
     * Endpoint de health check que devuelve información del sistema.
     * Proporciona estado de la aplicación, versión y timestamp.
     * 
     * Endpoint: GET /api/health
     * 
     * @return información del estado de la API
     * 
     * @apiNote Ejemplo de uso:
     * GET /api/health
     * 
     * Response:
     * {
     *   "status": "UP",
     *   "application": "Dio Burger API",
     *   "version": "1.0.0",
     *   "timestamp": "2025-10-22T03:45:00"
     * }
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "Dio Burger API");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
