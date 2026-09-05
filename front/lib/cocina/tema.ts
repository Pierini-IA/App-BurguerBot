/**
 * Paleta y tipografía del panel de cocina.
 *
 * El panel no usa el look claro del resto de la app: es una pantalla operativa
 * montada en una tablet a ~1,5 m, pensada para leerse de un vistazo durante el
 * servicio. Fondo "plancha curada" (marrón espresso), pedidos como chits de
 * papel crema, y el naranja de marca reservado solo para la señal de "ahora".
 */

export const cocinaTema = {
  /** Fondo general: espresso casi negro, cálido (no azulado). */
  fondo: "#1F1A17",
  /** Barra de pase y canaleta del riel. */
  fondoRelieve: "#2A2320",
  /** Borde/separadores sobre el fondo oscuro. */
  linea: "#3A322D",
  /** Papel del chit. */
  chit: "#F7F1E6",
  /** Tinta sobre el papel. */
  tinta: "#221E1B",
  /** Texto secundario sobre el papel. */
  tintaTenue: "#6E645A",
  /** Texto secundario sobre el fondo oscuro. */
  sobreFondoTenue: "#9B8F82",
  /** Naranja de marca: única señal de "arrancá / apurá". */
  ember: "#FF6B35",
  emberProfundo: "#C0402A",
  /** Verde sobrio para "listo / entregado" (no el verde ácido de KDS). */
  hecho: "#5C7A5A",
} as const;

/** Familia condensada tipo cartelería (cargada en `app/layout.tsx`). */
export const FUENTE_CONDENSADA = 'var(--font-barlow-condensed), "Arial Narrow", "Roboto Condensed", sans-serif';
