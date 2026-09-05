package com.dioburger.channels;

import com.dioburger.model.entity.ConfiguracionLocal;

import java.util.Map;

/**
 * Contrato común que implementa cada canal de Meta (WhatsApp, comentarios de
 * Instagram/Facebook). Aísla al resto del sistema del formato particular de
 * cada webhook y de cada endpoint de Graph API.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
public interface ChannelAdapter {

    ChannelName getChannelName();

    /**
     * Convierte el payload crudo del webhook de Meta en un {@link NormalizedMessage}.
     * Devuelve {@code null} si el evento no trae un mensaje/comentario real
     * (ej. eventos "statuses" de WhatsApp, o reacciones/likes en Facebook).
     */
    NormalizedMessage parse(Map<String, Object> payload);

    /**
     * Envía la respuesta generada por el bot.
     *
     * @param to        WhatsApp: teléfono del destinatario. Comentarios: comment_id (se responde AL comentario, no al autor).
     * @param responseText texto a enviar
     * @param config    credenciales Meta del local (nunca hardcodeadas)
     */
    void send(String to, String responseText, ConfiguracionLocal config);

    /**
     * Resuelve el contexto del post al que pertenece un comentario (caption, permalink),
     * para dárselo al LLM. Solo lo implementan los adapters de comentarios.
     */
    default PostContext resolvePostContext(String postOrMediaId, ConfiguracionLocal config) {
        return null;
    }
}
