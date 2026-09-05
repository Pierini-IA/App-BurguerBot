package com.dioburger.ai;

import com.dioburger.model.entity.Local;

/**
 * Contexto fijo con el que se ejecuta cada tool, separado de los argumentos
 * que decide el modelo. El teléfono del cliente y el local NUNCA los decide
 * el LLM: vienen del canal (WhatsApp) o de la sesión, para que el modelo no
 * pueda "inventar" a nombre de quién se hace un pedido.
 *
 * @param requestSeed id único del mensaje/evento entrante (ej. wamid de WhatsApp),
 *                    usado para derivar requestIds idempotentes en las tools que
 *                    crean/modifican pedidos y reservas
 */
public record ToolExecutionContext(Local local, String telefonoCliente, String nombreCliente,
                                    boolean commentMode, String requestSeed) {
}
