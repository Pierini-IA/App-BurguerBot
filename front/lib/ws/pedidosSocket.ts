/**
 * Cliente WebSocket (STOMP sobre SockJS) para eventos de pedidos.
 *
 * El backend expone un endpoint STOMP en `/ws` (con fallback SockJS) y publica
 * en el broker simple `/topic`. Por cada local hay varios destinos:
 *
 *   /topic/pedidos/{telefonoLocal}                    -> pedido nuevo
 *   /topic/pedidos/{telefonoLocal}/actualizaciones    -> cambio de estado
 *   /topic/pedidos/{telefonoLocal}/modificados        -> pedido editado
 *   /topic/pedidos/{telefonoLocal}/cancelados         -> pedido cancelado
 *   /topic/pedidos/{telefonoLocal}/repartidor-asignado
 *
 * El broker simple de Spring hace match exacto de destino, así que hay que
 * suscribirse a cada uno por separado.
 */

import { Client, type IMessage, type IStompSocket } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { env } from "@/lib/config/env";
import { defaults } from "@/lib/config/defaults";

/** Tipo de evento de pedido recibido por WebSocket. */
export type PedidoEvento = "nuevo" | "actualizacion" | "modificado" | "cancelado" | "repartidor";

/** Sufijo de destino STOMP para cada tipo de evento. */
const SUFIJO_DESTINO: Record<PedidoEvento, string> = {
  nuevo: "",
  actualizacion: "/actualizaciones",
  modificado: "/modificados",
  cancelado: "/cancelados",
  repartidor: "/repartidor-asignado",
};

export interface CrearPedidosClientOpts {
  /** Teléfono del local (identificador multi-tenant). */
  telefonoLocal: string;
  /** JWT para autenticar la conexión STOMP (opcional). */
  token?: string | null;
  /** Se invoca por cada mensaje recibido, con el payload ya parseado. */
  onEvento: (evento: PedidoEvento, payload: unknown) => void;
  /** Se invoca cuando cambia el estado de conexión. */
  onConexionCambio?: (conectado: boolean) => void;
}

/**
 * Crea (sin activar) un cliente STOMP suscripto a todos los topics de pedidos
 * del local indicado. Llamar `.activate()` para conectar y `.deactivate()`
 * para cerrar.
 */
export function crearPedidosClient(opts: CrearPedidosClientOpts): Client {
  const { telefonoLocal, token, onEvento, onConexionCambio } = opts;

  const client = new Client({
    webSocketFactory: () => new SockJS(`${env.wsUrl}/ws`) as unknown as IStompSocket,
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    reconnectDelay: defaults.websocketReconnectDelay,
    heartbeatIncoming: 10_000,
    heartbeatOutgoing: 10_000,
  });

  client.onConnect = () => {
    onConexionCambio?.(true);
    (Object.keys(SUFIJO_DESTINO) as PedidoEvento[]).forEach((evento) => {
      const destino = `/topic/pedidos/${telefonoLocal}${SUFIJO_DESTINO[evento]}`;
      client.subscribe(destino, (mensaje: IMessage) => {
        let payload: unknown = mensaje.body;
        try {
          payload = JSON.parse(mensaje.body);
        } catch {
          /* si no es JSON, se deja el string crudo */
        }
        onEvento(evento, payload);
      });
    });
  };

  client.onWebSocketClose = () => onConexionCambio?.(false);
  client.onStompError = () => onConexionCambio?.(false);

  return client;
}
