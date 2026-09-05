package com.dioburger.ai;

import com.dioburger.channels.NormalizedMessage;
import com.dioburger.model.dto.MenuCompletoDTO;
import com.dioburger.model.entity.Local;
import com.dioburger.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Motor de IA: conecta {@link RouterService} (intención), {@link PromptManagerService}
 * (system prompt), {@link BurgerToolsService} (tools) y {@link OpenAiClient} (chat
 * completions) en el loop de tool-calling que produce la respuesta final del bot.
 *
 * Análogo a {@code agent-engine.service.ts} de ben-decon, pero con un único
 * "vertical" (hamburguesería) en vez de un registro de verticals plugin.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentEngineService {

    private static final int MAX_TOOL_ITERATIONS = 6;
    private static final String FALLBACK_RESPONSE =
            "Perdón, tuve un problema para procesarlo. ¿Podés reformular tu mensaje?";

    private final RouterService routerService;
    private final PromptManagerService promptManagerService;
    private final BurgerToolsService burgerToolsService;
    private final OpenAiClient openAiClient;
    private final CatalogoService catalogoService;
    private final MemoriaConversacionService memoriaConversacionService;

    @Value("${app.openai.model:gpt-4.1-mini}")
    private String model;

    /**
     * Procesa un mensaje ya normalizado y devuelve el texto de respuesta a enviar por el canal.
     *
     * @param message     mensaje normalizado (WhatsApp o comentario)
     * @param local       local (tenant) al que pertenece la conversación
     * @param commentMode true si viene de un comentario público (tools de solo lectura)
     */
    public String processMessage(NormalizedMessage message, Local local, boolean commentMode) {
        if (!message.hasText()) {
            // Puede pasar si el mensaje era solo una imagen y Gemini no pudo describirla
            // (fallo de red, API key mal configurada, etc.) - no tiene sentido llamar a OpenAI sin texto.
            return "No pude leer bien tu mensaje. ¿Podés escribirlo en texto, por favor?";
        }

        RouterService.Intent intent = routerService.classify(message.text());

        MenuCompletoDTO menu = null;
        try {
            menu = catalogoService.obtenerCatalogoCompleto(local.getTelefono());
        } catch (Exception e) {
            log.warn("⚠️ No se pudo cargar el menú para el prompt del bot: {}", e.getMessage());
        }

        String systemPrompt = promptManagerService.buildSystemPrompt(local, menu, intent, commentMode, message.postCaption());
        List<ToolDefinition> tools = burgerToolsService.getDefinitions(commentMode);

        ToolExecutionContext context = new ToolExecutionContext(
                local,
                message.senderId(),
                message.senderName(),
                commentMode,
                message.externalMessageId()
        );

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));
        // El historial va entre el system y el mensaje nuevo, para que el modelo lea la
        // conversacion en orden. Los comentarios publicos son de una sola vuelta: ahi no
        // hay conversacion que recordar.
        if (!commentMode) {
            messages.addAll(memoriaConversacionService.recuperar(local, message.senderId()));
        }
        messages.add(ChatMessage.user(message.text()));

        for (int i = 0; i < MAX_TOOL_ITERATIONS; i++) {
            ChatCompletionResult result = openAiClient.chatCompletion(messages, tools, model);

            if (!result.hasToolCalls()) {
                String respuesta = result.content() != null ? result.content() : FALLBACK_RESPONSE;
                // Si OpenAI fallo, el texto es un aviso de error y no una respuesta real:
                // guardarlo ensuciaria el contexto de los mensajes siguientes.
                if (!commentMode && !"error".equals(result.finishReason())) {
                    memoriaConversacionService.guardarTurno(
                            local, message.senderId(), message.text(), respuesta);
                }
                return respuesta;
            }

            messages.add(ChatMessage.assistantToolCalls(result.toolCalls()));

            for (ToolCall toolCall : result.toolCalls()) {
                String toolResult = burgerToolsService.execute(toolCall.name(), toolCall.argumentsJson(), context);
                messages.add(ChatMessage.toolResult(toolCall.id(), toolResult));
            }
        }

        log.warn("⚠️ Se alcanzó el máximo de iteraciones de tool-calling ({}) para el mensaje de {}",
                MAX_TOOL_ITERATIONS, message.senderId());
        return FALLBACK_RESPONSE;
    }
}
