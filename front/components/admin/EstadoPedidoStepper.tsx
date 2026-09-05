"use client";

import { Box, Typography } from "@mui/material";
import { EstadoPedido, Modalidad } from "@/types/api";
import { ETIQUETA_ESTADO } from "@/lib/pedidos/formato";

/** Camino real que recorre un pedido, según su modalidad. Es la única secuencia genuina acá. */
const CAMINO: Record<Modalidad, EstadoPedido[]> = {
  [Modalidad.RETIRAR]: [
    EstadoPedido.PENDIENTE,
    EstadoPedido.CONFIRMADO,
    EstadoPedido.EN_PREPARACION,
    EstadoPedido.LISTO,
    EstadoPedido.ENTREGADO,
  ],
  [Modalidad.DELIVERY]: [
    EstadoPedido.PENDIENTE,
    EstadoPedido.CONFIRMADO,
    EstadoPedido.EN_PREPARACION,
    EstadoPedido.LISTO,
    EstadoPedido.EN_CAMINO,
    EstadoPedido.ENTREGADO,
  ],
};

interface EstadoPedidoStepperProps {
  estado: EstadoPedido;
  modalidad: Modalidad;
}

/**
 * Recorrido del pedido por sus estados reales. Si está cancelado, se muestra
 * un aviso en vez del camino (cancelar no es un paso más de la secuencia).
 */
export function EstadoPedidoStepper({ estado, modalidad }: EstadoPedidoStepperProps) {
  if (estado === EstadoPedido.CANCELADO) {
    return (
      <Box sx={{ py: 1, px: 1.5, borderRadius: 1, bgcolor: "error.main", color: "error.contrastText" }}>
        <Typography variant="body2" fontWeight={600}>
          Este pedido fue cancelado
        </Typography>
      </Box>
    );
  }

  const pasos = CAMINO[modalidad];
  const indiceActual = pasos.indexOf(estado);

  return (
    <Box sx={{ display: "flex", alignItems: "flex-start" }}>
      {pasos.map((paso, i) => {
        const completado = i < indiceActual;
        const actual = i === indiceActual;
        const esUltimo = i === pasos.length - 1;
        return (
          <Box key={paso} sx={{ display: "flex", flexDirection: "column", alignItems: "center", flex: esUltimo ? "0 0 auto" : 1 }}>
            <Box sx={{ display: "flex", alignItems: "center", width: "100%" }}>
              <Box
                sx={{
                  width: actual ? 12 : 9,
                  height: actual ? 12 : 9,
                  borderRadius: "50%",
                  flexShrink: 0,
                  bgcolor: completado || actual ? "primary.main" : "action.disabledBackground",
                  border: actual ? "2px solid" : "none",
                  borderColor: "primary.dark",
                  transition: "all 150ms ease",
                }}
              />
              {!esUltimo && (
                <Box
                  sx={{
                    flex: 1,
                    height: 2,
                    bgcolor: completado ? "primary.main" : "action.disabledBackground",
                    transition: "background-color 150ms ease",
                  }}
                />
              )}
            </Box>
            <Typography
              variant="caption"
              align="center"
              sx={{
                mt: 0.5,
                fontWeight: actual ? 700 : 400,
                color: actual ? "text.primary" : "text.secondary",
                fontSize: 11,
                lineHeight: 1.2,
                maxWidth: 64,
              }}
            >
              {ETIQUETA_ESTADO[paso]}
            </Typography>
          </Box>
        );
      })}
    </Box>
  );
}
