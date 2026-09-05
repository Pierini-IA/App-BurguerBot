package com.dioburger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración de WebClient para realizar llamadas HTTP asíncronas.
 * Se usa para enviar tickets a la impresora vía webhook.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Configuration
public class WebClientConfig {

    /**
     * Bean de WebClient configurado con valores por defecto.
     * WebClient es el cliente HTTP reactivo de Spring WebFlux.
     *
     * @return WebClient configurado
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }
}
