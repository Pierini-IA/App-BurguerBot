package com.dioburger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket con STOMP para comunicación en tiempo real.
 * Permite emitir nuevos pedidos a la pantalla de cocina del local.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura el broker de mensajes.
     * - /topic: Broker simple para publicar mensajes
     * - /app: Prefijo para mensajes dirigidos a @MessageMapping
     */
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Habilitar broker simple en memoria para /topic
        config.enableSimpleBroker("/topic");
        
        // Prefijo para destinos de la aplicación
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registra los endpoints de WebSocket.
     * Los clientes se conectarán a /ws para establecer la conexión WebSocket.
     * SockJS proporciona fallback para navegadores que no soportan WebSocket.
     */
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // setAllowedOriginPatterns (no setAllowedOrigins("*")): el filtro CORS de
                // Spring Security tiene allowCredentials=true y rechaza el "*" literal.
                // TODO: en producción, restringir a los dominios del frontend.
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
