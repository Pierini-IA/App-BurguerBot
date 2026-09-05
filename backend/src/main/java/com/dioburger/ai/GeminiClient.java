package com.dioburger.ai;

import com.dioburger.channels.support.JsonNav;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Wrapper fino sobre {@link WebClient} para el endpoint multimodal de Gemini
 * ({@code generateContent}), usado para interpretar imágenes que llegan por
 * WhatsApp (fotos de un pedido escrito a mano, capturas de un chat, etc.).
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class GeminiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            WebClient webClient,
            @Value("${app.gemini.api-key:}") String apiKey,
            @Value("${app.gemini.model:gemini-2.5-flash}") String model) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Le pide a Gemini que describa en texto plano el contenido de una imagen.
     *
     * @param imageBase64 imagen codificada en base64
     * @param mimeType    tipo MIME de la imagen (ej. "image/jpeg")
     * @param promptText  instrucción de qué buscar en la imagen
     * @return texto descriptivo, o {@code null} si falló la llamada
     */
    public String describeImage(String imageBase64, String mimeType, String promptText) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("❌ GOOGLE_AI_API_KEY no configurada");
            return null;
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s"
                .formatted(model, apiKey);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("text", promptText),
                                Map.of("inline_data", Map.of("mime_type", mimeType, "data", imageBase64))
                        )
                ))
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(30));

            return extractText(response);
        } catch (Exception e) {
            log.error("❌ Error llamando a Gemini generateContent: {}", e.getMessage(), e);
            return null;
        }
    }

    private String extractText(Map<String, Object> response) {
        if (response == null) {
            return null;
        }
        List<Map<String, Object>> candidates = JsonNav.asListOfMaps(response.get("candidates"));
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        Map<String, Object> content = JsonNav.asMap(candidates.get(0).get("content"));
        if (content == null) {
            return null;
        }
        List<Map<String, Object>> parts = JsonNav.asListOfMaps(content.get("parts"));
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        return JsonNav.asString(parts.get(0).get("text"));
    }
}
