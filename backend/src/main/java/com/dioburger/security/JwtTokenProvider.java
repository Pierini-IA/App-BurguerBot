package com.dioburger.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Proveedor de tokens JWT.
 * Genera, valida y extrae información de tokens JWT.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${spring.security.jwt.secret}")
    private String secret;

    @Value("${spring.security.jwt.expiration:86400000}") // 24 horas por defecto
    private Long expirationMs;

    /**
     * Valida la configuración del JWT secret al inicializar el bean.
     * Asegura que el secret esté configurado y tenga la longitud mínima requerida.
     * 
     * @throws IllegalStateException si el secret no está configurado o es demasiado corto
     */
    @PostConstruct
    private void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET no está configurado. " +
                "Debes configurar la variable de entorno JWT_SECRET antes de iniciar la aplicación."
            );
        }
        
        if (secret.length() < 32) {
            throw new IllegalStateException(
                "JWT_SECRET debe tener al menos 32 caracteres para ser seguro. " +
                "Longitud actual: " + secret.length() + " caracteres. " +
                "Genera un secret seguro con: openssl rand -base64 32"
            );
        }
        
        log.info("✅ JWT Secret validado correctamente ({} caracteres)", secret.length());
    }

    /**
     * Genera una clave secreta a partir del string configurado.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT para un usuario.
     * 
     * @param userDetails Detalles del usuario autenticado
     * @return Token JWT como String
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        
        String token = createToken(claims, userDetails.getUsername());
        
        log.info("Token JWT generado para usuario: {}", userDetails.getUsername());
        
        return token;
    }

    /**
     * Crea un token JWT con claims y subject.
     * 
     * @param claims Claims adicionales
     * @param subject Usuario (username)
     * @return Token JWT
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el username del token.
     * 
     * @param token Token JWT
     * @return Username del usuario
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token.
     * 
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token.
     * 
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim
     * @param <T> Tipo del claim
     * @return Valor del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims de un token.
     * 
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica si el token ha expirado.
     * 
     * @param token Token JWT
     * @return true si ha expirado, false en caso contrario
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida un token JWT contra los detalles del usuario.
     * 
     * @param token Token JWT
     * @param userDetails Detalles del usuario
     * @return true si el token es válido, false en caso contrario
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            
            if (isValid) {
                log.debug("Token válido para usuario: {}", username);
            } else {
                log.warn("Token inválido para usuario: {}", username);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida un token JWT básico (solo verifica estructura y expiración).
     * 
     * @param token Token JWT
     * @return true si el token es válido, false en caso contrario
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }
}
