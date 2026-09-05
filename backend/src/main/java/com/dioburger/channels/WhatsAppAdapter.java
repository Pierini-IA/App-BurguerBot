package com.dioburger.channels;

import com.dioburger.channels.support.JsonNav;
import com.dioburger.model.entity.ConfiguracionLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Adapter del canal WhatsApp (Meta Business API): parsea los webhooks de
 * mensajes entrantes y envía las respuestas del bot vía Graph API.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class WhatsAppAdapter implements ChannelAdapter {

    private final WebClient webClient;
    private final boolean dryRunSend;
    private final String graphApiVersion;

    public WhatsAppAdapter(
            WebClient webClient,
            @Value("${app.meta.dry-run-send:false}") boolean dryRunSend,
            @Value("${app.meta.graph-api-version:v20.0}") String graphApiVersion) {
        this.webClient = webClient;
        this.dryRunSend = dryRunSend;
        this.graphApiVersion = graphApiVersion;
    }

    @Override
    public ChannelName getChannelName() {
        return ChannelName.WHATSAPP;
    }

    @Override
    public NormalizedMessage parse(Map<String, Object> payload) {
        Map<String, Object> value = JsonNav.firstEntryChangeValue(payload, "messages");
        if (value == null) {
            return null;
        }

        List<Map<String, Object>> messages = JsonNav.asListOfMaps(value.get("messages"));
        if (messages == null || messages.isEmpty()) {
            // Eventos "statuses" (delivered/read) u otros sin mensaje real - se descartan.
            return null;
        }

        Map<String, Object> message = messages.get(0);
        String from = JsonNav.asString(message.get("from"));
        String messageId = JsonNav.asString(message.get("id"));
        String type = JsonNav.asString(message.get("type"));

        String text = null;
        List<String> imageMediaIds = List.of();

        if ("text".equals(type)) {
            Map<String, Object> textNode = JsonNav.asMap(message.get("text"));
            text = textNode != null ? JsonNav.asString(textNode.get("body")) : null;
        } else if ("image".equals(type)) {
            Map<String, Object> imageNode = JsonNav.asMap(message.get("image"));
            if (imageNode != null) {
                String mediaId = JsonNav.asString(imageNode.get("id"));
                imageMediaIds = mediaId != null ? List.of(mediaId) : List.of();
                text = JsonNav.asString(imageNode.get("caption"));
            }
        }

        String senderName = extractSenderName(value, from);

        // El envío usa config.getWaPhoneId() (credenciales del local), no el phone_number_id
        // del payload - por eso "mediaId" queda null acá (solo lo usan los adapters de comentarios).
        return new NormalizedMessage(
                ChannelName.WHATSAPP,
                from,
                senderName,
                text,
                imageMediaIds,
                messageId,
                null,
                null,
                Instant.now()
        );
    }

    private String extractSenderName(Map<String, Object> value, String from) {
        List<Map<String, Object>> contacts = JsonNav.asListOfMaps(value.get("contacts"));
        if (contacts == null || from == null) {
            return null;
        }
        for (Map<String, Object> contact : contacts) {
            if (from.equals(JsonNav.asString(contact.get("wa_id")))) {
                Map<String, Object> profile = JsonNav.asMap(contact.get("profile"));
                return profile != null ? JsonNav.asString(profile.get("name")) : null;
            }
        }
        return null;
    }

    @Override
    public void send(String to, String responseText, ConfiguracionLocal config) {
        if (config == null || config.getWaPhoneId() == null || config.getWaAccessToken() == null) {
            log.error("❌ Local sin credenciales de WhatsApp configuradas (waPhoneId/waAccessToken)");
            return;
        }

        if (dryRunSend) {
            log.info("[DRY_RUN] WhatsApp -> {}: {}", to, responseText);
            return;
        }

        String url = "https://graph.facebook.com/%s/%s/messages".formatted(graphApiVersion, config.getWaPhoneId());
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "text",
                "text", Map.of("body", responseText)
        );

        try {
            webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + config.getWaAccessToken())
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(10));
            log.info("✅ Mensaje de WhatsApp enviado a {}", to);
        } catch (Exception e) {
            log.error("❌ Error enviando mensaje de WhatsApp a {}: {}", to, e.getMessage());
        }
    }
}
