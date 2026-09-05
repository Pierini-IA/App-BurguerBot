package com.dioburger.ai;

import com.dioburger.model.entity.ConversacionMensaje;
import com.dioburger.model.entity.Local;
import com.dioburger.repository.ConversacionMensajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Le da memoria al bot: guarda los turnos de texto de cada conversación y los
 * devuelve para reconstruir el contexto en el siguiente mensaje.
 *
 * Sin esto el agente procesa cada mensaje aislado, y el cliente tiene que
 * mandar el pedido completo de una sola vez.
 *
 * Dos límites que evitan que el prompt crezca sin control: cuántos mensajes se
 * recuerdan y desde hace cuánto. Cada mensaje viejo que se manda cuesta tokens
 * y suma tiempo de respuesta.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemoriaConversacionService {

    public static final String ROL_USUARIO = "user";
    public static final String ROL_ASISTENTE = "assistant";

    private final ConversacionMensajeRepository repository;

    /** Cuántos turnos de texto se le recuerdan al modelo. */
    @Value("${app.memoria.max-mensajes:10}")
    private int maxMensajes;

    /** Antigüedad máxima: más viejo que esto se ignora. */
    @Value("${app.memoria.minutos-vigencia:120}")
    private int minutosVigencia;

    /**
     * Devuelve la conversación previa en orden cronológico, lista para insertar
     * en el prompt entre el system y el mensaje nuevo.
     *
     * Nunca tira excepción: si la memoria falla, el bot tiene que seguir
     * respondiendo aunque sea sin contexto.
     *
     * @param local     local dueño de la conversación
     * @param remitente identificador del cliente en el canal
     * @return mensajes del más viejo al más nuevo; vacío si no hay nada
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> recuperar(Local local, String remitente) {
        if (local == null || remitente == null || remitente.isBlank() || maxMensajes <= 0) {
            return List.of();
        }

        try {
            LocalDateTime desde = LocalDateTime.now().minusMinutes(minutosVigencia);
            List<ConversacionMensaje> recientes = repository.buscarUltimos(
                    local, remitente, desde, PageRequest.of(0, maxMensajes));

            if (recientes.isEmpty()) {
                return List.of();
            }

            // Vienen del más nuevo al más viejo (así el límite recorta los viejos);
            // el prompt los necesita en el orden en que se dijeron.
            List<ConversacionMensaje> enOrden = new ArrayList<>(recientes);
            Collections.reverse(enOrden);

            List<ChatMessage> historial = new ArrayList<>(enOrden.size());
            for (ConversacionMensaje m : enOrden) {
                historial.add(ROL_ASISTENTE.equals(m.getRol())
                        ? ChatMessage.assistantText(m.getContenido())
                        : ChatMessage.user(m.getContenido()));
            }

            log.debug("🧠 Memoria: {} mensajes previos de {}", historial.size(), remitente);
            return historial;

        } catch (Exception e) {
            log.warn("⚠️ No se pudo recuperar la memoria de {}: {}", remitente, e.getMessage());
            return List.of();
        }
    }

    /**
     * Guarda el turno que acaba de pasar: lo que escribió el cliente y lo que
     * contestó el bot.
     *
     * Como {@link #recuperar}, nunca tira: que falle el guardado no puede
     * impedir que el cliente reciba su respuesta.
     *
     * @param local     local dueño de la conversación
     * @param remitente identificador del cliente en el canal
     * @param mensajeCliente lo que escribió el cliente
     * @param respuestaBot   lo que respondió el bot
     */
    @Transactional
    public void guardarTurno(Local local, String remitente, String mensajeCliente, String respuestaBot) {
        if (local == null || remitente == null || remitente.isBlank()) {
            return;
        }

        try {
            if (mensajeCliente != null && !mensajeCliente.isBlank()) {
                repository.save(ConversacionMensaje.builder()
                        .local(local)
                        .remitente(remitente)
                        .rol(ROL_USUARIO)
                        .contenido(mensajeCliente)
                        .build());
            }
            if (respuestaBot != null && !respuestaBot.isBlank()) {
                repository.save(ConversacionMensaje.builder()
                        .local(local)
                        .remitente(remitente)
                        .rol(ROL_ASISTENTE)
                        .contenido(respuestaBot)
                        .build());
            }
        } catch (Exception e) {
            log.warn("⚠️ No se pudo guardar la memoria de {}: {}", remitente, e.getMessage());
        }
    }
}
