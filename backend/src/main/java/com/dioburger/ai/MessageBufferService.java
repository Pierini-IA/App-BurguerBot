package com.dioburger.ai;

import com.dioburger.channels.NormalizedMessage;
import com.dioburger.model.entity.Local;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Agrupa mensajes seguidos de WhatsApp del mismo cliente (típico cuando alguien
 * escribe su pedido en varios mensajes cortos) antes de invocar al agente de IA,
 * para no generar una respuesta fragmentada por cada mensaje individual.
 *
 * Best-effort: no garantiza orden estricto bajo carrera extrema, pero es más
 * que suficiente para el volumen de un local de comida.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class MessageBufferService {

    private static final long BUFFER_WINDOW_MS = 12_000L;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "meta-buffer-scheduler");
        thread.setDaemon(true);
        return thread;
    });

    private final ConcurrentHashMap<String, BufferEntry> buffers = new ConcurrentHashMap<>();

    public void buffer(Local local, NormalizedMessage message, Consumer<NormalizedMessage> onFlush) {
        String key = local.getTelefono() + "::" + message.senderId();

        buffers.compute(key, (k, existing) -> {
            NormalizedMessage merged = (existing != null && existing.future.cancel(false))
                    ? existing.message.merge(message)
                    : message;
            ScheduledFuture<?> future = scheduler.schedule(() -> {
                buffers.remove(k);
                try {
                    onFlush.accept(merged);
                } catch (Exception e) {
                    log.error("❌ Error procesando mensaje agrupado de {}: {}", merged.senderId(), e.getMessage(), e);
                }
            }, BUFFER_WINDOW_MS, TimeUnit.MILLISECONDS);
            return new BufferEntry(merged, future);
        });
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private record BufferEntry(NormalizedMessage message, ScheduledFuture<?> future) {
    }
}
