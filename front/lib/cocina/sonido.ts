/**
 * Aviso sonoro para pedidos nuevos en cocina.
 *
 * Se genera con WebAudio (dos tonos cortos, tipo campana de pase), así no hace
 * falta cargar ningún archivo de audio. Es un no-op si el navegador no lo permite.
 */

type AudioContextCtor = typeof AudioContext;

let ctx: AudioContext | null = null;

function obtenerContexto(): AudioContext | null {
  if (typeof window === "undefined") return null;
  const Ctor: AudioContextCtor | undefined =
    window.AudioContext ??
    (window as unknown as { webkitAudioContext?: AudioContextCtor }).webkitAudioContext;
  if (!Ctor) return null;
  if (!ctx) ctx = new Ctor();
  return ctx;
}

/**
 * Reproduce el aviso de "pedido nuevo".
 * Llamar desde un handler de evento para que el navegador lo permita.
 */
export function sonarPedidoNuevo(): void {
  try {
    const audio = obtenerContexto();
    if (!audio) return;
    if (audio.state === "suspended") void audio.resume();

    const inicio = audio.currentTime;
    const tonos: Array<[frecuencia: number, retraso: number]> = [
      [880, 0],
      [1174.7, 0.13],
    ];

    for (const [frecuencia, retraso] of tonos) {
      const osc = audio.createOscillator();
      const gain = audio.createGain();
      osc.type = "triangle";
      osc.frequency.value = frecuencia;
      const t0 = inicio + retraso;
      gain.gain.setValueAtTime(0.0001, t0);
      gain.gain.exponentialRampToValueAtTime(0.22, t0 + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.0001, t0 + 0.35);
      osc.connect(gain).connect(audio.destination);
      osc.start(t0);
      osc.stop(t0 + 0.4);
    }
  } catch {
    /* audio no disponible: seguimos sin sonido */
  }
}
