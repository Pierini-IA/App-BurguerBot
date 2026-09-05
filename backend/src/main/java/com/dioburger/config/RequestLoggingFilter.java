package com.dioburger.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Filtro para logging estructurado de peticiones HTTP.
 * Agrega trazabilidad con Request ID único y métricas de tiempo.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Generar Request ID único
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Agregar Request ID al MDC (Mapped Diagnostic Context) para logs
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        // Wrappear request y response para poder leer el contenido
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        Instant startTime = Instant.now();

        try {
            // Log de entrada
            logRequest(requestWrapper, requestId);

            // Continuar con la cadena de filtros
            filterChain.doFilter(requestWrapper, responseWrapper);

            // Calcular duración
            Duration duration = Duration.between(startTime, Instant.now());

            // Log de salida
            logResponse(responseWrapper, requestId, duration);

        } finally {
            // Importante: copiar el contenido de vuelta a la response original
            responseWrapper.copyBodyToResponse();
            
            // Limpiar MDC
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    /**
     * Log de la petición entrante.
     */
    private void logRequest(HttpServletRequest request, String requestId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("➡️  {} {} {} | IP: {} | User-Agent: {}",
                method,
                uri,
                queryString != null ? "?" + queryString : "",
                remoteAddr,
                userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 50)) : "N/A");
    }

    /**
     * Log de la respuesta saliente.
     */
    private void logResponse(
            ContentCachingResponseWrapper response,
            String requestId,
            Duration duration) {

        int status = response.getStatus();
        long timeMs = duration.toMillis();

        // Emoji según el status code
        String emoji = getStatusEmoji(status);
        String level = getStatusLevel(status);

        String logMessage = String.format("%s  Status: %d | Duration: %dms",
                emoji, status, timeMs);

        // Log según nivel
        switch (level) {
            case "ERROR":
                log.error(logMessage);
                break;
            case "WARN":
                log.warn(logMessage);
                break;
            default:
                log.info(logMessage);
        }
    }

    /**
     * Obtiene la IP real del cliente (considerando proxies).
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    /**
     * Emoji según el status code HTTP.
     */
    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) {
            return "✅"; // 2xx Success
        } else if (status >= 300 && status < 400) {
            return "↩️"; // 3xx Redirection
        } else if (status >= 400 && status < 500) {
            return "⚠️"; // 4xx Client Error
        } else if (status >= 500) {
            return "❌"; // 5xx Server Error
        }
        return "ℹ️";
    }

    /**
     * Nivel de log según el status code.
     */
    private String getStatusLevel(int status) {
        if (status >= 500) {
            return "ERROR";
        } else if (status >= 400) {
            return "WARN";
        }
        return "INFO";
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // No loguear assets estáticos ni actuator
        return path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".ico");
    }
}
