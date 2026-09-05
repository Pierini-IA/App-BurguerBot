package com.dioburger.ai;

import com.dioburger.channels.support.JsonNav;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper fino sobre {@link WebClient} para la API de chat.completions de
 * OpenAI (chat + function/tool calling). No se usa el SDK oficial para no
 * agregar una dependencia nueva: el proyecto ya tiene WebClient disponible.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class OpenAiClient {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    private final WebClient webClient;
    private final String apiKey;

    public OpenAiClient(WebClient webClient, @Value("${app.openai.api-key:}") String apiKey) {
        this.webClient = webClient;
        this.apiKey = apiKey;
    }

    /**
     * Ejecuta una llamada de chat.completions con las tools disponibles.
     *
     * @param messages historial de la conversación (system/user/assistant/tool)
     * @param tools    tools que el modelo puede decidir invocar (puede ser vacía)
     * @param model    modelo de OpenAI a usar
     * @return contenido de texto y/o tool calls decididas por el modelo
     */
    public ChatCompletionResult chatCompletion(List<ChatMessage> messages, List<ToolDefinition> tools, String model) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("❌ OPENAI_API_KEY no configurada");
            return new ChatCompletionResult(
                    "Estamos con un problema técnico para procesar tu mensaje, por favor intentá de nuevo en unos minutos.",
                    null, "error");
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages.stream().map(this::toWireMessage).toList());
        if (tools != null && !tools.isEmpty()) {
            requestBody.put("tools", tools.stream().map(this::toWireTool).toList());
            requestBody.put("tool_choice", "auto");
        }

        try {
            Map<String, Object> response = webClient.post()
                    .uri(CHAT_COMPLETIONS_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(30));

            return parseResponse(response);
        } catch (Exception e) {
            log.error("❌ Error llamando a OpenAI chat.completions: {}", e.getMessage(), e);
            return new ChatCompletionResult(
                    "Perdón, tuve un problema para procesar tu mensaje. ¿Podés repetirlo?",
                    null, "error");
        }
    }

    private Map<String, Object> toWireMessage(ChatMessage message) {
        Map<String, Object> wire = new LinkedHashMap<>();
        wire.put("role", message.role());

        if ("assistant".equals(message.role()) && message.toolCalls() != null) {
            wire.put("content", message.content());
            wire.put("tool_calls", message.toolCalls().stream().map(tc -> Map.of(
                    "id", tc.id(),
                    "type", "function",
                    "function", Map.of("name", tc.name(), "arguments", tc.argumentsJson())
            )).toList());
        } else {
            wire.put("content", message.content());
        }

        if ("tool".equals(message.role())) {
            wire.put("tool_call_id", message.toolCallId());
        }

        return wire;
    }

    private Map<String, Object> toWireTool(ToolDefinition tool) {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", tool.name(),
                        "description", tool.description(),
                        "parameters", tool.parameters()
                )
        );
    }

    @SuppressWarnings("unchecked")
    private ChatCompletionResult parseResponse(Map<String, Object> response) {
        if (response == null) {
            return new ChatCompletionResult("No pude generar una respuesta, intentá de nuevo.", null, "error");
        }

        List<Map<String, Object>> choices = JsonNav.asListOfMaps(response.get("choices"));
        if (choices == null || choices.isEmpty()) {
            return new ChatCompletionResult("No pude generar una respuesta, intentá de nuevo.", null, "error");
        }

        Map<String, Object> choice = choices.get(0);
        String finishReason = JsonNav.asString(choice.get("finish_reason"));
        Map<String, Object> message = JsonNav.asMap(choice.get("message"));
        if (message == null) {
            return new ChatCompletionResult(null, null, finishReason);
        }

        String content = JsonNav.asString(message.get("content"));
        List<Map<String, Object>> rawToolCalls = JsonNav.asListOfMaps(message.get("tool_calls"));

        List<ToolCall> toolCalls = null;
        if (rawToolCalls != null && !rawToolCalls.isEmpty()) {
            toolCalls = new ArrayList<>();
            for (Map<String, Object> raw : rawToolCalls) {
                String id = JsonNav.asString(raw.get("id"));
                Map<String, Object> function = JsonNav.asMap(raw.get("function"));
                if (function == null) {
                    continue;
                }
                String name = JsonNav.asString(function.get("name"));
                String arguments = JsonNav.asString(function.get("arguments"));
                toolCalls.add(new ToolCall(id, name, arguments != null ? arguments : "{}"));
            }
        }

        return new ChatCompletionResult(content, toolCalls, finishReason);
    }
}
