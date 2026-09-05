import { Chip, ChipProps } from "@mui/material";
import { EstadoPedido } from "@/types/api";

const CONFIG: Record<EstadoPedido, { label: string; color: ChipProps["color"] }> = {
  [EstadoPedido.PENDIENTE]: { label: "Pendiente", color: "default" },
  [EstadoPedido.CONFIRMADO]: { label: "Confirmado", color: "info" },
  [EstadoPedido.EN_PREPARACION]: { label: "En preparación", color: "warning" },
  [EstadoPedido.LISTO]: { label: "Listo", color: "success" },
  [EstadoPedido.EN_CAMINO]: { label: "En camino", color: "primary" },
  [EstadoPedido.ENTREGADO]: { label: "Entregado", color: "default" },
  [EstadoPedido.CANCELADO]: { label: "Cancelado", color: "error" },
};

interface EstadoPedidoChipProps {
  estado: EstadoPedido;
  size?: ChipProps["size"];
}

/**
 * Chip con color y etiqueta según el estado del pedido.
 */
export const EstadoPedidoChip: React.FC<EstadoPedidoChipProps> = ({ estado, size = "small" }) => {
  const cfg = CONFIG[estado] ?? { label: estado, color: "default" as const };
  return <Chip label={cfg.label} color={cfg.color} size={size} sx={{ fontWeight: 600 }} />;
};
