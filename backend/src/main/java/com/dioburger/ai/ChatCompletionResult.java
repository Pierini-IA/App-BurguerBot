package com.dioburger.ai;

import java.util.List;

/**
 * Resultado de una llamada a chat.completions: o bien texto final para el
 * usuario, o bien una lista de tools que el modelo quiere ejecutar antes de
 * seguir.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
public record ChatCompletionResult(String content, List<ToolCall> toolCalls, String finishReason) {

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
