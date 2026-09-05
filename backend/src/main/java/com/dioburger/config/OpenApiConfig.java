package com.dioburger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación automática de la API.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Dio Burger API")
                        .version(appVersion)
                        .description("""
                                API REST para gestión de hamburguesería con:
                                
                                - 🔐 Autenticación JWT
                                - 🏪 Multi-tenancy (múltiples locales)
                                - 📦 Gestión de pedidos (Delivery, Take Away)
                                - 📅 Sistema de reservas
                                - 📊 Control de stock e ingredientes
                                - 🔔 WebSocket para actualizaciones en tiempo real
                                - 🤖 Integración con bots de WhatsApp
                                - 📈 Sistema de reportes y análisis de ventas
                                - 🔗 Webhooks para notificaciones a n8n
                                - ♻️ Gestión automática de stock (descuento/restauración)
                                
                                ## Autenticación
                                
                                La mayoría de los endpoints requieren autenticación JWT.
                                
                                1. Login en `/api/auth/login` con credenciales
                                2. Obtener el token JWT
                                3. Usar el token en el header: `Authorization: Bearer <token>`
                                
                                ## Endpoints Públicos
                                
                                - `/api/ping` - Health check
                                - `/api/bot/*` - Endpoints para bots de WhatsApp
                                - `/api/auth/login` - Autenticación
                                
                                ## Roles
                                
                                - **ROLE_ADMIN**: Acceso completo
                                - **ROLE_COCINA**: Solo vista de cocina
                                
                                ## Webhooks
                                
                                El sistema envía notificaciones POST a n8n cuando:
                                - Un pedido TAKE_AWAY está LISTO para retirar
                                - Un pedido DELIVERY está EN_CAMINO
                                
                                Configurar `urlWebhookNotificaciones` en ConfiguracionLocal.
                                
                                ## Estados de Pedido
                                
                                PENDIENTE → CONFIRMADO → EN_PREPARACION → LISTO → EN_CAMINO → ENTREGADO
                                
                                También puede ser CANCELADO en cualquier momento (con restricciones).
                                """)
                        .contact(new Contact()
                                .name("Dio Burger Team")
                                .email("support@dioburger.com")
                                .url("https://dioburger.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor Local"),
                        new Server()
                                .url("https://api.dioburger.com")
                                .description("Producción")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtenido del endpoint /api/auth/login")));
    }
}
