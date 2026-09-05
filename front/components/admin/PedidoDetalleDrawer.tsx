"use client";

import { useState } from "react";
import { Box, Button, CircularProgress, Drawer, IconButton, Typography, useMediaQuery, useTheme } from "@mui/material";
import { Close as CloseIcon, DeliveryDining, Storefront } from "@mui/icons-material";
import { MedioPago, Modalidad, OrigenPedido, type Pedido } from "@/types/api";
import { EstadoPedidoChip } from "@/components/shared";
import { EstadoPedidoStepper } from "./EstadoPedidoStepper";
import { precioAR, horaCorta, tiempoRelativo, ETIQUETA_MODALIDAD } from "@/lib/pedidos/formato";
import { accionParaEstado, ejecutarAccion } from "@/lib/cocina/acciones";

const ETIQUETA_MEDIO_PAGO: Record<MedioPago, string> = {
  [MedioPago.EFECTIVO]: "Efectivo",
  [MedioPago.TRANSFERENCIA]: "Transferencia",
  [MedioPago.TARJETA_DEBITO]: "Tarjeta de débito",
  [MedioPago.TARJETA_CREDITO]: "Tarjeta de crédito",
  [MedioPago.QR]: "QR",
};

interface PedidoDetalleDrawerProps {
  pedido: Pedido | null;
  onClose: () => void;
  onCambiado: (pedido: Pedido) => void;
  onError: (mensaje: string) => void;
}

/**
 * Detalle de un pedido, con la forma de una comanda: encabezado, recorrido de
 * estados, ítems separados por una perforación, y el total al pie.
 */
export function PedidoDetalleDrawer({ pedido, onClose, onCambiado, onError }: PedidoDetalleDrawerProps) {
  const theme = useTheme();
  const pantallaChica = useMediaQuery(theme.breakpoints.down("sm"));
  const [procesando, setProcesando] = useState(false);

  if (!pedido) return null;

  const accion = accionParaEstado(pedido);

  const avanzar = async () => {
    if (!accion) return;
    setProcesando(true);
    try {
      const actualizado = await ejecutarAccion(pedido.id, accion.accion);
      onCambiado(actualizado);
    } catch (e: unknown) {
      const mensaje =
        e && typeof e === "object" && "response" in e
          ? // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (e as any).response?.data?.message
          : undefined;
      onError(mensaje || "No se pudo actualizar el pedido");
    } finally {
      setProcesando(false);
    }
  };

  return (
    <Drawer anchor={pantallaChica ? "bottom" : "right"} open={!!pedido} onClose={onClose}>
      <Box
        sx={{
          width: pantallaChica ? "100vw" : 400,
          maxHeight: pantallaChica ? "88vh" : "100vh",
          display: "flex",
          flexDirection: "column",
          bgcolor: "background.paper",
        }}
      >
        {/* Encabezado */}
        <Box sx={{ px: 3, pt: 3, pb: 2 }}>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
            <Box>
              <Typography variant="h5" fontWeight={700} sx={{ fontVariantNumeric: "tabular-nums" }}>
                Pedido #{pedido.id}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {horaCorta(pedido.horaPedido)} · {tiempoRelativo(pedido.horaPedido)}
              </Typography>
            </Box>
            <IconButton onClick={onClose} aria-label="Cerrar detalle del pedido" size="small">
              <CloseIcon fontSize="small" />
            </IconButton>
          </Box>

          <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1, mt: 1.5 }}>
            <EstadoPedidoChip estado={pedido.estado} />
            {pedido.origenPedido === OrigenPedido.BOT && (
              <Box
                sx={{
                  px: 1,
                  py: "2px",
                  borderRadius: "4px",
                  bgcolor: "rgba(37,211,102,0.12)",
                  color: "#1f7a44",
                  fontSize: 12.5,
                  fontWeight: 600,
                  display: "flex",
                  alignItems: "center",
                }}
              >
                Pedido por WhatsApp
              </Box>
            )}
          </Box>
        </Box>

        {/* Recorrido de estados */}
        <Box sx={{ px: 3, pb: 2.5 }}>
          <EstadoPedidoStepper estado={pedido.estado} modalidad={pedido.modalidad} />
        </Box>

        <Box sx={{ mx: 3, borderTop: "1px solid", borderColor: "divider" }} />

        {/* Cliente y entrega */}
        <Box sx={{ px: 3, py: 2 }}>
          {pedido.cliente?.nombre && (
            <Typography variant="body2" fontWeight={600}>
              {pedido.cliente.nombre}
              {pedido.cliente.telefono && (
                <Typography component="span" variant="body2" color="text.secondary">
                  {" · " + pedido.cliente.telefono}
                </Typography>
              )}
            </Typography>
          )}
          <Box sx={{ display: "flex", alignItems: "center", gap: 0.75, mt: pedido.cliente?.nombre ? 0.5 : 0, color: "text.secondary" }}>
            {pedido.modalidad === Modalidad.DELIVERY ? (
              <DeliveryDining fontSize="small" />
            ) : (
              <Storefront fontSize="small" />
            )}
            <Typography variant="body2">{ETIQUETA_MODALIDAD[pedido.modalidad]}</Typography>
          </Box>
          {pedido.modalidad === Modalidad.DELIVERY && pedido.direccionEnvio && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>
              {pedido.direccionEnvio}
            </Typography>
          )}
          {pedido.repartidorNombre && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>
              Repartidor: {pedido.repartidorNombre}
            </Typography>
          )}
        </Box>

        {/* Perforación de la comanda */}
        <Box sx={{ mx: 3, borderTop: "1.5px dashed", borderColor: "divider" }} />

        {/* Items */}
        <Box sx={{ flex: 1, overflowY: "auto", px: 3, py: 2 }}>
          {pedido.items?.length ? (
            pedido.items.map((item) => (
              <Box key={item.id} sx={{ mb: 1.75, "&:last-of-type": { mb: 0 } }}>
                <Box sx={{ display: "flex", justifyContent: "space-between", gap: 1 }}>
                  <Box sx={{ display: "flex", gap: 1 }}>
                    <Typography variant="body2" fontWeight={700} sx={{ minWidth: 20 }}>
                      {item.cantidad}
                    </Typography>
                    <Typography variant="body2">{item.producto?.nombre ?? "Producto"}</Typography>
                  </Box>
                  {item.producto?.precio != null && (
                    <Typography variant="body2" color="text.secondary" sx={{ fontVariantNumeric: "tabular-nums" }}>
                      {precioAR.format(item.producto.precio * item.cantidad)}
                    </Typography>
                  )}
                </Box>
                {item.extrasSeleccionados && item.extrasSeleccionados.length > 0 && (
                  <Typography variant="caption" color="text.secondary" sx={{ pl: "28px", display: "block" }}>
                    {item.extrasSeleccionados
                      .map((e) => e.extra?.nombre)
                      .filter(Boolean)
                      .map((n) => `+ ${n}`)
                      .join("   ")}
                  </Typography>
                )}
                {item.observaciones && (
                  <Typography variant="caption" color="error.main" sx={{ pl: "28px", display: "block", fontStyle: "italic" }}>
                    {item.observaciones}
                  </Typography>
                )}
              </Box>
            ))
          ) : (
            <Typography variant="body2" color="text.secondary">
              Sin ítems cargados.
            </Typography>
          )}
        </Box>

        <Box sx={{ mx: 3, borderTop: "1.5px dashed", borderColor: "divider" }} />

        {/* Total y pago */}
        <Box sx={{ px: 3, py: 2 }}>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "baseline" }}>
            <Typography variant="body1" fontWeight={600}>
              Total
            </Typography>
            <Typography variant="h6" fontWeight={700} sx={{ fontVariantNumeric: "tabular-nums" }}>
              {precioAR.format(pedido.total ?? 0)}
            </Typography>
          </Box>
          {pedido.medioPago && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              {ETIQUETA_MEDIO_PAGO[pedido.medioPago]}
              {pedido.estadoPago === "PAGADO" && " · Pagado"}
              {pedido.estadoPago === "PENDIENTE" && " · Pago pendiente"}
            </Typography>
          )}
        </Box>

        {/* Acción */}
        {accion && (
          <Box sx={{ px: 3, pb: 3, pt: 1 }}>
            <Button
              fullWidth
              variant="contained"
              size="large"
              disabled={procesando}
              onClick={avanzar}
              startIcon={procesando ? <CircularProgress size={18} color="inherit" /> : undefined}
            >
              {accion.label}
            </Button>
          </Box>
        )}
      </Box>
    </Drawer>
  );
}
