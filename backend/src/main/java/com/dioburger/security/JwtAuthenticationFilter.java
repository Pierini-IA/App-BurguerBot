package com.dioburger.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT.
 * Intercepta cada request, extrae el token del header Authorization,
 * valida el token y establece la autenticación en el contexto de seguridad.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Filtra cada request para validar el token JWT.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Cadena de filtros
     * @throws ServletException Si ocurre un error de servlet
     * @throws IOException Si ocurre un error de I/O
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Extraer el token del header Authorization
            String jwt = extractJwtFromRequest(request);

            // 2. Si hay token, validarlo y autenticar
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.extractUsername(jwt);

                // 3. Cargar los detalles del usuario desde la base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 4. Validar el token contra los detalles del usuario
                if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                    // 5. Crear el objeto de autenticación
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 6. Establecer la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Usuario autenticado: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Error en autenticación JWT: {}", e.getMessage());
            // No lanzar excepción, permitir que el request continue
            // Spring Security manejará la falta de autenticación
        }

        // 7. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     * El header debe tener el formato: "Bearer <token>"
     * 
     * @param request HTTP request
     * @return Token JWT o null si no existe
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remover "Bearer " del inicio
        }

        return null;
    }

    /**
     * Determina si este filtro debe aplicarse al request.
     * Por defecto se aplica a todos los requests excepto los públicos.
     * 
     * @param request HTTP request
     * @return true si debe aplicarse, false en caso contrario
     * @throws ServletException Si ocurre un error
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // No aplicar filtro a endpoints públicos
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/ping") ||
               path.startsWith("/api/locales") ||
               path.startsWith("/ws") ||          // handshake WebSocket/SockJS
               path.startsWith("/h2-console") ||
               path.startsWith("/actuator");
    }
}
