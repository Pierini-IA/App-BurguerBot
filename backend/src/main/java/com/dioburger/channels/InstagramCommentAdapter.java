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
 * Adapter de comentarios de Instagram: parsea comentarios entrantes en posts
 * y responde vía Graph API. A diferencia de WhatsApp, el destinatario de
 * {@link #send} es el comment_id (se responde AL comentario, no al autor).
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class InstagramCommentAdapter implements ChannelAdapter {

    private final WebClient webClient;
    private final boolean dryRunSend;
    private final String graphApiVersion;

    public InstagramCommentAdapter(
            WebClient webClient,
            @Value("${app.meta.dry-run-send:false}") boolean dryRunSend,
            @Value("${app.meta.graph-api-version:v20.0}") String graphApiVersion) {
        this.webClient = webClient;
        this.dryRunSend = dryRunSend;
        this.graphApiVersion = graphApiVersion;
    }

    @Override
    public ChannelName getChannelName() {
        return ChannelName.INSTAGRAM_COMMENT;
    }

    @Override
    public NormalizedMessage parse(Map<String, Object> payload) {
        Map<String, Object> value = JsonNav.firstEntryChangeValue(payload, "comments");
        if (value == null) {
            return null;
        }

        String commentId = JsonNav.asString(value.get("id"));
        String text = JsonNav.asString(value.get("text"));
        if (commentId == null || text == null) {
            return null;
        }

        Map<String, Object> from = JsonNav.asMap(value.get("from"));
        String senderId = from != null ? JsonNav.asString(from.get("id")) : null;
        String senderName = from != null ? JsonNav.asString(from.get("username")) : null;

        Map<String, Object> media = JsonNav.asMap(value.get("media"));
        String mediaId = media != null ? JsonNav.asString(media.get("id")) : null;

        return new NormalizedMessage(
                ChannelName.INSTAGRAM_COMMENT,
                senderId,
                senderName,
                text,
                List.of(),
                commentId,
                mediaId,
                null,
                Instant.now()
        );
    }

    @Override
    public void send(String to, String responseText, ConfiguracionLocal config) {
        if (config == null || config.getIgToken() == null) {
            log.error("❌ Local sin credenciales de Instagram configuradas (igToken)");
            return;
        }

        if (dryRunSend) {
            log.info("[DRY_RUN] IG reply -> comment {}: {}", to, responseText);
            return;
        }

        String url = "https://graph.facebook.com/%s/%s/replies".formatted(graphApiVersion, to);

        try {
            webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + config.getIgToken())
                    .bodyValue(Map.of("message", responseText))
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(10));
            log.info("✅ Respuesta a comentario de Instagram enviada (comment {})", to);
        } catch (Exception e) {
            log.error("❌ Error respondiendo comentario de Instagram {}: {}", to, e.getMessage());
        }
    }

    @Override
    public PostContext resolvePostContext(String mediaId, ConfiguracionLocal config) {
        if (mediaId == null || config == null || config.getIgToken() == null) {
            return null;
        }

        String url = "https://graph.facebook.com/%s/%s?fields=caption,permalink&access_token=%s"
                .formatted(graphApiVersion, mediaId, config.getIgToken());

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(10));

            if (response == null) {
                return null;
            }
            return new PostContext(
                    JsonNav.asString(response.get("caption")),
                    JsonNav.asString(response.get("permalink"))
            );
        } catch (Exception e) {
            log.warn("⚠️ No se pudo resolver el contexto del post de Instagram {}: {}", mediaId, e.getMessage());
            return null;
        }
    }
}
