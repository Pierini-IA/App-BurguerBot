package com.dioburger.ai;

import java.util.List;

/**
 * Mensaje del historial de chat, en el formato que espera la API de
 * chat.completions de OpenAI (roles "system", "user", "assistant", "tool").
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
public record ChatMessage(String role, String content, String toolCallId, List<ToolCall> toolCalls) {

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content, null, null);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content, null, null);
    }

    public static ChatMessage assistantText(String content) {
        return new ChatMessage("assistant", content, null, null);
    }

    public static ChatMessage assistantToolCalls(List<ToolCall> toolCalls) {
        return new ChatMessage("assistant", null, null, toolCalls);
    }

    public static ChatMessage toolResult(String toolCallId, String content) {
        return new ChatMessage("tool", content, toolCallId, null);
    }
}
