/**
 * Acción de "bump" de un chit: qué hace el próximo toque según el estado.
 */

import { cocinaApi } from "@/lib/api/cocina";
import { EstadoPedido, Modalidad, type Pedido } from "@/types/api";

export type AccionTicket = "iniciar" | "listo" | "en-camino" | "entregar";

/** Estados que siguen "vivos" en el riel de cocina. */
export const ESTADOS_ACTIVOS: ReadonlySet<EstadoPedido> = new Set([
  EstadoPedido.PENDIENTE,
  EstadoPedido.CONFIRMADO,
  EstadoPedido.EN_PREPARACION,
  EstadoPedido.LISTO,
]);

/**
 * Devuelve la acción del bump bar para el estado actual del pedido,
 * o `null` si el chit ya no tiene próximo paso en cocina.
 */
export function accionParaEstado(pedido: Pedido): { accion: AccionTicket; label: string } | null {
  switch (pedido.estado) {
    case EstadoPedido.PENDIENTE:
    case EstadoPedido.CONFIRMADO:
      return { accion: "iniciar", label: "Empezar" };
    case EstadoPedido.EN_PREPARACION:
      return { accion: "listo", label: "Marcar listo" };
    case EstadoPedido.LISTO:
      return pedido.modalidad === Modalidad.DELIVERY
        ? { accion: "en-camino", label: "Marcar en camino" }
        : { accion: "entregar", label: "Entregar" };
    case EstadoPedido.EN_CAMINO:
      return { accion: "entregar", label: "Entregar" };
    default:
      return null;
  }
}

/** Llama al endpoint de cocina correspondiente y devuelve el pedido actualizado. */
export function ejecutarAccion(id: number, accion: AccionTicket): Promise<Pedido> {
  switch (accion) {
    case "iniciar":
      return cocinaApi.iniciarPreparacion(id);
    case "listo":
      return cocinaApi.marcarListo(id);
    case "en-camino":
      return cocinaApi.marcarEnCamino(id);
    case "entregar":
      return cocinaApi.entregarPedido(id);
  }
}
