"use client";

import { Box } from "@mui/material";
import { Modalidad, OrigenPedido, type Pedido } from "@/types/api";
import { precioAR, horaCorta, ETIQUETA_MODALIDAD } from "@/lib/pedidos/formato";

interface TicketComandaProps {
  pedido: Pedido;
  nombreLocal?: string | null;
  /** Marca visual de reimpresión. */
  reimpresion?: boolean;
}

const MONO = '"Courier New", "DejaVu Sans Mono", monospace';

/**
 * Comanda para impresión térmica (~80 mm). Blanco y negro, monoespaciada,
 * pensada para leerse de un vistazo en el mostrador y para el cliente.
 *
 * Solo se ve al imprimir (ver regla `.comanda-print` en `globals.css`).
 */
export function TicketComanda({ pedido, nombreLocal, reimpresion }: TicketComandaProps) {
  const linea = "--------------------------------";

  return (
    <Box
      className="comanda-print"
      sx={{
        fontFamily: MONO,
        color: "#000",
        bgcolor: "#fff",
        fontSize: 12.5,
        lineHeight: 1.45,
        width: "80mm",
        p: 1,
      }}
    >
      <Box sx={{ textAlign: "center", mb: 0.5 }}>
        <Box sx={{ fontWeight: 700, fontSize: 15, textTransform: "uppercase" }}>{nombreLocal ?? "Dio Burger"}</Box>
        <Box>Comanda de pedido</Box>
      </Box>

      <Box>{linea}</Box>

      <Box sx={{ display: "flex", justifyContent: "space-between", fontWeight: 700, fontSize: 14 }}>
        <span>PEDIDO #{pedido.id}</span>
        <span>{horaCorta(pedido.horaPedido)}</span>
      </Box>
      <Box sx={{ display: "flex", justifyContent: "space-between" }}>
        <span>{ETIQUETA_MODALIDAD[pedido.modalidad]}</span>
        {pedido.origenPedido === OrigenPedido.BOT && <span>WhatsApp</span>}
      </Box>
      {pedido.cliente?.nombre && <Box>Cliente: {pedido.cliente.nombre}</Box>}
      {pedido.cliente?.telefono && <Box>Tel: {pedido.cliente.telefono}</Box>}
      {pedido.modalidad === Modalidad.DELIVERY && pedido.direccionEnvio && (
        <Box>Envío: {pedido.direccionEnvio}</Box>
      )}

      <Box>{linea}</Box>

      {pedido.items?.map((item) => (
        <Box key={item.id} sx={{ mb: 0.5 }}>
          <Box sx={{ display: "flex", justifyContent: "space-between" }}>
            <span>
              {item.cantidad} x {item.producto?.nombre ?? "Producto"}
            </span>
            {item.producto?.precio != null && <span>{precioAR.format(item.producto.precio * item.cantidad)}</span>}
          </Box>
          {item.extrasSeleccionados?.map((e, i) =>
            e.extra?.nombre ? (
              <Box key={i} sx={{ pl: 2 }}>
                + {e.extra.nombre}
              </Box>
            ) : null
          )}
          {item.observaciones && <Box sx={{ pl: 2, fontWeight: 700 }}>* {item.observaciones}</Box>}
        </Box>
      ))}

      <Box>{linea}</Box>

      <Box sx={{ display: "flex", justifyContent: "space-between", fontWeight: 700, fontSize: 14 }}>
        <span>TOTAL</span>
        <span>{precioAR.format(pedido.total ?? 0)}</span>
      </Box>
      {pedido.medioPago && <Box>Pago: {pedido.medioPago.replace("_", " ").toLowerCase()}</Box>}

      <Box>{linea}</Box>

      <Box sx={{ textAlign: "center", mt: 0.5 }}>
        {reimpresion && <Box sx={{ fontWeight: 700 }}>** REIMPRESIÓN **</Box>}
        <Box>¡Gracias por tu compra!</Box>
      </Box>
    </Box>
  );
}
