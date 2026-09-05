package com.dioburger.ai;

import com.dioburger.channels.support.JsonNav;
import com.dioburger.model.entity.ConfiguracionLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * Resuelve una imagen recibida por WhatsApp (media ID) a su descripción de
 * pedido en texto plano, vía Graph API (para bajar la imagen) + Gemini (para
 * interpretarla). El texto resultante se inyecta como si fuera el mensaje del
 * cliente y sigue el mismo pipeline de tools que un mensaje de texto normal.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class GeminiImageAnalysisService {

    private static final String PROMPT = """
            Esta imagen fue enviada por un cliente al WhatsApp de una hamburguesería para hacer un pedido.
            Puede ser una foto de una nota escrita a mano, una captura de un chat, o una foto del menú con marcas.
            Describí en texto plano y en español qué productos y cantidades pidió el cliente, con la mayor precisión posible.
            Si hay alguna aclaración (sin cebolla, para retirar, etc.) inclínala también.
            Si no podés identificar ningún pedido en la imagen, respondé exactamente: "No se pudo identificar un pedido en la imagen".
            """;

    private final WebClient webClient;
    private final GeminiClient geminiClient;
    private final String graphApiVersion;

    public GeminiImageAnalysisService(
            WebClient webClient,
            GeminiClient geminiClient,
            @Value("${app.meta.graph-api-version:v20.0}") String graphApiVersion) {
        this.webClient = webClient;
        this.geminiClient = geminiClient;
        this.graphApiVersion = graphApiVersion;
    }

    /**
     * Baja la imagen de WhatsApp identificada por {@code mediaId} y le pide a
     * Gemini que describa el pedido que contiene.
     *
     * @return descripción en texto plano, o {@code null} si algo falló (nunca lanza)
     */
    public String describirImagenDePedido(String mediaId, ConfiguracionLocal config) {
        if (mediaId == null || config == null || config.getWaAccessToken() == null) {
            return null;
        }

        try {
            MediaInfo mediaInfo = resolverUrlDeMedia(mediaId, config.getWaAccessToken());
            if (mediaInfo == null) {
                return null;
            }

            byte[] bytes = descargarMedia(mediaInfo.url(), config.getWaAccessToken());
            if (bytes == null) {
                return null;
            }

            String base64 = Base64.getEncoder().encodeToString(bytes);
            return geminiClient.describeImage(base64, mediaInfo.mimeType(), PROMPT);
        } catch (Exception e) {
            log.error("❌ Error analizando imagen de pedido (mediaId={}): {}", mediaId, e.getMessage(), e);
            return null;
        }
    }

    private MediaInfo resolverUrlDeMedia(String mediaId, String waAccessToken) {
        String url = "https://graph.facebook.com/%s/%s".formatted(graphApiVersion, mediaId);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + waAccessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(15));

        if (response == null) {
            return null;
        }

        String mediaUrl = JsonNav.asString(response.get("url"));
        String mimeType = JsonNav.asString(response.get("mime_type"));
        if (mediaUrl == null) {
            return null;
        }
        return new MediaInfo(mediaUrl, mimeType != null ? mimeType : "image/jpeg");
    }

    private byte[] descargarMedia(String mediaUrl, String waAccessToken) {
        return webClient.get()
                .uri(mediaUrl)
                .header("Authorization", "Bearer " + waAccessToken)
                .retrieve()
                .bodyToMono(byte[].class)
                .block(Duration.ofSeconds(20));
    }

    private record MediaInfo(String url, String mimeType) {
    }
}
