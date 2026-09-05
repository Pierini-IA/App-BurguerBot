package com.dioburger.controller;

import com.dioburger.ai.AgentEngineService;
import com.dioburger.ai.GeminiImageAnalysisService;
import com.dioburger.ai.MessageBufferService;
import com.dioburger.channels.ChannelAdapter;
import com.dioburger.channels.ChannelName;
import com.dioburger.channels.ChannelRegistryService;
import com.dioburger.channels.NormalizedMessage;
import com.dioburger.channels.PostContext;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.model.enums.Feature;
import com.dioburger.service.LocalService;
import com.dioburger.service.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Webhooks de Meta (WhatsApp, comentarios de Instagram/Facebook) — reemplaza
 * el rol que antes cumplía n8n: recibe el evento directo de Meta, lo normaliza
 * vía el {@link ChannelAdapter} correspondiente y dispara el motor de IA.
 *
 * Endpoint: /api/webhooks/meta/{canal}/{telefonoLocal}
 * canal = "whatsapp" | "instagram-comment" | "facebook-comment"
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/webhooks/meta")
@Slf4j
public class MetaWebhookController {

    private final LocalService localService;
    private final ChannelRegistryService channelRegistry;
    private final AgentEngineService agentEngineService;
    private final MessageBufferService messageBufferService;
    private final PlanService planService;
    private final GeminiImageAnalysisService geminiImageAnalysisService;
    private final Executor metaTaskExecutor;

    @Value("${app.meta.webhook-verify-token:}")
    private String verifyToken;

    // Constructor explícito (no @RequiredArgsConstructor): Spring WebSocket ya registra sus propios
    // beans Executor internos (clientInboundChannelExecutor, etc.), así que hace falta @Qualifier
    // para no chocar contra ellos al resolver metaTaskExecutor por tipo.
    public MetaWebhookController(
            LocalService localService,
            ChannelRegistryService channelRegistry,
            AgentEngineService agentEngineService,
            MessageBufferService messageBufferService,
            PlanService planService,
            GeminiImageAnalysisService geminiImageAnalysisService,
            @Qualifier("metaTaskExecutor") Executor metaTaskExecutor) {
        this.localService = localService;
        this.channelRegistry = channelRegistry;
        this.agentEngineService = agentEngineService;
        this.messageBufferService = messageBufferService;
        this.planService = planService;
        this.geminiImageAnalysisService = geminiImageAnalysisService;
        this.metaTaskExecutor = metaTaskExecutor;
    }

    /**
     * Handshake de verificación que Meta hace una sola vez al configurar el webhook.
     */
    @GetMapping("/{canal}/{telefonoLocal}")
    public ResponseEntity<String> verificar(
            @PathVariable String canal,
            @PathVariable String telefonoLocal,
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && verifyToken != null && verifyToken.equals(token)) {
            log.info("✅ Webhook de Meta verificado (canal={}, local={})", canal, telefonoLocal);
            return ResponseEntity.ok(challenge);
        }

        log.warn("❌ Verificación de webhook de Meta rechazada (canal={}, local={})", canal, telefonoLocal);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Recepción de mensajes/comentarios. Responde 200 de inmediato y procesa
     * en segundo plano (Meta reintenta si no recibe ack rápido).
     */
    @PostMapping("/{canal}/{telefonoLocal}")
    public ResponseEntity<Void> recibir(
            @PathVariable String canal,
            @PathVariable String telefonoLocal,
            @RequestBody(required = false) Map<String, Object> payload) {

        if (payload == null) {
            return ResponseEntity.ok().build();
        }

        ChannelName channelName;
        try {
            channelName = ChannelName.fromSlug(canal);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Canal de Meta desconocido en el webhook: {}", canal);
            return ResponseEntity.ok().build();
        }

        // Fetch join de la configuración: el procesamiento sigue en un hilo en segundo plano
        // (buffer de WhatsApp o executor de comentarios) donde ya no hay sesión de Hibernate.
        Local local = localService.buscarPorTelefonoConConfiguracion(telefonoLocal);

        boolean isComment = channelName != ChannelName.WHATSAPP;
        planService.validarAccesoFeature(local, isComment ? Feature.BOT_COMMENTS_META : Feature.BOT_WHATSAPP);

        ChannelAdapter adapter = channelRegistry.getAdapter(channelName);
        NormalizedMessage normalized = adapter.parse(payload);

        if (normalized == null || (!normalized.hasText() && !normalized.hasImages())) {
            // Eventos sin contenido útil (statuses, reactions, etc.) - se descartan sin procesar.
            return ResponseEntity.ok().build();
        }

        if (isComment) {
            procesarEnSegundoPlano(() -> {
                NormalizedMessage conContexto = enriquecerConPostContext(adapter, normalized, local);
                responder(adapter, local, conContexto, true);
            });
        } else {
            messageBufferService.buffer(local, normalized, buffered -> responder(adapter, local, buffered, false));
        }

        return ResponseEntity.ok().build();
    }

    private NormalizedMessage enriquecerConPostContext(ChannelAdapter adapter, NormalizedMessage normalized, Local local) {
        PostContext postContext = adapter.resolvePostContext(normalized.mediaId(), local.getConfiguracion());
        return postContext != null ? normalized.withPostCaption(postContext.caption()) : normalized;
    }

    private void responder(ChannelAdapter adapter, Local local, NormalizedMessage message, boolean commentMode) {
        try {
            NormalizedMessage enriquecido = commentMode ? message : enriquecerConDescripcionDeImagen(message, local);
            String respuesta = agentEngineService.processMessage(enriquecido, local, commentMode);
            String destinatario = commentMode ? message.externalMessageId() : message.senderId();
            adapter.send(destinatario, respuesta, local.getConfiguracion());
        } catch (Exception e) {
            log.error("❌ Error procesando mensaje de {} para local {}: {}",
                    message.channel(), local.getTelefono(), e.getMessage(), e);
        }
    }

    /**
     * Si el mensaje de WhatsApp trae imágenes (foto de un pedido escrito a mano,
     * captura de un chat, etc.), les pide a Gemini que las describa y agrega esa
     * descripción al texto del mensaje antes de pasarlo por el motor de IA.
     */
    private NormalizedMessage enriquecerConDescripcionDeImagen(NormalizedMessage message, Local local) {
        if (!message.hasImages()) {
            return message;
        }

        ConfiguracionLocal config = local.getConfiguracion();
        StringBuilder descripciones = new StringBuilder();

        for (String mediaId : message.imageMediaIds()) {
            String descripcion = geminiImageAnalysisService.describirImagenDePedido(mediaId, config);
            if (descripcion != null && !descripcion.isBlank()) {
                descripciones.append(descripcion).append("\n");
            }
        }

        if (descripciones.isEmpty()) {
            return message;
        }

        String textoFinal = message.hasText() ? message.text() + "\n" + descripciones : descripciones.toString();
        return message.withText(textoFinal);
    }

    private void procesarEnSegundoPlano(Runnable task) {
        metaTaskExecutor.execute(task);
    }
}
