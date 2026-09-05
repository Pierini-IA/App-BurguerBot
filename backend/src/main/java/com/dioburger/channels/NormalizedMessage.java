package com.dioburger.channels;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Representación canónica de un mensaje o comentario entrante, independiente
 * del canal de Meta que lo originó. Es la salida de {@link ChannelAdapter#parse}
 * y la entrada del motor de IA.
 *
 * @param channel            canal de origen
 * @param senderId           WhatsApp: teléfono del cliente. Comentarios: id del autor del comentario (informativo, no se usa para responder)
 * @param senderName         nombre visible del remitente (si Meta lo informó)
 * @param text                texto del mensaje o del comentario
 * @param imageMediaIds      media IDs de imágenes adjuntas (WhatsApp), vacío si no hay
 * @param externalMessageId  WhatsApp: id del mensaje. Comentarios: comment_id — es el destinatario real de {@link ChannelAdapter#send}
 * @param mediaId            Comentarios: id del post/media al que pertenece, usado para {@link ChannelAdapter#resolvePostContext}
 * @param postCaption        caption del post (solo comentarios, resuelto después del parseo inicial)
 * @param timestamp           momento en que se recibió
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
public record NormalizedMessage(
        ChannelName channel,
        String senderId,
        String senderName,
        String text,
        List<String> imageMediaIds,
        String externalMessageId,
        String mediaId,
        String postCaption,
        Instant timestamp
) {

    public boolean hasText() {
        return text != null && !text.isBlank();
    }

    public boolean hasImages() {
        return imageMediaIds != null && !imageMediaIds.isEmpty();
    }

    /**
     * Combina este mensaje con uno posterior del mismo remitente (usado por el buffer
     * de mensajes de WhatsApp para agrupar varios mensajes seguidos en uno solo).
     * Conserva las imágenes de ambos mensajes: si la imagen llega sin caption y el
     * texto llega en un mensaje aparte (en cualquier orden), no se pierde ninguna.
     */
    public NormalizedMessage merge(NormalizedMessage next) {
        String combinedText = hasText() && next.hasText()
                ? text + "\n" + next.text
                : (hasText() ? text : next.text);

        List<String> combinedImages = new ArrayList<>(imageMediaIds != null ? imageMediaIds : List.of());
        if (next.imageMediaIds != null) {
            combinedImages.addAll(next.imageMediaIds);
        }

        return new NormalizedMessage(channel, senderId, senderName, combinedText, combinedImages,
                next.externalMessageId, mediaId, postCaption, next.timestamp);
    }

    /** Devuelve una copia con el texto reemplazado (usado al inyectar la descripción que arma Gemini a partir de una imagen). */
    public NormalizedMessage withText(String newText) {
        return new NormalizedMessage(channel, senderId, senderName, newText, imageMediaIds,
                externalMessageId, mediaId, postCaption, timestamp);
    }

    /** Devuelve una copia con el caption del post resuelto (usado por los adapters de comentarios). */
    public NormalizedMessage withPostCaption(String caption) {
        return new NormalizedMessage(channel, senderId, senderName, text, imageMediaIds,
                externalMessageId, mediaId, caption, timestamp);
    }
}
