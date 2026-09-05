-- ============================================================================
-- V10 - Memoria de conversación del bot
--
-- Hasta acá el agente procesaba cada mensaje de forma aislada: el cliente tenía
-- que mandar el pedido completo en un solo mensaje porque el bot no recordaba
-- nada de lo anterior. Esta tabla guarda los turnos de texto de cada
-- conversación para poder reconstruir el contexto.
--
-- Solo se guardan los turnos de texto (lo que escribe el cliente y lo que
-- responde el bot). Las llamadas a herramientas NO se persisten: la API de
-- OpenAI exige que cada tool_call venga seguida de su resultado en el mismo
-- pedido, así que reconstruir secuencias parciales rompería el contrato.
-- ============================================================================

CREATE TABLE conversacion_mensajes (
    id              BIGSERIAL PRIMARY KEY,
    local_id        BIGINT       NOT NULL REFERENCES locales(id) ON DELETE CASCADE,
    -- Identificador del cliente en el canal (para WhatsApp, su número).
    remitente       VARCHAR(64)  NOT NULL,
    -- "user" = lo escribió el cliente; "assistant" = lo respondió el bot.
    rol             VARCHAR(16)  NOT NULL,
    contenido       TEXT         NOT NULL,
    creado_en       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_conversacion_rol CHECK (rol IN ('user', 'assistant'))
);

-- Se consulta siempre igual: los últimos N mensajes de un cliente en un local,
-- más recientes primero.
CREATE INDEX idx_conversacion_local_remitente
    ON conversacion_mensajes (local_id, remitente, creado_en DESC);

COMMENT ON TABLE conversacion_mensajes IS
    'Historial de texto de las conversaciones del bot, para dar contexto al agente de IA';
