"use client";

/**
 * Hook para escuchar eventos de pedidos en tiempo real vía WebSocket (STOMP).
 *
 * Uso típico en el panel de cocina / pedidos:
 *
 *   const { conectado } = usePedidosSocket({
 *     telefonoLocal: user?.telefonoLocal,
 *     onNuevoPedido: (p) => agregarPedido(p),
 *     onActualizacion: (p) => mergePedido(p),
 *   });
 *
 * Los callbacks se leen siempre por referencia, así que no hace falta
 * memoizarlos: cambiarlos no reinicia la conexión.
 */

import { useEffect, useRef, useState } from "react";
import { crearPedidosClient } from "@/lib/ws/pedidosSocket";
import { defaults } from "@/lib/config/defaults";
import type { Pedido } from "@/types/api";

export interface UsePedidosSocketOpts {
  /** Teléfono del local. Si es falsy, el hook no conecta. */
  telefonoLocal?: string | null;
  /** Permite desactivar el socket sin desmontar el componente. */
  enabled?: boolean;
  onNuevoPedido?: (pedido: Pedido) => void;
  onActualizacion?: (pedido: Pedido) => void;
  onModificado?: (pedido: Pedido) => void;
  onCancelado?: (pedido: Pedido) => void;
  onRepartidorAsignado?: (pedido: Pedido) => void;
}

export function usePedidosSocket(opts: UsePedidosSocketOpts): { conectado: boolean } {
  const { telefonoLocal, enabled = true } = opts;
  const [conectado, setConectado] = useState(false);

  // Referencia viva a los callbacks para no reconectar cuando cambian.
  const optsRef = useRef(opts);
  useEffect(() => {
    optsRef.current = opts;
  });

  useEffect(() => {
    if (!enabled || !telefonoLocal) return;

    const token =
      typeof window !== "undefined" ? localStorage.getItem(defaults.storage.token) : null;

    const client = crearPedidosClient({
      telefonoLocal,
      token,
      onConexionCambio: setConectado,
      onEvento: (evento, payload) => {
        const pedido = payload as Pedido;
        const cb = optsRef.current;
        switch (evento) {
          case "nuevo":
            cb.onNuevoPedido?.(pedido);
            break;
          case "actualizacion":
            cb.onActualizacion?.(pedido);
            break;
          case "modificado":
            cb.onModificado?.(pedido);
            break;
          case "cancelado":
            cb.onCancelado?.(pedido);
            break;
          case "repartidor":
            cb.onRepartidorAsignado?.(pedido);
            break;
        }
      },
    });

    client.activate();

    return () => {
      void client.deactivate();
      setConectado(false);
    };
  }, [telefonoLocal, enabled]);

  return { conectado };
}
