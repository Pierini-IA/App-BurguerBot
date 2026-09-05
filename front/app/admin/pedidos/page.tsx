"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Box,
  Button,
  InputAdornment,
  Tab,
  Tabs,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from "@mui/material";
import { Search as SearchIcon, DeliveryDining, Storefront } from "@mui/icons-material";
import { PageHeader, DataTable, EstadoPedidoChip, FeedbackSnackbar, type Column } from "@/components/shared";
import { PedidoDetalleDrawer } from "@/components/admin/PedidoDetalleDrawer";
import { pedidosApi } from "@/lib/api/pedidos";
import { getErrorMessage } from "@/lib/api/axios";
import { useLocal } from "@/lib/context/LocalContext";
import { usePedidosSocket } from "@/lib/hooks/usePedidosSocket";
import { useSnackbar } from "@/lib/hooks/useSnackbar";
import { EstadoPedido, Modalidad, OrigenPedido, type Pedido } from "@/types/api";
import { cantidadItems, fechaISO, hace, horaCorta, hoy, precioAR, ETIQUETA_MODALIDAD } from "@/lib/pedidos/formato";

type RangoPreset = "HOY" | "AYER" | "7D" | "PERSONALIZADO";
type TabEstado = "TODOS" | "ACTIVOS" | "ENTREGADOS" | "CANCELADOS";

const ACTIVOS = new Set<EstadoPedido>([
  EstadoPedido.PENDIENTE,
  EstadoPedido.CONFIRMADO,
  EstadoPedido.EN_PREPARACION,
  EstadoPedido.LISTO,
  EstadoPedido.EN_CAMINO,
]);

function rangoDe(preset: RangoPreset): { desde: string; hasta: string } {
  const h = fechaISO(hoy());
  switch (preset) {
    case "HOY":
      return { desde: h, hasta: h };
    case "AYER": {
      const a = fechaISO(hace(1));
      return { desde: a, hasta: a };
    }
    case "7D":
      return { desde: fechaISO(hace(6)), hasta: h };
    default:
      return { desde: h, hasta: h };
  }
}

export default function PedidosPage() {
  const { telefonoLocal } = useLocal();
  const snackbar = useSnackbar();

  const [preset, setPreset] = useState<RangoPreset>("HOY");
  const [desde, setDesde] = useState(rangoDe("HOY").desde);
  const [hasta, setHasta] = useState(rangoDe("HOY").hasta);

  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [tab, setTab] = useState<TabEstado>("TODOS");
  const [modalidad, setModalidad] = useState<"TODAS" | Modalidad>("TODAS");
  const [busqueda, setBusqueda] = useState("");
  const [seleccionado, setSeleccionado] = useState<Pedido | null>(null);

  const cambiarPreset = (p: RangoPreset) => {
    setPreset(p);
    if (p !== "PERSONALIZADO") {
      const r = rangoDe(p);
      setDesde(r.desde);
      setHasta(r.hasta);
    }
  };

  const cargar = useCallback(async () => {
    if (!telefonoLocal) return;
    setCargando(true);
    setError(null);
    try {
      const data = await pedidosApi.getByRango({ telefonoLocal, fechaInicio: desde, fechaFin: hasta });
      setPedidos(data);
    } catch (e) {
      setError(getErrorMessage(e) || "No se pudieron cargar los pedidos");
    } finally {
      setCargando(false);
    }
  }, [telefonoLocal, desde, hasta]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  // Tiempo real: solo tiene sentido si el rango visible incluye hoy.
  const rangoIncluyeHoy = desde <= fechaISO(hoy()) && hasta >= fechaISO(hoy());
  const upsert = useCallback((p: Pedido) => {
    setPedidos((prev) => {
      const existe = prev.some((x) => x.id === p.id);
      return existe ? prev.map((x) => (x.id === p.id ? p : x)) : [p, ...prev];
    });
    setSeleccionado((actual) => (actual && actual.id === p.id ? p : actual));
  }, []);
  usePedidosSocket({
    telefonoLocal,
    enabled: rangoIncluyeHoy,
    onNuevoPedido: upsert,
    onActualizacion: upsert,
    onModificado: upsert,
    onCancelado: upsert,
    onRepartidorAsignado: upsert,
  });

  const filtrados = useMemo(() => {
    const q = busqueda.trim().toLowerCase();
    return pedidos
      .filter((p) => {
        if (tab === "ACTIVOS") return ACTIVOS.has(p.estado);
        if (tab === "ENTREGADOS") return p.estado === EstadoPedido.ENTREGADO;
        if (tab === "CANCELADOS") return p.estado === EstadoPedido.CANCELADO;
        return true;
      })
      .filter((p) => modalidad === "TODAS" || p.modalidad === modalidad)
      .filter((p) => {
        if (!q) return true;
        return (
          String(p.id).includes(q) ||
          p.cliente?.nombre?.toLowerCase().includes(q) ||
          p.cliente?.telefono?.toLowerCase().includes(q)
        );
      })
      .sort((a, b) => new Date(b.horaPedido).getTime() - new Date(a.horaPedido).getTime());
  }, [pedidos, tab, modalidad, busqueda]);

  const conteos = useMemo(
    () => ({
      TODOS: pedidos.length,
      ACTIVOS: pedidos.filter((p) => ACTIVOS.has(p.estado)).length,
      ENTREGADOS: pedidos.filter((p) => p.estado === EstadoPedido.ENTREGADO).length,
      CANCELADOS: pedidos.filter((p) => p.estado === EstadoPedido.CANCELADO).length,
    }),
    [pedidos]
  );

  const columnas: Column<Pedido>[] = [
    {
      key: "id",
      header: "Pedido",
      width: 110,
      render: (p) => (
        <Typography variant="body2" fontWeight={700} sx={{ fontVariantNumeric: "tabular-nums" }}>
          #{p.id}
        </Typography>
      ),
    },
    {
      key: "hora",
      header: "Hora",
      width: 90,
      render: (p) => (
        <Typography variant="body2" sx={{ fontVariantNumeric: "tabular-nums" }}>
          {horaCorta(p.horaPedido)}
        </Typography>
      ),
    },
    {
      key: "cliente",
      header: "Cliente",
      render: (p) => (
        <Box>
          <Typography variant="body2" fontWeight={600}>
            {p.cliente?.nombre ?? "Sin datos"}
          </Typography>
          {p.origenPedido === OrigenPedido.BOT && (
            <Typography variant="caption" color="success.main">
              Por WhatsApp
            </Typography>
          )}
        </Box>
      ),
    },
    {
      key: "modalidad",
      header: "Modalidad",
      width: 140,
      render: (p) => (
        <Box sx={{ display: "flex", alignItems: "center", gap: 0.75, color: "text.secondary" }}>
          {p.modalidad === Modalidad.DELIVERY ? (
            <DeliveryDining fontSize="small" />
          ) : (
            <Storefront fontSize="small" />
          )}
          <Typography variant="body2">{ETIQUETA_MODALIDAD[p.modalidad]}</Typography>
        </Box>
      ),
    },
    {
      key: "items",
      header: "Ítems",
      align: "center",
      width: 70,
      render: (p) => cantidadItems(p),
    },
    {
      key: "total",
      header: "Total",
      align: "right",
      width: 110,
      render: (p) => (
        <Typography variant="body2" fontWeight={600} sx={{ fontVariantNumeric: "tabular-nums" }}>
          {precioAR.format(p.total ?? 0)}
        </Typography>
      ),
    },
    {
      key: "estado",
      header: "Estado",
      width: 150,
      render: (p) => <EstadoPedidoChip estado={p.estado} />,
    },
  ];

  return (
    <Box>
      <PageHeader title="Pedidos" subtitle="Historial y seguimiento de los pedidos del local" />

      {/* Filtros */}
      <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1.5, alignItems: "center", mb: 2 }}>
        <ToggleButtonGroup
          size="small"
          value={preset}
          exclusive
          onChange={(_, v) => v && cambiarPreset(v)}
        >
          <ToggleButton value="HOY">Hoy</ToggleButton>
          <ToggleButton value="AYER">Ayer</ToggleButton>
          <ToggleButton value="7D">Últimos 7 días</ToggleButton>
          <ToggleButton value="PERSONALIZADO">Rango</ToggleButton>
        </ToggleButtonGroup>

        {preset === "PERSONALIZADO" && (
          <>
            <TextField
              type="date"
              size="small"
              label="Desde"
              value={desde}
              onChange={(e) => setDesde(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              type="date"
              size="small"
              label="Hasta"
              value={hasta}
              onChange={(e) => setHasta(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </>
        )}

        <Box sx={{ flex: 1, minWidth: 8 }} />

        <ToggleButtonGroup size="small" value={modalidad} exclusive onChange={(_, v) => v && setModalidad(v)}>
          <ToggleButton value="TODAS">Todas</ToggleButton>
          <ToggleButton value={Modalidad.RETIRAR}>Retirar</ToggleButton>
          <ToggleButton value={Modalidad.DELIVERY}>Delivery</ToggleButton>
        </ToggleButtonGroup>

        <TextField
          size="small"
          placeholder="Buscar por pedido o cliente"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          sx={{ minWidth: 220 }}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            },
          }}
        />
      </Box>

      <Tabs
        value={tab}
        onChange={(_, v) => setTab(v)}
        sx={{ mb: 2, minHeight: 40, "& .MuiTab-root": { minHeight: 40 } }}
      >
        <Tab value="TODOS" label={<ConteoTab label="Todos" n={conteos.TODOS} />} />
        <Tab value="ACTIVOS" label={<ConteoTab label="Activos" n={conteos.ACTIVOS} />} />
        <Tab value="ENTREGADOS" label={<ConteoTab label="Entregados" n={conteos.ENTREGADOS} />} />
        <Tab value="CANCELADOS" label={<ConteoTab label="Cancelados" n={conteos.CANCELADOS} />} />
      </Tabs>

      <DataTable
        columns={columnas}
        rows={filtrados}
        getRowId={(p) => p.id}
        loading={cargando}
        error={error}
        onRetry={cargar}
        onRowClick={setSeleccionado}
        emptyTitle="No hay pedidos en este rango"
        emptyDescription="Probá con otro período o modalidad, o esperá a que entre el próximo pedido."
        emptyAction={
          preset !== "HOY" ? (
            <Button variant="outlined" onClick={() => cambiarPreset("HOY")}>
              Ver hoy
            </Button>
          ) : undefined
        }
      />

      <PedidoDetalleDrawer
        pedido={seleccionado}
        onClose={() => setSeleccionado(null)}
        onCambiado={(p) => {
          upsert(p);
          snackbar.showSnackbar(`Pedido #${p.id} actualizado`, "success");
        }}
        onError={(mensaje) => snackbar.showSnackbar(mensaje, "error")}
      />

      <FeedbackSnackbar
        open={snackbar.open}
        message={snackbar.message}
        severity={snackbar.severity}
        onClose={snackbar.hideSnackbar}
      />
    </Box>
  );
}

function ConteoTab({ label, n }: { label: string; n: number }) {
  return (
    <Box sx={{ display: "flex", alignItems: "center", gap: 0.75 }}>
      <span>{label}</span>
      {n > 0 && (
        <Box
          component="span"
          sx={{
            minWidth: 20,
            height: 20,
            px: 0.5,
            borderRadius: "10px",
            bgcolor: "action.selected",
            color: "text.secondary",
            fontSize: 12,
            fontWeight: 700,
            display: "inline-flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          {n}
        </Box>
      )}
    </Box>
  );
}
