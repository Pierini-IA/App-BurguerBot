"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Box, Button, CircularProgress, IconButton } from "@mui/material";
import { keyframes } from "@mui/system";
import {
  VolumeUp as VolumeUpIcon,
  VolumeOff as VolumeOffIcon,
  Logout as LogoutIcon,
} from "@mui/icons-material";
import { useRouter } from "next/navigation";
import { cocinaApi } from "@/lib/api/cocina";
import { getErrorMessage } from "@/lib/api/axios";
import { useAuth } from "@/lib/hooks/useAuth";
import { useLocal } from "@/lib/context/LocalContext";
import { usePedidosSocket } from "@/lib/hooks/usePedidosSocket";
import { useSnackbar } from "@/lib/hooks/useSnackbar";
import { FeedbackSnackbar } from "@/components/shared";
import { EstadoPedido, type Pedido } from "@/types/api";
import { cocinaTema as T, FUENTE_CONDENSADA } from "@/lib/cocina/tema";
import { tiempoTranscurrido } from "@/lib/cocina/tiempo";
import { sonarPedidoNuevo } from "@/lib/cocina/sonido";
import { ESTADOS_ACTIVOS, ejecutarAccion, type AccionTicket } from "@/lib/cocina/acciones";
import { TicketPedido } from "./TicketPedido";

const CLAVE_SONIDO = "cocina_sonido";
const EN_COLA = new Set<EstadoPedido>([
  EstadoPedido.PENDIENTE,
  EstadoPedido.CONFIRMADO,
  EstadoPedido.EN_PREPARACION,
]);

const pulso = keyframes`
  0%, 100% { opacity: 1; }
  50%      { opacity: 0.35; }
`;

function porHora(a: Pedido, b: Pedido): number {
  return new Date(a.horaPedido).getTime() - new Date(b.horaPedido).getTime();
}

export function CocinaBoard() {
  const { telefonoLocal, nombreLocal } = useLocal();
  const { logout } = useAuth();
  const router = useRouter();
  const snackbar = useSnackbar();

  const cerrarSesion = () => {
    logout();
    router.push("/login");
  };

  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [procesando, setProcesando] = useState<ReadonlySet<number>>(new Set());
  const [recien, setRecien] = useState<ReadonlySet<number>>(new Set());
  const [ahoraMs, setAhoraMs] = useState(() => Date.now());
  const [sonido, setSonido] = useState(true);

  // Preferencia de sonido (por dispositivo).
  useEffect(() => {
    try {
      const guardado = localStorage.getItem(CLAVE_SONIDO);
      if (guardado !== null) setSonido(guardado === "1");
    } catch {
      /* sin localStorage: queda en el default */
    }
  }, []);

  const cambiarSonido = () => {
    setSonido((prev) => {
      const nuevo = !prev;
      try {
        localStorage.setItem(CLAVE_SONIDO, nuevo ? "1" : "0");
      } catch {
        /* ignora */
      }
      if (nuevo) sonarPedidoNuevo(); // confirma y desbloquea el audio del navegador
      return nuevo;
    });
  };

  // Tick del reloj (una sola vez por segundo para todos los chits).
  useEffect(() => {
    const id = window.setInterval(() => setAhoraMs(Date.now()), 1000);
    return () => window.clearInterval(id);
  }, []);

  const cargar = useCallback(async () => {
    if (!telefonoLocal) return;
    try {
      setError(null);
      const data = await cocinaApi.getPedidosActivos(telefonoLocal);
      setPedidos([...data].sort(porHora));
    } catch (e) {
      setError(getErrorMessage(e) || "No se pudieron cargar los pedidos");
    } finally {
      setCargando(false);
    }
  }, [telefonoLocal]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  // Merge de un pedido que llega por socket o por respuesta de una acción.
  const aplicarPedido = useCallback((p: Pedido) => {
    setPedidos((prev) => {
      if (!ESTADOS_ACTIVOS.has(p.estado)) return prev.filter((x) => x.id !== p.id);
      const existe = prev.some((x) => x.id === p.id);
      const lista = existe ? prev.map((x) => (x.id === p.id ? p : x)) : [...prev, p];
      return lista.sort(porHora);
    });
  }, []);

  const quitarPedido = useCallback((id: number) => {
    setPedidos((prev) => prev.filter((x) => x.id !== id));
  }, []);

  const marcarRecien = useCallback((id: number) => {
    setRecien((prev) => new Set(prev).add(id));
    window.setTimeout(() => {
      setRecien((prev) => {
        const next = new Set(prev);
        next.delete(id);
        return next;
      });
    }, 2600);
  }, []);

  const sonidoRef = useRef(sonido);
  sonidoRef.current = sonido;

  const { conectado } = usePedidosSocket({
    telefonoLocal,
    onNuevoPedido: (p) => {
      setPedidos((prev) => (prev.some((x) => x.id === p.id) ? prev : [...prev, p].sort(porHora)));
      marcarRecien(p.id);
      if (sonidoRef.current) sonarPedidoNuevo();
    },
    onActualizacion: aplicarPedido,
    onModificado: aplicarPedido,
    onCancelado: (p) => quitarPedido(p.id),
  });

  // Al reconectar, resincronizar por si se perdió algún evento.
  const estabaConectado = useRef(conectado);
  useEffect(() => {
    if (conectado && !estabaConectado.current) void cargar();
    estabaConectado.current = conectado;
  }, [conectado, cargar]);

  // Fallback: si no hay socket, refrescar cada 15 s.
  useEffect(() => {
    if (conectado) return;
    const id = window.setInterval(() => void cargar(), 15000);
    return () => window.clearInterval(id);
  }, [conectado, cargar]);

  const avanzar = async (pedido: Pedido, accion: AccionTicket) => {
    setProcesando((prev) => new Set(prev).add(pedido.id));
    try {
      const actualizado = await ejecutarAccion(pedido.id, accion);
      aplicarPedido(actualizado);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo actualizar el pedido", "error");
    } finally {
      setProcesando((prev) => {
        const next = new Set(prev);
        next.delete(pedido.id);
        return next;
      });
    }
  };

  const enCola = useMemo(() => pedidos.filter((p) => EN_COLA.has(p.estado)).length, [pedidos]);
  const masViejo = useMemo(() => {
    const activos = pedidos.filter((p) => EN_COLA.has(p.estado));
    if (activos.length === 0) return "—";
    const viejo = activos.reduce((a, b) => (porHora(a, b) <= 0 ? a : b));
    return tiempoTranscurrido(viejo.horaPedido, ahoraMs);
  }, [pedidos, ahoraMs]);

  return (
    <Box
      sx={{
        height: "100dvh",
        display: "flex",
        flexDirection: "column",
        bgcolor: T.fondo,
        color: "#fff",
        overflow: "hidden",
      }}
    >
      {/* Barra de pase */}
      <Box
        sx={{
          flex: "0 0 auto",
          px: { xs: 2, sm: 3 },
          py: 1.5,
          bgcolor: T.fondoRelieve,
          borderBottom: `1px solid ${T.linea}`,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: 2,
        }}
      >
        <Box sx={{ display: "flex", alignItems: "baseline", gap: 1.25, minWidth: 0 }}>
          <Box
            component="span"
            sx={{
              fontFamily: FUENTE_CONDENSADA,
              fontWeight: 700,
              fontSize: { xs: 20, sm: 24 },
              letterSpacing: "0.01em",
              whiteSpace: "nowrap",
              overflow: "hidden",
              textOverflow: "ellipsis",
            }}
          >
            {nombreLocal ?? "Cocina"}
          </Box>
          {nombreLocal && (
            <Box component="span" sx={{ color: T.sobreFondoTenue, fontSize: 14, whiteSpace: "nowrap" }}>
              Cocina
            </Box>
          )}
        </Box>

        <Box sx={{ display: "flex", alignItems: "center", gap: { xs: 2, sm: 3 } }}>
          <Metric numero={String(enCola)} etiqueta="en cola" />
          <Metric numero={masViejo} etiqueta="más viejo" />

          <Box sx={{ display: "flex", alignItems: "center", gap: 0.75 }}>
            <Box
              sx={{
                width: 8,
                height: 8,
                borderRadius: "50%",
                bgcolor: conectado ? T.ember : T.sobreFondoTenue,
                animation: conectado ? `${pulso} 2s ease-in-out infinite` : "none",
              }}
            />
            <Box component="span" sx={{ fontSize: 13, color: T.sobreFondoTenue, whiteSpace: "nowrap" }}>
              {conectado ? "en vivo" : "sin conexión"}
            </Box>
          </Box>

          <IconButton
            onClick={cambiarSonido}
            size="small"
            aria-label={sonido ? "Silenciar avisos de pedidos" : "Activar avisos de pedidos"}
            sx={{ color: sonido ? "#fff" : T.sobreFondoTenue }}
          >
            {sonido ? <VolumeUpIcon fontSize="small" /> : <VolumeOffIcon fontSize="small" />}
          </IconButton>

          <IconButton
            onClick={cerrarSesion}
            size="small"
            aria-label="Cerrar sesión"
            sx={{ color: T.sobreFondoTenue }}
          >
            <LogoutIcon fontSize="small" />
          </IconButton>
        </Box>
      </Box>

      {!conectado && !cargando && (
        <Box
          sx={{
            flex: "0 0 auto",
            px: { xs: 2, sm: 3 },
            py: 0.75,
            bgcolor: "rgba(192,64,42,0.18)",
            color: "#F0C088",
            fontSize: 13,
          }}
        >
          Sin conexión en vivo. Reintentando y actualizando la cola cada 15 segundos.
        </Box>
      )}

      {/* Riel de chits */}
      <Box sx={{ flex: 1, minHeight: 0, position: "relative" }}>
        {cargando ? (
          <Centro>
            <CircularProgress sx={{ color: T.ember }} />
          </Centro>
        ) : error ? (
          <Centro>
            <Box sx={{ textAlign: "center", maxWidth: 360 }}>
              <Box sx={{ fontSize: 16, mb: 2, color: "#F0C088" }}>{error}</Box>
              <Button
                onClick={() => void cargar()}
                variant="outlined"
                sx={{ color: "#fff", borderColor: T.linea, "&:hover": { borderColor: "#fff" } }}
              >
                Reintentar
              </Button>
            </Box>
          </Centro>
        ) : pedidos.length === 0 ? (
          <Centro>
            <Box sx={{ textAlign: "center" }}>
              <Box sx={{ fontFamily: FUENTE_CONDENSADA, fontWeight: 700, fontSize: 30, mb: 0.5 }}>
                La cola está vacía
              </Box>
              <Box sx={{ color: T.sobreFondoTenue, fontSize: 15 }}>
                Los pedidos entran acá apenas se confirman.
              </Box>
            </Box>
          </Centro>
        ) : (
          <Box
            sx={{
              position: "absolute",
              inset: 0,
              display: "flex",
              alignItems: "flex-start",
              gap: 1.5,
              px: 2,
              pt: 2,
              pb: 2,
              overflowX: "auto",
              overflowY: "hidden",
              // Riel del que "cuelgan" los chits.
              "&::before": {
                content: '""',
                position: "absolute",
                left: 0,
                right: 0,
                top: 22,
                height: "3px",
                bgcolor: T.linea,
                pointerEvents: "none",
              },
            }}
          >
            {pedidos.map((pedido) => (
              <TicketPedido
                key={pedido.id}
                pedido={pedido}
                ahoraMs={ahoraMs}
                procesando={procesando.has(pedido.id)}
                recienLlegado={recien.has(pedido.id)}
                onAvanzar={avanzar}
              />
            ))}
          </Box>
        )}
      </Box>

      <FeedbackSnackbar
        open={snackbar.open}
        message={snackbar.message}
        severity={snackbar.severity}
        onClose={snackbar.hideSnackbar}
      />
    </Box>
  );
}

function Metric({ numero, etiqueta }: { numero: string; etiqueta: string }) {
  return (
    <Box sx={{ display: "flex", alignItems: "baseline", gap: 0.625 }}>
      <Box
        component="span"
        sx={{
          fontFamily: FUENTE_CONDENSADA,
          fontWeight: 700,
          fontSize: { xs: 20, sm: 24 },
          lineHeight: 1,
          fontVariantNumeric: "tabular-nums",
        }}
      >
        {numero}
      </Box>
      <Box component="span" sx={{ fontSize: 12.5, color: T.sobreFondoTenue, whiteSpace: "nowrap" }}>
        {etiqueta}
      </Box>
    </Box>
  );
}

function Centro({ children }: { children: React.ReactNode }) {
  return (
    <Box
      sx={{
        position: "absolute",
        inset: 0,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        p: 3,
      }}
    >
      {children}
    </Box>
  );
}
