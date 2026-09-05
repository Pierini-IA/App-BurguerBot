package com.dioburger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Dio Burger API.
 * Backend Multi-Tenancy para gestión de hamburgueserías.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 * @since 2025-10-21
 */
@SpringBootApplication
public class DioBurgerApplication {

    /**
     * Punto de entrada principal de la aplicación.
     * 
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(DioBurgerApplication.class, args);
    }
}
