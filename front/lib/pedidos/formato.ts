/**
 * Helpers de formato para Admin → Pedidos.
 */

import { EstadoPedido, Modalidad, type Pedido } from "@/types/api";

export const precioAR = new Intl.NumberFormat("es-AR", {
  style: "currency",
  currency: "ARS",
  maximumFractionDigits: 0,
});

/** Fecha en formato `yyyy-MM-dd` (horario local), para los query params del backend. */
export function fechaISO(d: Date): string {
  const tz = d.getTimezoneOffset() * 60_000;
  return new Date(d.getTime() - tz).toISOString().slice(0, 10);
}

export function hoy(): Date {
  return new Date();
}

export function hace(dias: number): Date {
  const d = new Date();
  d.setDate(d.getDate() - dias);
  return d;
}

/** Hora corta `HH:mm` de un ISO datetime. */
export function horaCorta(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "--:--";
  return d.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit" });
}

/** "hace 12 min" / "hace 2 h" desde un ISO datetime. */
export function tiempoRelativo(iso: string, ahoraMs: number = Date.now()): string {
  const min = Math.max(0, Math.round((ahoraMs - new Date(iso).getTime()) / 60_000));
  if (min < 1) return "recién";
  if (min < 60) return `hace ${min} min`;
  const h = Math.floor(min / 60);
  return `hace ${h} h`;
}

export const ETIQUETA_MODALIDAD: Record<Modalidad, string> = {
  [Modalidad.RETIRAR]: "Para retirar",
  [Modalidad.DELIVERY]: "Delivery",
};

export const ETIQUETA_ESTADO: Record<EstadoPedido, string> = {
  [EstadoPedido.PENDIENTE]: "Pendiente",
  [EstadoPedido.CONFIRMADO]: "Confirmado",
  [EstadoPedido.EN_PREPARACION]: "En preparación",
  [EstadoPedido.LISTO]: "Listo",
  [EstadoPedido.EN_CAMINO]: "En camino",
  [EstadoPedido.ENTREGADO]: "Entregado",
  [EstadoPedido.CANCELADO]: "Cancelado",
};

/** Total de ítems (sumando cantidades) de un pedido. */
export function cantidadItems(pedido: Pedido): number {
  return (pedido.items ?? []).reduce((acc, it) => acc + (it.cantidad ?? 0), 0);
}
