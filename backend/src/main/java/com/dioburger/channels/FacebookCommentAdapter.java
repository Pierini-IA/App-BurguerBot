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
 * Adapter de comentarios de Facebook: parsea comentarios entrantes en posts
 * de la página (descartando reactions/likes) y responde vía Graph API.
 * Igual que Instagram, se responde AL comentario (comment_id), no al autor.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class FacebookCommentAdapter implements ChannelAdapter {

    private final WebClient webClient;
    private final boolean dryRunSend;
    private final String graphApiVersion;

    public FacebookCommentAdapter(
            WebClient webClient,
            @Value("${app.meta.dry-run-send:false}") boolean dryRunSend,
            @Value("${app.meta.graph-api-version:v20.0}") String graphApiVersion) {
        this.webClient = webClient;
        this.dryRunSend = dryRunSend;
        this.graphApiVersion = graphApiVersion;
    }

    @Override
    public ChannelName getChannelName() {
        return ChannelName.FACEBOOK_COMMENT;
    }

    @Override
    public NormalizedMessage parse(Map<String, Object> payload) {
        Map<String, Object> value = JsonNav.firstEntryChangeValue(payload, "feed");
        if (value == null || !"comment".equals(value.get("item"))) {
            // Descarta reactions/likes y otros eventos de "feed" que no son comentarios.
            return null;
        }

        String commentId = JsonNav.asString(value.get("comment_id"));
        String postId = JsonNav.asString(value.get("post_id"));
        String text = JsonNav.asString(value.get("message"));
        if (commentId == null || text == null) {
            return null;
        }

        Map<String, Object> from = JsonNav.asMap(value.get("from"));
        String senderId = from != null ? JsonNav.asString(from.get("id")) : null;
        String senderName = from != null ? JsonNav.asString(from.get("name")) : null;

        return new NormalizedMessage(
                ChannelName.FACEBOOK_COMMENT,
                senderId,
                senderName,
                text,
                List.of(),
                commentId,
                postId,
                null,
                Instant.now()
        );
    }

    @Override
    public void send(String to, String responseText, ConfiguracionLocal config) {
        if (config == null || config.getFbPageAccessToken() == null) {
            log.error("❌ Local sin credenciales de Facebook configuradas (fbPageAccessToken)");
            return;
        }

        if (dryRunSend) {
            log.info("[DRY_RUN] FB reply -> comment {}: {}", to, responseText);
            return;
        }

        String url = "https://graph.facebook.com/%s/%s/comments".formatted(graphApiVersion, to);

        try {
            webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + config.getFbPageAccessToken())
                    .bodyValue(Map.of("message", responseText))
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(10));
            log.info("✅ Respuesta a comentario de Facebook enviada (comment {})", to);
        } catch (Exception e) {
            log.error("❌ Error respondiendo comentario de Facebook {}: {}", to, e.getMessage());
        }
    }

    @Override
    public PostContext resolvePostContext(String postId, ConfiguracionLocal config) {
        if (postId == null || config == null || config.getFbPageAccessToken() == null) {
            return null;
        }

        String url = "https://graph.facebook.com/%s/%s?fields=message,permalink_url&access_token=%s"
                .formatted(graphApiVersion, postId, config.getFbPageAccessToken());

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
                    JsonNav.asString(response.get("message")),
                    JsonNav.asString(response.get("permalink_url"))
            );
        } catch (Exception e) {
            log.warn("⚠️ No se pudo resolver el contexto del post de Facebook {}: {}", postId, e.getMessage());
            return null;
        }
    }
}
