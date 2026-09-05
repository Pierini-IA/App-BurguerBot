package com.dioburger.ai;

import com.dioburger.model.dto.MenuCompletoDTO;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Arma el system prompt del bot: quién es, qué local es, su menú y horarios,
 * y cómo debe comportarse según el canal (mensaje directo vs comentario
 * público) e intención detectada.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
public class PromptManagerService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public String buildSystemPrompt(Local local, MenuCompletoDTO menu, RouterService.Intent intent,
                                     boolean commentMode, String postCaption) {
        StringBuilder sb = new StringBuilder();

        sb.append("Sos el asistente virtual de \"").append(local.getNombre())
                .append("\", una hamburguesería. Respondé siempre en español rioplatense, ")
                .append("de forma breve, cordial y directa, sin usar markdown (el texto se envía tal cual por WhatsApp o como comentario).\n\n");

        ConfiguracionLocal config = local.getConfiguracion();
        if (config != null) {
            sb.append("Horario de atención: ").append(config.getHoraApertura().format(HORA))
                    .append(" a ").append(config.getHoraCierre().format(HORA)).append(".\n");
            sb.append("Modalidades disponibles: ");
            if (Boolean.TRUE.equals(config.getPermiteDelivery())) sb.append("delivery ");
            if (Boolean.TRUE.equals(config.getPermiteTakeAway())) sb.append("retiro en el local ");
            if (Boolean.TRUE.equals(config.getPermiteReservas())) sb.append("reserva de mesas ");
            sb.append(".\n\n");
        }

        if (menu != null && menu.getCategorias() != null && !menu.getCategorias().isEmpty()) {
            sb.append("Menú actual (usá SIEMPRE estos IDs de producto al crear un pedido, nunca inventes IDs):\n");
            for (var categoria : menu.getCategorias()) {
                sb.append("- ").append(categoria.getNombre()).append(":\n");
                for (var producto : categoria.getProductos()) {
                    if (Boolean.FALSE.equals(producto.getDisponible())) {
                        continue;
                    }
                    sb.append("  · [id=").append(producto.getId()).append("] ")
                            .append(producto.getNombre()).append(" - $").append(producto.getPrecio())
                            .append("\n");
                }
            }
            sb.append("\n");
        }

        if (commentMode) {
            sb.append("IMPORTANTE: estás respondiendo un comentario público en una publicación de Instagram/Facebook, ")
                    .append("no un chat privado. NUNCA crees pedidos ni pidas datos personales acá. ")
                    .append("Respondé la consulta (menú, precios, horarios, si hacen delivery) en 1-2 frases ")
                    .append("e invitá a escribir por WhatsApp para pedir.\n");
            if (postCaption != null && !postCaption.isBlank()) {
                sb.append("La publicación que están comentando dice: \"").append(postCaption).append("\"\n");
            }
        } else {
            sb.append("Estás charlando por WhatsApp con un cliente. Ayudalo a armar su pedido usando las tools ")
                    .append("disponibles: consultá el menú/stock si hace falta, creá el pedido cuando tengas todos los datos ")
                    .append("(qué productos, cantidades, modalidad, dirección si es delivery, medio de pago), ")
                    .append("y confirmá el pedido con el cliente antes de crearlo. No inventes productos ni precios ")
                    .append("que no estén en el menú.\n");
            sb.append("Tenés el historial de esta conversación: si el cliente ya te dijo algo antes ")
                    .append("(qué quiere, la modalidad, el medio de pago), NO se lo vuelvas a preguntar. ")
                    .append("Preguntá solo lo que falte, de a poco, y cuando ya tengas todo pedile una ")
                    .append("confirmación corta antes de crear el pedido.\n");
            sb.append("Pista de intención detectada en el último mensaje: ").append(describeIntent(intent)).append(".\n");
        }

        return sb.toString();
    }

    private String describeIntent(RouterService.Intent intent) {
        return switch (intent) {
            case PEDIDO -> "el cliente quiere hacer o completar un pedido";
            case RESERVA -> "el cliente quiere reservar una mesa";
            case ESTADO_PEDIDO -> "el cliente pregunta por el estado de un pedido existente";
            case CONSULTA_GENERAL -> "consulta general (menú, horarios, ubicación, etc.)";
        };
    }
}
