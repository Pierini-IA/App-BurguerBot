package com.dioburger.channels;

/**
 * Canales de Meta soportados por la integración directa (sin n8n).
 * Cada valor tiene un "slug" que es el que aparece en la URL del webhook
 * ({@code /api/webhooks/meta/{slug}/{telefonoLocal}}).
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
public enum ChannelName {

    WHATSAPP("whatsapp"),
    INSTAGRAM_COMMENT("instagram-comment"),
    FACEBOOK_COMMENT("facebook-comment");

    private final String slug;

    ChannelName(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    /**
     * Resuelve un canal a partir del slug recibido en la URL del webhook.
     *
     * @param slug segmento de URL (ej. "whatsapp")
     * @return canal correspondiente
     * @throws IllegalArgumentException si el slug no corresponde a ningún canal soportado
     */
    public static ChannelName fromSlug(String slug) {
        for (ChannelName value : values()) {
            if (value.slug.equals(slug)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Canal de Meta desconocido: " + slug);
    }
}
