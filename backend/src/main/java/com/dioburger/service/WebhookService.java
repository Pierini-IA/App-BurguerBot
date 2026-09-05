package com.dioburger.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Servicio base para enviar webhooks a n8n.
 * Centraliza la lógica común de validación y envío de webhooks
 * para evitar duplicación de código (DRY).
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebClient webClient;

    /**
     * Valida que una URL de webhook esté configurada.
     *
     * @param webhookUrl URL a validar
     * @param nombreLocal Nombre del local (para logging)
     * @return true si es válida, false si está vacía
     */
    public boolean validarWebhookUrl(String webhookUrl, String nombreLocal) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("⚠️ No hay webhook configurado en el local {}", nombreLocal);
            return false;
        }
        return true;
    }

    /**
     * Envía un webhook de forma asíncrona a n8n.
     * No bloquea el flujo principal y maneja errores automáticamente.
     *
     * @param url URL del webhook
     * @param payload Datos a enviar (será convertido a JSON)
     * @param tipoWebhook Tipo de webhook para logging (ej: "NOTIFICACION_PEDIDO")
     * @param identificador ID del recurso relacionado (ej: pedidoId)
     */
    public void enviarAsync(String url, Map<String, Object> payload, String tipoWebhook, Object identificador) {
        log.info("📤 Enviando webhook {} para {} a: {}", tipoWebhook, identificador, url);

        webClient.post()
                .uri(url)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> 
                    log.info("✅ Webhook {} enviado exitosamente para {}. Response: {}", 
                            tipoWebhook, identificador, response))
                .doOnError(error -> 
                    log.error("❌ Error al enviar webhook {} para {}: {}", 
                            tipoWebhook, identificador, error.getMessage()))
                .onErrorResume(error -> {
                    // No propagamos el error para que no afecte el flujo principal
                    log.debug("Webhook {} falló pero no se propaga el error", tipoWebhook);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * Envía un webhook y espera la respuesta (síncrono).
     * Útil cuando necesitas procesar la respuesta del webhook.
     *
     * @param url URL del webhook
     * @param payload Datos a enviar
     * @param tipoWebhook Tipo de webhook para logging
     * @param identificador ID del recurso relacionado
     * @return Mono con la respuesta del webhook
     */
    public Mono<String> enviarSync(String url, Map<String, Object> payload, String tipoWebhook, Object identificador) {
        log.info("📤 Enviando webhook síncrono {} para {} a: {}", tipoWebhook, identificador, url);

        return webClient.post()
                .uri(url)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> 
                    log.info("✅ Webhook {} completado para {}", tipoWebhook, identificador))
                .doOnError(error -> 
                    log.error("❌ Error en webhook {} para {}: {}", 
                            tipoWebhook, identificador, error.getMessage()));
    }
}
