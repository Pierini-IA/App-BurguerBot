"use client";

import { Box, Button, CircularProgress } from "@mui/material";
import { keyframes } from "@mui/system";
import { EstadoPedido, Modalidad, OrigenPedido, type Pedido } from "@/types/api";
import { cocinaTema as T, FUENTE_CONDENSADA } from "@/lib/cocina/tema";
import { COLOR_NIVEL, minutosDesde, nivelPorEdad, tiempoTranscurrido } from "@/lib/cocina/tiempo";
import { accionParaEstado, type AccionTicket } from "@/lib/cocina/acciones";

const entrada = keyframes`
  from { transform: translateX(28px); opacity: 0; }
  to   { transform: translateX(0);    opacity: 1; }
`;

const latido = keyframes`
  0%, 100% { box-shadow: inset 4px 0 0 var(--edge); }
  50%      { box-shadow: inset 4px 0 0 var(--edge), 0 0 0 3px rgba(255,107,53,0.55); }
`;

interface TicketPedidoProps {
  pedido: Pedido;
  ahoraMs: number;
  procesando: boolean;
  recienLlegado: boolean;
  onAvanzar: (pedido: Pedido, accion: AccionTicket) => void;
}

const ETIQUETA_MODALIDAD: Record<Modalidad, string> = {
  [Modalidad.RETIRAR]: "Para retirar",
  [Modalidad.DELIVERY]: "Delivery",
};

export function TicketPedido({ pedido, ahoraMs, procesando, recienLlegado, onAvanzar }: TicketPedidoProps) {
  const minutos = minutosDesde(pedido.horaPedido, ahoraMs);
  const listoOEnCamino = pedido.estado === EstadoPedido.LISTO || pedido.estado === EstadoPedido.EN_CAMINO;
  const nivel = nivelPorEdad(minutos);
  const colorBorde = listoOEnCamino ? T.hecho : COLOR_NIVEL[nivel];
  const accion = accionParaEstado(pedido);

  return (
    <Box
      component="article"
      data-testid={`pedido-${pedido.id}`}
      sx={{
        "--edge": colorBorde,
        flex: "0 0 auto",
        width: { xs: 270, sm: 300 },
        maxHeight: "100%",
        display: "flex",
        flexDirection: "column",
        bgcolor: T.chit,
        color: T.tinta,
        borderRadius: "3px",
        boxShadow: "inset 4px 0 0 var(--edge)",
        overflow: "hidden",
        opacity: listoOEnCamino ? 0.72 : 1,
        animation: recienLlegado
          ? `${entrada} 240ms ease-out, ${latido} 900ms ease-in-out 2`
          : `${entrada} 240ms ease-out`,
        "@media (prefers-reduced-motion: reduce)": { animation: "none" },
      }}
    >
      {/* Encabezado del chit */}
      <Box sx={{ px: 2, pt: 1.5, pb: 1.25 }}>
        <Box sx={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", gap: 1 }}>
          <Box
            component="span"
            sx={{ fontFamily: FUENTE_CONDENSADA, fontWeight: 700, fontSize: 30, lineHeight: 1, letterSpacing: "0.01em" }}
          >
            #{pedido.id}
          </Box>
          <Box
            component="span"
            sx={{
              fontFamily: FUENTE_CONDENSADA,
              fontWeight: 600,
              fontSize: 24,
              lineHeight: 1,
              fontVariantNumeric: "tabular-nums",
              color: listoOEnCamino ? T.tintaTenue : colorBorde,
            }}
          >
            {tiempoTranscurrido(pedido.horaPedido, ahoraMs)}
          </Box>
        </Box>

        <Box
          sx={{
            mt: 0.75,
            display: "flex",
            flexWrap: "wrap",
            alignItems: "center",
            gap: 0.75,
            fontSize: 13,
            color: T.tintaTenue,
          }}
        >
          <Box component="span" sx={{ fontWeight: 600, color: T.tinta }}>
            {ETIQUETA_MODALIDAD[pedido.modalidad]}
          </Box>
          {pedido.origenPedido === OrigenPedido.BOT && (
            <Box
              component="span"
              sx={{
                px: 0.75,
                py: "1px",
                borderRadius: "3px",
                bgcolor: "rgba(37,211,102,0.16)",
                color: "#1f7a44",
                fontWeight: 600,
                fontSize: 11.5,
              }}
            >
              WhatsApp
            </Box>
          )}
          {pedido.cliente?.nombre && <Box component="span">{pedido.cliente.nombre}</Box>}
        </Box>

        {pedido.modalidad === Modalidad.DELIVERY && pedido.direccionEnvio && (
          <Box sx={{ mt: 0.5, fontSize: 12.5, color: T.tintaTenue }}>{pedido.direccionEnvio}</Box>
        )}
      </Box>

      {/* Perforación */}
      <Box sx={{ mx: 2, borderTop: `1.5px dashed ${T.tintaTenue}`, opacity: 0.5 }} />

      {/* Items */}
      <Box sx={{ flex: "1 1 auto", minHeight: 64, overflowY: "auto", px: 2, py: 1.25 }}>
        {pedido.items?.length ? (
          pedido.items.map((item) => (
            <Box key={item.id} sx={{ mb: 1.5, "&:last-of-type": { mb: 0 } }}>
              <Box sx={{ display: "flex", gap: 1, alignItems: "baseline" }}>
                <Box
                  component="span"
                  sx={{ fontFamily: FUENTE_CONDENSADA, fontWeight: 700, fontSize: 20, minWidth: 22, lineHeight: 1.1 }}
                >
                  {item.cantidad}
                </Box>
                <Box component="span" sx={{ fontSize: 16, fontWeight: 500, lineHeight: 1.25 }}>
                  {item.producto?.nombre ?? "Producto"}
                </Box>
              </Box>
              {item.extrasSeleccionados && item.extrasSeleccionados.length > 0 && (
                <Box sx={{ pl: "30px", fontSize: 13.5, color: T.tintaTenue, mt: 0.25 }}>
                  {item.extrasSeleccionados
                    .map((e) => e.extra?.nombre)
                    .filter(Boolean)
                    .map((nombre) => `+ ${nombre}`)
                    .join("   ")}
                </Box>
              )}
              {item.observaciones && (
                <Box
                  sx={{
                    pl: "30px",
                    fontSize: 13.5,
                    fontStyle: "italic",
                    color: T.emberProfundo,
                    mt: 0.25,
                  }}
                >
                  {item.observaciones}
                </Box>
              )}
            </Box>
          ))
        ) : (
          <Box sx={{ fontSize: 13.5, color: T.tintaTenue }}>Sin items cargados.</Box>
        )}
      </Box>

      {/* Bump bar */}
      {accion && (
        <Button
          onClick={() => onAvanzar(pedido, accion.accion)}
          disabled={procesando}
          disableElevation
          sx={{
            m: 0,
            borderRadius: 0,
            height: 56,
            fontFamily: FUENTE_CONDENSADA,
            fontWeight: 600,
            fontSize: 19,
            letterSpacing: "0.02em",
            textTransform: "none",
            color: accion.accion === "iniciar" ? T.tinta : "#fff",
            bgcolor:
              accion.accion === "iniciar"
                ? "transparent"
                : accion.accion === "listo"
                  ? T.ember
                  : T.hecho,
            borderTop: accion.accion === "iniciar" ? `1.5px solid ${T.tinta}` : "none",
            "&:hover": {
              bgcolor:
                accion.accion === "iniciar"
                  ? "rgba(34,30,27,0.08)"
                  : accion.accion === "listo"
                    ? "#E85A2A"
                    : "#4C6A4A",
            },
            "&.Mui-disabled": { color: T.tintaTenue },
          }}
        >
          {procesando ? <CircularProgress size={20} sx={{ color: "inherit" }} /> : accion.label}
        </Button>
      )}
    </Box>
  );
}
