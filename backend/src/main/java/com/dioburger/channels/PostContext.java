package com.dioburger.channels;

/**
 * Contexto de un post de Instagram/Facebook al que pertenece un comentario,
 * resuelto vía Graph API para darle contexto al LLM sobre qué se está comentando.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
public record PostContext(String caption, String permalink) {
}
