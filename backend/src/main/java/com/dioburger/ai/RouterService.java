package com.dioburger.ai;

import org.springframework.stereotype.Service;

/**
 * Clasifica la intención del mensaje entrante para darle una pista al
 * {@link PromptManagerService} sobre cómo encarar la respuesta. La
 * disponibilidad real de las tools la sigue decidiendo el canal
 * (mensaje vs comentario), no esta clasificación.
 *
 * A diferencia del router de ben-decon (keyword-first + fallback a un modelo
 * "mini"), acá alcanza con keywords: el dominio es acotado (pedidos de una
 * hamburguesería) y no vale la pena un round-trip extra a OpenAI solo para
 * clasificar.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
public class RouterService {

    public enum Intent {
        PEDIDO,
        RESERVA,
        ESTADO_PEDIDO,
        CONSULTA_GENERAL
    }

    private static final String[] KEYWORDS_ESTADO = {
            "estado de mi pedido", "como va mi pedido", "cómo va mi pedido", "donde esta mi pedido",
            "dónde está mi pedido", "cuanto falta", "cuánto falta", "ya sale", "listo mi pedido"
    };

    private static final String[] KEYWORDS_RESERVA = {
            "reserva", "reservar", "mesa para", "quiero una mesa"
    };

    private static final String[] KEYWORDS_PEDIDO = {
            "quiero", "pedido", "pedir", "dame", "mandame", "mándame", "envienme", "envíenme",
            "encargar", "agregar", "pasame", "pásame", "llevar", "para retirar", "delivery"
    };

    public Intent classify(String text) {
        if (text == null || text.isBlank()) {
            return Intent.CONSULTA_GENERAL;
        }

        String normalizado = text.toLowerCase();

        if (containsAny(normalizado, KEYWORDS_ESTADO)) {
            return Intent.ESTADO_PEDIDO;
        }
        if (containsAny(normalizado, KEYWORDS_RESERVA)) {
            return Intent.RESERVA;
        }
        if (containsAny(normalizado, KEYWORDS_PEDIDO)) {
            return Intent.PEDIDO;
        }
        return Intent.CONSULTA_GENERAL;
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
