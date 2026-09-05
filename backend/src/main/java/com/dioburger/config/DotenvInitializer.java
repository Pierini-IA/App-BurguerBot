package com.dioburger.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Inicializador que carga variables de entorno desde archivo .env
 * antes de que Spring Boot inicie.
 * 
 * Esto permite usar variables de entorno locales sin necesidad
 * de configurarlas en el sistema operativo.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Slf4j
public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        try {
            // Intentar cargar .env del directorio raíz del proyecto
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()  // No fallar si .env no existe
                    .load();

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> envMap = new HashMap<>();

            // Copiar todas las variables de .env a las propiedades de Spring
            dotenv.entries().forEach(entry -> {
                envMap.put(entry.getKey(), entry.getValue());
            });

            // Agregar las propiedades de .env a Spring Environment
            environment.getPropertySources()
                    .addFirst(new MapPropertySource("dotenvProperties", envMap));

            log.info("✅ Variables de entorno cargadas desde .env ({} variables)", envMap.size());

        } catch (Exception e) {
            log.warn("⚠️ No se pudo cargar archivo .env: {}. Usando variables de entorno del sistema.", 
                    e.getMessage());
        }
    }
}
