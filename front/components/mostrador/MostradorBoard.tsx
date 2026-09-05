"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { Box, Button, Chip, CircularProgress, IconButton, Tooltip, Typography } from "@mui/material";
import { Print as PrintIcon, Logout as LogoutIcon, DeliveryDining, Storefront } from "@mui/icons-material";
import { cocinaApi } from "@/lib/api/cocina";
import { getErrorMessage } from "@/lib/api/axios";
import { useAuth } from "@/lib/hooks/useAuth";
import { useLocal } from "@/lib/context/LocalContext";
import { usePedidosSocket } from "@/lib/hooks/usePedidosSocket";
import { useSnackbar } from "@/lib/hooks/useSnackbar";
import { EstadoPedidoChip, FeedbackSnackbar } from "@/components/shared";
import { EstadoPedido, Modalidad, OrigenPedido, type Pedido } from "@/types/api";
import { cantidadItems, horaCorta, precioAR, ETIQUETA_MODALIDAD } from "@/lib/pedidos/formato";
import { ESTADOS_ACTIVOS, accionParaEstado, ejecutarAccion, type AccionTicket } from "@/lib/cocina/acciones";
import { TicketComanda } from "./TicketComanda";

function porHora(a: Pedido, b: Pedido) {
  return new Date(a.horaPedido).getTime() - new Date(b.horaPedido).getTime();
}

export function MostradorBoard() {
  const { telefonoLocal, nombreLocal } = useLocal();
  const { logout } = useAuth();
  const router = useRouter();
  const snackbar = useSnackbar();

  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [procesando, setProcesando] = useState<ReadonlySet<number>>(new Set());
  const [imprimir, setImprimir] = useState<{ pedido: Pedido; reimpresion: boolean } | null>(null);
  const yaImpresos = useRef<Set<number>>(new Set());

  const cerrarSesion = () => {
    logout();
    router.push("/login");
  };

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

  const aplicar = useCallback((p: Pedido) => {
    setPedidos((prev) => {
      if (!ESTADOS_ACTIVOS.has(p.estado)) return prev.filter((x) => x.id !== p.id);
      const existe = prev.some((x) => x.id === p.id);
      return (existe ? prev.map((x) => (x.id === p.id ? p : x)) : [...prev, p]).sort(porHora);
    });
  }, []);

  const { conectado } = usePedidosSocket({
    telefonoLocal,
    onNuevoPedido: aplicar,
    onActualizacion: aplicar,
    onModificado: aplicar,
    onCancelado: (p) => setPedidos((prev) => prev.filter((x) => x.id !== p.id)),
  });

  const estabaConectado = useRef(conectado);
  useEffect(() => {
    if (conectado && !estabaConectado.current) void cargar();
    estabaConectado.current = conectado;
  }, [conectado, cargar]);

  useEffect(() => {
    if (conectado) return;
    const id = window.setInterval(() => void cargar(), 15000);
    return () => window.clearInterval(id);
  }, [conectado, cargar]);

  // Dispara la impresión una vez que la comanda está en el DOM.
  useEffect(() => {
    if (!imprimir) return;
    const t = window.setTimeout(() => {
      window.print();
      yaImpresos.current.add(imprimir.pedido.id);
      setImprimir(null);
    }, 60);
    return () => window.clearTimeout(t);
  }, [imprimir]);

  const pedirImpresion = (pedido: Pedido) => {
    setImprimir({ pedido, reimpresion: yaImpresos.current.has(pedido.id) });
  };

  const avanzar = async (pedido: Pedido, accion: AccionTicket) => {
    setProcesando((prev) => new Set(prev).add(pedido.id));
    try {
      const actualizado = await ejecutarAccion(pedido.id, accion);
      aplicar(actualizado);
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo actualizar el pedido", "error");
    } finally {
      setProcesando((prev) => {
        const n = new Set(prev);
        n.delete(pedido.id);
        return n;
      });
    }
  };

  return (
    <Box sx={{ minHeight: "100dvh", bgcolor: "background.default", display: "flex", flexDirection: "column" }}>
      {/* Barra */}
      <Box
        sx={{
          px: { xs: 2, sm: 3 },
          py: 1.5,
          bgcolor: "background.paper",
          borderBottom: "1px solid",
          borderColor: "divider",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: 2,
        }}
      >
        <Box sx={{ display: "flex", alignItems: "baseline", gap: 1.25, minWidth: 0 }}>
          <Typography variant="h6" fontWeight={700} noWrap>
            {nombreLocal ?? "Mostrador"}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Mostrador
          </Typography>
        </Box>
        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          <Chip
            size="small"
            label={conectado ? "En vivo" : "Sin conexión"}
            color={conectado ? "success" : "default"}
            variant="outlined"
          />
          <Tooltip title="Cerrar sesión">
            <IconButton onClick={cerrarSesion} aria-label="Cerrar sesión">
              <LogoutIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* Contenido */}
      <Box sx={{ flex: 1, p: { xs: 2, sm: 3 } }}>
        {cargando ? (
          <Box sx={{ display: "flex", justifyContent: "center", py: 10 }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Box sx={{ textAlign: "center", py: 8 }}>
            <Typography color="error" sx={{ mb: 2 }}>
              {error}
            </Typography>
            <Button variant="outlined" onClick={cargar}>
              Reintentar
            </Button>
          </Box>
        ) : pedidos.length === 0 ? (
          <Box sx={{ textAlign: "center", py: 10 }}>
            <Typography variant="h6" gutterBottom>
              No hay pedidos en curso
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Los pedidos aparecen acá apenas se confirman.
            </Typography>
          </Box>
        ) : (
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: { xs: "1fr", sm: "repeat(2, 1fr)", lg: "repeat(3, 1fr)" },
              gap: 2,
            }}
          >
            {pedidos.map((pedido) => {
              const accion = accionParaEstado(pedido);
              const listo = pedido.estado === EstadoPedido.LISTO || pedido.estado === EstadoPedido.EN_CAMINO;
              return (
                <Box
                  key={pedido.id}
                  data-testid={`pedido-${pedido.id}`}
                  sx={{
                    p: 2.5,
                    borderRadius: 3,
                    border: "1px solid",
                    borderColor: listo ? "primary.main" : "divider",
                    bgcolor: "background.paper",
                    display: "flex",
                    flexDirection: "column",
                    gap: 1.5,
                  }}
                >
                  <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                    <Box>
                      <Typography variant="h6" fontWeight={700} sx={{ fontVariantNumeric: "tabular-nums" }}>
                        #{pedido.id}
                      </Typography>
                      <Box sx={{ display: "flex", alignItems: "center", gap: 0.5, color: "text.secondary" }}>
                        {pedido.modalidad === Modalidad.DELIVERY ? (
                          <DeliveryDining fontSize="small" />
                        ) : (
                          <Storefront fontSize="small" />
                        )}
                        <Typography variant="body2">
                          {ETIQUETA_MODALIDAD[pedido.modalidad]} · {horaCorta(pedido.horaPedido)}
                        </Typography>
                      </Box>
                    </Box>
                    <EstadoPedidoChip estado={pedido.estado} />
                  </Box>

                  <Box>
                    <Typography variant="body2" fontWeight={600}>
                      {pedido.cliente?.nombre ?? "Sin datos"}
                      {pedido.origenPedido === OrigenPedido.BOT && (
                        <Typography component="span" variant="caption" color="success.main">
                          {"  ·  WhatsApp"}
                        </Typography>
                      )}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {cantidadItems(pedido)} ítem{cantidadItems(pedido) !== 1 ? "s" : ""} · {precioAR.format(pedido.total ?? 0)}
                    </Typography>
                  </Box>

                  <Box sx={{ display: "flex", gap: 1, mt: "auto" }}>
                    <Button
                      variant="outlined"
                      startIcon={<PrintIcon />}
                      onClick={() => pedirImpresion(pedido)}
                      sx={{ flex: 1 }}
                    >
                      Imprimir
                    </Button>
                    {accion && (
                      <Button
                        variant="contained"
                        disabled={procesando.has(pedido.id)}
                        onClick={() => avanzar(pedido, accion.accion)}
                        sx={{ flex: 1 }}
                      >
                        {procesando.has(pedido.id) ? (
                          <CircularProgress size={18} color="inherit" />
                        ) : (
                          accion.label
                        )}
                      </Button>
                    )}
                  </Box>
                </Box>
              );
            })}
          </Box>
        )}
      </Box>

      {/* Comanda oculta en pantalla (0 de alto, overflow oculto). En impresión, la
          regla `.comanda-print` de globals.css la reposiciona y la hace visible. */}
      {imprimir && (
        <Box sx={{ height: 0, overflow: "hidden" }} aria-hidden>
          <TicketComanda pedido={imprimir.pedido} nombreLocal={nombreLocal} reimpresion={imprimir.reimpresion} />
        </Box>
      )}

      <FeedbackSnackbar
        open={snackbar.open}
        message={snackbar.message}
        severity={snackbar.severity}
        onClose={snackbar.hideSnackbar}
      />
    </Box>
  );
}
