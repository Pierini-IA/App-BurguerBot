package com.dioburger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Pool de hilos dedicado a procesar mensajes/comentarios de Meta en segundo
 * plano, para que el webhook pueda responder 200 a Meta de inmediato sin
 * esperar a que termine la clasificación de IA + Graph API.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "metaTaskExecutor")
    public Executor metaTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("meta-bot-");
        executor.initialize();
        return executor;
    }
}
