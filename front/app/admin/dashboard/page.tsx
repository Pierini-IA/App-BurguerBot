"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { Box, Button, IconButton, Skeleton, Tooltip, Typography } from "@mui/material";
import { Refresh as RefreshIcon, ShoppingBag as ShoppingBagIcon, EventNote as EventNoteIcon } from "@mui/icons-material";
import { useRouter } from "next/navigation";
import { PageHeader, EmptyState, EstadoPedidoChip } from "@/components/shared";
import { useLocal } from "@/lib/context/LocalContext";
import { estadisticasApi } from "@/lib/api/estadisticas";
import { pedidosApi } from "@/lib/api/pedidos";
import { ingredientesApi } from "@/lib/api/ingredientes";
import { getErrorMessage } from "@/lib/api/axios";
import type { EstadisticasDelDia, Pedido } from "@/types/api";
import type { Ingrediente } from "@/types/producto";
import { UNIDAD_MEDIDA_LABEL } from "@/types/producto";
import { cantidadItems, horaCorta, precioAR } from "@/lib/pedidos/formato";

const ULTIMOS_PEDIDOS = 5;
const INGREDIENTES_A_VIGILAR = 4;

export default function DashboardPage() {
  const { telefonoLocal, nombreLocal } = useLocal();
  const router = useRouter();

  const [stats, setStats] = useState<EstadisticasDelDia | null>(null);
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [ingredientes, setIngredientes] = useState<Ingrediente[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const cargar = useCallback(async () => {
    if (!telefonoLocal) return;
    setCargando(true);
    setError(null);
    try {
      const [statsData, pedidosData, ingredientesData] = await Promise.all([
        estadisticasApi.getDelDia(telefonoLocal),
        pedidosApi.getDelDia(telefonoLocal),
        ingredientesApi.getAll(),
      ]);
      setStats(statsData);
      setPedidos(pedidosData);
      setIngredientes(ingredientesData);
    } catch (e) {
      setError(getErrorMessage(e) || "No se pudo cargar el resumen del día");
    } finally {
      setCargando(false);
    }
  }, [telefonoLocal]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const ticketPromedio = useMemo(() => {
    if (!stats || stats.totalPedidos === 0) return null;
    return stats.ingresosPedidos / stats.totalPedidos;
  }, [stats]);

  const ultimosPedidos = useMemo(
    () =>
      [...pedidos]
        .sort((a, b) => new Date(b.horaPedido).getTime() - new Date(a.horaPedido).getTime())
        .slice(0, ULTIMOS_PEDIDOS),
    [pedidos]
  );

  const aVigilar = useMemo(
    () => [...ingredientes].sort((a, b) => (a.stockActual ?? 0) - (b.stockActual ?? 0)).slice(0, INGREDIENTES_A_VIGILAR),
    [ingredientes]
  );

  if (error) {
    return (
      <Box>
        <PageHeader title="Dashboard" subtitle={nombreLocal ? `El día de hoy en ${nombreLocal}` : undefined} />
        <EmptyState
          title="No se pudo cargar el resumen"
          description={error}
          action={
            <Button variant="contained" onClick={cargar}>
              Reintentar
            </Button>
          }
        />
      </Box>
    );
  }

  return (
    <Box>
      <PageHeader
        title="Dashboard"
        subtitle={nombreLocal ? `El día de hoy en ${nombreLocal}` : "Un vistazo al día de hoy"}
        action={
          <Tooltip title="Actualizar">
            <IconButton onClick={cargar} disabled={cargando} aria-label="Actualizar dashboard">
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        }
      />

      {/* Hero + métricas secundarias */}
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: { xs: "1fr", md: "2fr 1fr 1fr" },
          gap: 2,
          mb: 3,
        }}
      >
        <Box
          sx={{
            p: 3,
            borderRadius: 3,
            border: "1px solid",
            borderColor: "divider",
            background: (theme) =>
              `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`,
            color: "primary.contrastText",
          }}
        >
          <Typography variant="body2" sx={{ opacity: 0.85 }}>
            Ingresos de hoy
          </Typography>
          {cargando ? (
            <Skeleton variant="text" width={180} height={56} sx={{ bgcolor: "rgba(255,255,255,0.25)" }} />
          ) : (
            <Typography variant="h3" fontWeight={700} sx={{ fontVariantNumeric: "tabular-nums", mt: 0.5 }}>
              {precioAR.format(stats?.ingresosTotal ?? 0)}
            </Typography>
          )}
          {ticketPromedio != null && !cargando && (
            <Typography variant="body2" sx={{ opacity: 0.85, mt: 0.5 }}>
              Ticket promedio {precioAR.format(ticketPromedio)}
            </Typography>
          )}
        </Box>

        <MetricaSecundaria
          icon={<ShoppingBagIcon />}
          label="Pedidos hoy"
          valor={stats?.totalPedidos}
          cargando={cargando}
          onClick={() => router.push("/admin/pedidos")}
        />
        <MetricaSecundaria
          icon={<EventNoteIcon />}
          label="Reservas hoy"
          valor={stats?.totalReservas}
          cargando={cargando}
        />
      </Box>

      {/* Últimos pedidos + stock a vigilar */}
      <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", md: "2fr 1fr" }, gap: 2 }}>
        <Panel title="Últimos pedidos" verMas={{ label: "Ver todo", onClick: () => router.push("/admin/pedidos") }}>
          {cargando ? (
            <Skeletons n={4} />
          ) : ultimosPedidos.length === 0 ? (
            <Typography variant="body2" color="text.secondary">
              Todavía no entró ningún pedido hoy.
            </Typography>
          ) : (
            ultimosPedidos.map((p) => (
              <Box
                key={p.id}
                onClick={() => router.push("/admin/pedidos")}
                sx={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  gap: 1.5,
                  py: 1.25,
                  borderBottom: "1px solid",
                  borderColor: "divider",
                  cursor: "pointer",
                  "&:last-of-type": { borderBottom: "none" },
                  "&:hover": { bgcolor: "action.hover" },
                }}
              >
                <Box sx={{ display: "flex", alignItems: "center", gap: 1.5, minWidth: 0 }}>
                  <Typography variant="body2" fontWeight={700} sx={{ fontVariantNumeric: "tabular-nums" }}>
                    #{p.id}
                  </Typography>
                  <Box sx={{ minWidth: 0 }}>
                    <Typography variant="body2" noWrap>
                      {p.cliente?.nombre ?? "Sin datos"}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {horaCorta(p.horaPedido)} · {cantidadItems(p)} ítem{cantidadItems(p) !== 1 ? "s" : ""}
                    </Typography>
                  </Box>
                </Box>
                <Box sx={{ display: "flex", alignItems: "center", gap: 1.5, flexShrink: 0 }}>
                  <Typography variant="body2" fontWeight={600} sx={{ fontVariantNumeric: "tabular-nums" }}>
                    {precioAR.format(p.total ?? 0)}
                  </Typography>
                  <EstadoPedidoChip estado={p.estado} />
                </Box>
              </Box>
            ))
          )}
        </Panel>

        <Panel title="Stock bajo" verMas={{ label: "Ver todo", onClick: () => router.push("/admin/ingredientes") }}>
          {cargando ? (
            <Skeletons n={4} />
          ) : aVigilar.length === 0 ? (
            <Typography variant="body2" color="text.secondary">
              Todavía no cargaste ingredientes.
            </Typography>
          ) : (
            aVigilar.map((i) => (
              <Box
                key={i.id}
                sx={{
                  display: "flex",
                  alignItems: "baseline",
                  justifyContent: "space-between",
                  py: 1.25,
                  borderBottom: "1px solid",
                  borderColor: "divider",
                  "&:last-of-type": { borderBottom: "none" },
                }}
              >
                <Typography variant="body2" noWrap sx={{ pr: 1 }}>
                  {i.nombre}
                </Typography>
                <Typography
                  variant="body2"
                  fontWeight={700}
                  color={(i.stockActual ?? 0) <= 0 ? "error.main" : "text.primary"}
                  sx={{ fontVariantNumeric: "tabular-nums", flexShrink: 0 }}
                >
                  {i.stockActual ?? 0} {UNIDAD_MEDIDA_LABEL[i.unidadMedida]?.toLowerCase()}
                </Typography>
              </Box>
            ))
          )}
        </Panel>
      </Box>
    </Box>
  );
}

function MetricaSecundaria({
  icon,
  label,
  valor,
  cargando,
  onClick,
}: {
  icon: React.ReactNode;
  label: string;
  valor?: number;
  cargando: boolean;
  onClick?: () => void;
}) {
  return (
    <Box
      onClick={onClick}
      sx={{
        p: 3,
        borderRadius: 3,
        border: "1px solid",
        borderColor: "divider",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        cursor: onClick ? "pointer" : "default",
        "&:hover": onClick ? { borderColor: "primary.main" } : undefined,
      }}
    >
      <Box sx={{ display: "flex", alignItems: "center", gap: 1, color: "text.secondary", mb: 0.5 }}>
        {icon}
        <Typography variant="body2">{label}</Typography>
      </Box>
      {cargando ? (
        <Skeleton variant="text" width={60} height={40} />
      ) : (
        <Typography variant="h4" fontWeight={700} sx={{ fontVariantNumeric: "tabular-nums" }}>
          {valor ?? 0}
        </Typography>
      )}
    </Box>
  );
}

function Panel({
  title,
  verMas,
  children,
}: {
  title: string;
  verMas?: { label: string; onClick: () => void };
  children: React.ReactNode;
}) {
  return (
    <Box sx={{ p: 3, borderRadius: 3, border: "1px solid", borderColor: "divider" }}>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 1.5, gap: 1 }}>
        <Typography variant="subtitle1" fontWeight={700} noWrap>
          {title}
        </Typography>
        {verMas && (
          <Button size="small" onClick={verMas.onClick} sx={{ flexShrink: 0, whiteSpace: "nowrap" }}>
            {verMas.label}
          </Button>
        )}
      </Box>
      {children}
    </Box>
  );
}

function Skeletons({ n }: { n: number }) {
  return (
    <>
      {Array.from({ length: n }).map((_, i) => (
        <Skeleton key={i} variant="text" height={40} sx={{ my: 0.5 }} />
      ))}
    </>
  );
}
