package com.dioburger.ai;

/**
 * Llamada a una tool decidida por el modelo (function calling de OpenAI).
 * {@code argumentsJson} viene tal cual lo manda el modelo (JSON como texto).
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
public record ToolCall(String id, String name, String argumentsJson) {
}
