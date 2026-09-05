/**
 * Helpers de tiempo para el panel de cocina.
 * Todo se calcula contra un `ahoraMs` que pasa el board (un solo tick por segundo).
 */

/** Minutos transcurridos desde una fecha ISO. */
export function minutosDesde(iso: string, ahoraMs: number): number {
  const t = new Date(iso).getTime();
  if (Number.isNaN(t)) return 0;
  return Math.max(0, (ahoraMs - t) / 60_000);
}

/** Tiempo transcurrido en formato de chit: `mm:ss`, o `Nh mm` si pasa la hora. */
export function tiempoTranscurrido(iso: string, ahoraMs: number): string {
  const t = new Date(iso).getTime();
  if (Number.isNaN(t)) return "--:--";
  const totalSeg = Math.floor(Math.max(0, (ahoraMs - t) / 1000));
  const min = Math.floor(totalSeg / 60);
  const seg = totalSeg % 60;
  if (min >= 60) {
    const h = Math.floor(min / 60);
    return `${h}h ${String(min % 60).padStart(2, "0")}`;
  }
  return `${String(min).padStart(2, "0")}:${String(seg).padStart(2, "0")}`;
}

export type NivelDemora = "fresco" | "medio" | "demora" | "critico";

/**
 * Nivel de demora de un pedido según sus minutos en cola.
 * `alertaMin` es el umbral en el que se considera "crítico" (default 12').
 */
export function nivelPorEdad(minutos: number, alertaMin = 12): NivelDemora {
  if (minutos < alertaMin * 0.34) return "fresco";
  if (minutos < alertaMin * 0.67) return "medio";
  if (minutos < alertaMin) return "demora";
  return "critico";
}

/** Rampa monocroma cálida: recién llegado → apurado. Sin verde de KDS genérico. */
export const COLOR_NIVEL: Record<NivelDemora, string> = {
  fresco: "#8A7F6E", // taupe cálido: acaba de entrar
  medio: "#E8A13C", // ámbar: en marcha
  demora: "#FF6B35", // ember (naranja de marca): apurá
  critico: "#C0402A", // ladrillo quemado: se está demorando
};
