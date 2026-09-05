package com.dioburger.ai;

import java.util.Map;

/**
 * Definición de una tool disponible para el modelo, en el formato de
 * "function calling" de OpenAI. {@code parameters} es un JSON-schema
 * simplificado ({@code type/properties/required}).
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
public record ToolDefinition(String name, String description, Map<String, Object> parameters) {
}
