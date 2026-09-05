package com.dioburger.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador raíz que proporciona información general sobre la aplicación Dio Burger.
 * Este endpoint sirve como punto de entrada para conocer las capacidades del sistema.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api")
@Tag(name = "📖 Información de la API", description = "Documentación y descripción general de la aplicación Dio Burger")
public class RootController {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${spring.application.name:Dio Burger API}")
    private String appName;

    /**
     * Endpoint raíz que describe la aplicación Dio Burger.
     * Proporciona información detallada sobre las funcionalidades del sistema.
     * 
     * @return Información completa de la aplicación
     */
    @GetMapping
    @Operation(
        summary = "Información de la aplicación",
        description = """
            **Bienvenido a Dio Burger API** 🍔
            
            Sistema completo de gestión para hamburgueserías con arquitectura multi-tenancy.
            
            Esta API permite:
            - Gestionar múltiples locales desde una única instancia
            - Procesar pedidos de Delivery y Take Away
            - Control de inventario en tiempo real
            - Sistema de reservas de mesas
            - Integración con bots de WhatsApp
            - Notificaciones automáticas vía webhooks
            - Panel de cocina en tiempo real con WebSocket
            - Reportes de ventas y análisis de rendimiento
            
            **Explora los endpoints disponibles en las secciones de abajo** 👇
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Información de la aplicación obtenida exitosamente",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Respuesta exitosa",
                        value = """
                            {
                              "application": "Dio Burger API",
                              "version": "1.0.0",
                              "description": "Sistema de gestión para hamburgueserías con multi-tenancy",
                              "timestamp": "2025-10-22T10:30:00",
                              "features": [
                                "Multi-tenancy por teléfono de local",
                                "Gestión de pedidos (Delivery y Take Away)",
                                "Control de stock e ingredientes",
                                "Sistema de reservas",
                                "Autenticación JWT",
                                "WebSocket para actualizaciones en tiempo real",
                                "Integración con bots de WhatsApp",
                                "Webhooks a n8n",
                                "Reportes y análisis de ventas",
                                "Panel de cocina"
                              ],
                              "endpoints": {
                                "swagger": "/swagger-ui/index.html",
                                "api_docs": "/v3/api-docs",
                                "health": "/api/ping"
                              },
                              "authentication": {
                                "type": "JWT Bearer Token",
                                "login_endpoint": "/api/auth/login",
                                "header": "Authorization: Bearer <token>"
                              },
                              "roles": [
                                "ROLE_SUPERADMIN - Acceso total al sistema",
                                "ROLE_ADMIN - Gestión del local",
                                "ROLE_COCINA - Vista de cocina únicamente"
                              ],
                              "technology_stack": [
                                "Spring Boot 3.2.0",
                                "Java 21",
                                "PostgreSQL 15+",
                                "JWT",
                                "WebSocket",
                                "Flyway",
                                "MapStruct"
                              ]
                            }
                            """
                    )
                )
            )
        }
    )
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Información básica
        info.put("application", appName);
        info.put("version", appVersion);
        info.put("description", "Sistema de gestión para hamburgueserías con multi-tenancy");
        info.put("timestamp", LocalDateTime.now());
        
        // Características principales
        info.put("features", List.of(
            "🏢 Multi-tenancy por teléfono de local",
            "📦 Gestión de pedidos (Delivery y Take Away)",
            "📊 Control de stock e ingredientes",
            "📅 Sistema de reservas",
            "🔐 Autenticación JWT",
            "🔔 WebSocket para actualizaciones en tiempo real",
            "🤖 Integración con bots de WhatsApp",
            "🔗 Webhooks a n8n",
            "📈 Reportes y análisis de ventas",
            "👨‍🍳 Panel de cocina en tiempo real",
            "💰 Sistema de promociones",
            "🚚 Delivery automático con estados",
            "⚙️ Configuración flexible de cancelaciones"
        ));
        
        // Endpoints útiles
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("swagger", "/swagger-ui/index.html");
        endpoints.put("api_docs", "/v3/api-docs");
        endpoints.put("health", "/api/ping");
        endpoints.put("websocket", "/ws");
        info.put("endpoints", endpoints);
        
        // Información de autenticación
        Map<String, String> auth = new HashMap<>();
        auth.put("type", "JWT Bearer Token");
        auth.put("login_endpoint", "/api/auth/login");
        auth.put("header", "Authorization: Bearer <token>");
        info.put("authentication", auth);
        
        // Roles del sistema
        info.put("roles", List.of(
            "ROLE_SUPERADMIN - Acceso total al sistema y gestión de locales",
            "ROLE_ADMIN - Gestión completa del local asignado",
            "ROLE_COCINA - Vista de cocina únicamente (solo lectura)"
        ));
        
        // Stack tecnológico
        info.put("technology_stack", List.of(
            "Spring Boot 3.2.0",
            "Java 21",
            "PostgreSQL 15+",
            "JWT (Spring Security)",
            "WebSocket (STOMP)",
            "Flyway (Migraciones)",
            "MapStruct (Mapeo de objetos)",
            "OpenAPI 3.0 (Swagger)"
        ));
        
        // Módulos principales
        Map<String, String> modules = new HashMap<>();
        modules.put("auth", "Autenticación y autorización con JWT");
        modules.put("admin", "Gestión de locales y usuarios (SuperAdmin)");
        modules.put("local", "Operaciones del local (pedidos, reservas, productos)");
        modules.put("bot", "Integración con bots de WhatsApp");
        modules.put("cocina", "Panel de cocina en tiempo real");
        modules.put("reportes", "Análisis de ventas y reportes");
        info.put("modules", modules);
        
        // Información de contacto
        Map<String, String> contact = new HashMap<>();
        contact.put("team", "Dio Burger Team");
        contact.put("email", "support@dioburger.com");
        contact.put("website", "https://dioburger.com");
        info.put("contact", contact);
        
        return ResponseEntity.ok(info);
    }

    /**
     * Endpoint que retorna el estado de salud de la aplicación.
     * Alias del endpoint /api/ping para mayor claridad.
     * 
     * @return Estado de la aplicación
     */
    @GetMapping("/status")
    @Operation(
        summary = "Estado de salud de la aplicación",
        description = "Verifica que la aplicación esté funcionando correctamente",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Aplicación funcionando correctamente",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                            {
                              "status": "UP",
                              "application": "Dio Burger API",
                              "version": "1.0.0",
                              "timestamp": "2025-10-22T10:30:00"
                            }
                            """
                    )
                )
            )
        }
    )
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("application", appName);
        status.put("version", appVersion);
        status.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(status);
    }
}
