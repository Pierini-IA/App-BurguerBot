package com.dioburger.config;

import com.dioburger.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de seguridad con JWT.
 * Define las reglas de autenticación y autorización para todos los endpoints.
 * 
 * Seguridad por endpoint:
 * - /api/auth/** - Público (login)
 * - /api/bot/** - Público (API genérica de bot, uso interno/testing)
 * - /api/webhooks/meta/** - Público (webhooks de Meta: WhatsApp, comentarios IG/FB)
 * - /api/admin/** - Solo ROLE_ADMIN
 * - /api/cocina/** - ROLE_ADMIN o ROLE_COCINA
 * - /api/local/** - Autenticado (cualquier rol)
 * 
 * @author Dio Burger Team
 * @version 1.4.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Orígenes permitidos por CORS. Coma-separado. En el deploy hay que
     * setear {@code CORS_ALLOWED_ORIGINS} con el dominio del frontend
     * (ej. {@code https://mi-app.vercel.app}).
     */
    @org.springframework.beans.factory.annotation.Value(
            "${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://127.0.0.1:3000}")
    private String corsAllowedOrigins;

    /**
     * Configura la cadena de filtros de seguridad.
     * 
     * @param http configuración de seguridad HTTP
     * @return cadena de filtros configurada
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (usamos JWT)
                .csrf(csrf -> csrf.disable())
                
                // Configurar CORS (permitir peticiones desde el frontend)
                .cors(cors -> cors.configure(http))
                
                // Configurar autorización por endpoint
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/bot/**").permitAll()
                        .requestMatchers("/api/webhooks/meta/**").permitAll()
                        .requestMatchers("/api", "/api/status", "/api/ping", "/api/health", "/api/locales").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Menú del local: público (lo ve el cliente, ej. desde un QR).
                        .requestMatchers(HttpMethod.GET, "/api/menu/**").permitAll()
                        // Handshake de WebSocket/SockJS: público. La autenticación del
                        // canal STOMP se hace en el frame CONNECT (header Authorization).
                        .requestMatchers("/ws/**").permitAll()
                        
                        // Documentación de la API (público)
                        .requestMatchers("/", "/docs").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        
                        // Endpoints del SuperAdmin (solo SUPERADMIN)
                        .requestMatchers("/api/superadmin/**").hasRole("SUPERADMIN")
                        
                        // Endpoints administrativos (ADMIN o SUPERADMIN)
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        
                        // Endpoints de cocina (ADMIN o COCINA)
                        .requestMatchers("/api/cocina/**").hasAnyRole("ADMIN", "COCINA")
                        
                        // Endpoints del local (cualquier usuario autenticado)
                        .requestMatchers("/api/local/**").authenticated()
                        
                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                )
                
                // Configurar sesiones como STATELESS (sin estado, usamos JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Agregar el filtro JWT antes del filtro de autenticación estándar
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Proveedor de autenticación que usa UserDetailsService.
     * 
     * @return proveedor de autenticación configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Bean para codificación de contraseñas con BCrypt.
     * 
     * @return encoder BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración de CORS para permitir peticiones desde el frontend.
     * 
     * @return configuración de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes del frontend. En dev: localhost:3000. En deploy: CORS_ALLOWED_ORIGINS.
        configuration.setAllowedOrigins(Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Exponer headers en la respuesta
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Permitir envío de credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Tiempo de cache para preflight requests (1 hora)
        configuration.setMaxAge(3600L);
        
        // Aplicar configuración a todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Authentication Manager para manejar autenticación.
     * 
     * @param config configuración de autenticación
     * @return authentication manager
     * @throws Exception si hay error
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
}
