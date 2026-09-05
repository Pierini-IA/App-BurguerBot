"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormControlLabel,
  IconButton,
  InputAdornment,
  Skeleton,
  Switch,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import {
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  ContentCopy as ContentCopyIcon,
} from "@mui/icons-material";
import { PageHeader, EmptyState, FeedbackSnackbar } from "@/components/shared";
import { configuracionApi } from "@/lib/api/configuracion";
import { getErrorMessage } from "@/lib/api/axios";
import { useLocal } from "@/lib/context/LocalContext";
import { useSnackbar } from "@/lib/hooks/useSnackbar";
import { env } from "@/lib/config/env";
import type { MiConfiguracion } from "@/types/local";

/** Estado editable del formulario. Los `""` de tokens = "no cambiar". */
interface FormState {
  horaApertura: string;
  horaCierre: string;
  permiteTakeAway: boolean;
  permiteDelivery: boolean;
  permiteReservas: boolean;
  waPhoneId: string;
  waAccessToken: string;
  fbPageId: string;
  fbPageAccessToken: string;
  igToken: string;
  impresionActiva: boolean;
  urlWebhookImpresora: string;
  urlWebhookNotificaciones: string;
}

function aFormState(c: MiConfiguracion): FormState {
  return {
    horaApertura: (c.horaApertura ?? "20:00:00").slice(0, 5),
    horaCierre: (c.horaCierre ?? "23:00:00").slice(0, 5),
    permiteTakeAway: c.permiteTakeAway ?? true,
    permiteDelivery: c.permiteDelivery ?? false,
    permiteReservas: c.permiteReservas ?? false,
    waPhoneId: c.waPhoneId ?? "",
    waAccessToken: "",
    fbPageId: c.fbPageId ?? "",
    fbPageAccessToken: "",
    igToken: "",
    impresionActiva: c.impresionActiva ?? false,
    urlWebhookImpresora: c.urlWebhookImpresora ?? "",
    urlWebhookNotificaciones: c.urlWebhookNotificaciones ?? "",
  };
}

export default function ConfiguracionPage() {
  const { telefonoLocal, hasFeature } = useLocal();
  const snackbar = useSnackbar();

  const [config, setConfig] = useState<MiConfiguracion | null>(null);
  const [form, setForm] = useState<FormState | null>(null);
  const [inicial, setInicial] = useState<FormState | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [guardando, setGuardando] = useState(false);
  const [verTokens, setVerTokens] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const c = await configuracionApi.get();
      setConfig(c);
      const fs = aFormState(c);
      setForm(fs);
      setInicial(fs);
    } catch (e) {
      setError(getErrorMessage(e) || "No se pudo cargar la configuración");
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const modificado = useMemo(
    () => !!form && !!inicial && JSON.stringify(form) !== JSON.stringify(inicial),
    [form, inicial]
  );

  const set = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setForm((f) => (f ? { ...f, [key]: value } : f));
  };

  const webhookUrl = useMemo(() => {
    if (!telefonoLocal) return "";
    const base = env.apiUrl.replace(/\/api\/?$/, "");
    return `${base}/api/webhooks/meta/whatsapp/${telefonoLocal}`;
  }, [telefonoLocal]);

  const copiar = async (texto: string) => {
    try {
      await navigator.clipboard.writeText(texto);
      snackbar.showSnackbar("Copiado al portapapeles", "success");
    } catch {
      snackbar.showSnackbar("No se pudo copiar", "error");
    }
  };

  const guardar = async () => {
    if (!form) return;
    setGuardando(true);
    try {
      // Solo se mandan los tokens si el usuario escribió algo.
      const cambios: Partial<MiConfiguracion> = {
        horaApertura: `${form.horaApertura}:00`,
        horaCierre: `${form.horaCierre}:00`,
        permiteTakeAway: form.permiteTakeAway,
        permiteDelivery: form.permiteDelivery,
        permiteReservas: form.permiteReservas,
        waPhoneId: form.waPhoneId,
        fbPageId: form.fbPageId,
        impresionActiva: form.impresionActiva,
        urlWebhookImpresora: form.urlWebhookImpresora,
        urlWebhookNotificaciones: form.urlWebhookNotificaciones,
        ...(form.waAccessToken ? { waAccessToken: form.waAccessToken } : {}),
        ...(form.fbPageAccessToken ? { fbPageAccessToken: form.fbPageAccessToken } : {}),
        ...(form.igToken ? { igToken: form.igToken } : {}),
      };
      const actualizada = await configuracionApi.update(cambios);
      setConfig(actualizada);
      const fs = aFormState(actualizada);
      setForm(fs);
      setInicial(fs);
      snackbar.showSnackbar("Configuración guardada", "success");
    } catch (e) {
      snackbar.showSnackbar(getErrorMessage(e) || "No se pudo guardar", "error");
    } finally {
      setGuardando(false);
    }
  };

  if (error) {
    return (
      <Box>
        <PageHeader title="Configuración" subtitle="Ajustes del local" />
        <EmptyState
          title="No se pudo cargar la configuración"
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

  const tokenAdorno = (
    <InputAdornment position="end">
      <IconButton
        aria-label={verTokens ? "Ocultar tokens" : "Ver tokens"}
        onClick={() => setVerTokens((v) => !v)}
        edge="end"
      >
        {verTokens ? <VisibilityOffIcon /> : <VisibilityIcon />}
      </IconButton>
    </InputAdornment>
  );

  const tokenHelper = (configurado?: boolean) =>
    configurado ? "Ya hay un token guardado. Dejalo vacío para no cambiarlo." : "Todavía no hay token cargado.";

  return (
    <Box sx={{ pb: 10 }}>
      <PageHeader title="Configuración" subtitle="Horarios, modalidades y conexión con Meta" />

      {cargando || !form ? (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          {[0, 1, 2].map((i) => (
            <Skeleton key={i} variant="rounded" height={180} />
          ))}
        </Box>
      ) : (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3, maxWidth: 720 }}>
          <Seccion titulo="Horarios de atención">
            <Box sx={{ display: "flex", gap: 2, flexWrap: "wrap" }}>
              <TextField
                type="time"
                label="Abre"
                value={form.horaApertura}
                onChange={(e) => set("horaApertura", e.target.value)}
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                type="time"
                label="Cierra"
                value={form.horaCierre}
                onChange={(e) => set("horaCierre", e.target.value)}
                slotProps={{ inputLabel: { shrink: true } }}
              />
            </Box>
          </Seccion>

          <Seccion titulo="Modalidades" descripcion="Cómo puede pedir el cliente.">
            <FormControlLabel
              control={<Switch checked={form.permiteTakeAway} onChange={(e) => set("permiteTakeAway", e.target.checked)} />}
              label="Retiro en el local"
            />
            <FormControlLabel
              control={<Switch checked={form.permiteDelivery} onChange={(e) => set("permiteDelivery", e.target.checked)} />}
              label="Delivery"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={form.permiteReservas}
                  onChange={(e) => set("permiteReservas", e.target.checked)}
                  disabled={!hasFeature("SISTEMA_RESERVAS")}
                />
              }
              label={
                hasFeature("SISTEMA_RESERVAS") ? "Reservas de mesa" : "Reservas de mesa (requiere plan Estándar o superior)"
              }
            />
          </Seccion>

          <Seccion
            titulo="WhatsApp (Meta)"
            descripcion="Credenciales del número de WhatsApp Business con el que responde el bot."
          >
            {!hasFeature("BOT_WHATSAPP") && (
              <Alert severity="info" sx={{ mb: 1 }}>
                El asistente por WhatsApp está disponible desde el plan Estándar. Podés cargar las credenciales
                igual, pero el bot no va a responder hasta activar el plan.
              </Alert>
            )}
            <TextField
              label="Phone Number ID"
              value={form.waPhoneId}
              onChange={(e) => set("waPhoneId", e.target.value)}
              fullWidth
              helperText="El ID del número, no el número en sí. Lo da Meta en la app de WhatsApp Business."
            />
            <TextField
              label="Access token"
              type={verTokens ? "text" : "password"}
              value={form.waAccessToken}
              onChange={(e) => set("waAccessToken", e.target.value)}
              fullWidth
              placeholder={config?.waConfigurado ? "•••••••••••• (guardado)" : ""}
              helperText={tokenHelper(config?.waConfigurado)}
              slotProps={{ input: { endAdornment: tokenAdorno } }}
            />

            <Box
              sx={{
                mt: 1,
                p: 2,
                borderRadius: 2,
                bgcolor: "action.hover",
                border: "1px solid",
                borderColor: "divider",
              }}
            >
              <Typography variant="subtitle2" gutterBottom>
                Para configurar el webhook en Meta
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                Pegá esta URL como <em>Callback URL</em> del webhook de WhatsApp en tu app de Meta, y usá el
                verify token que configuraste en el servidor (<code>META_WEBHOOK_VERIFY_TOKEN</code>).
              </Typography>
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <TextField
                  value={webhookUrl}
                  size="small"
                  fullWidth
                  slotProps={{ input: { readOnly: true, sx: { fontFamily: "monospace", fontSize: 13 } } }}
                />
                <Tooltip title="Copiar">
                  <IconButton onClick={() => copiar(webhookUrl)} aria-label="Copiar URL del webhook">
                    <ContentCopyIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Box>
            </Box>
          </Seccion>

          <Seccion
            titulo="Instagram y Facebook (opcional)"
            descripcion="Para que el bot responda comentarios en publicaciones."
          >
            <TextField
              label="Instagram — Access token"
              type={verTokens ? "text" : "password"}
              value={form.igToken}
              onChange={(e) => set("igToken", e.target.value)}
              fullWidth
              placeholder={config?.igConfigurado ? "•••••••••••• (guardado)" : ""}
              helperText={tokenHelper(config?.igConfigurado)}
              slotProps={{ input: { endAdornment: tokenAdorno } }}
            />
            <TextField
              label="Facebook — Page ID"
              value={form.fbPageId}
              onChange={(e) => set("fbPageId", e.target.value)}
              fullWidth
            />
            <TextField
              label="Facebook — Page Access token"
              type={verTokens ? "text" : "password"}
              value={form.fbPageAccessToken}
              onChange={(e) => set("fbPageAccessToken", e.target.value)}
              fullWidth
              placeholder={config?.fbConfigurado ? "•••••••••••• (guardado)" : ""}
              helperText={tokenHelper(config?.fbConfigurado)}
              slotProps={{ input: { endAdornment: tokenAdorno } }}
            />
          </Seccion>

          <Seccion
            titulo="Impresión y notificaciones"
            descripcion="Integración con impresora de comandas o webhooks externos."
          >
            <FormControlLabel
              control={<Switch checked={form.impresionActiva} onChange={(e) => set("impresionActiva", e.target.checked)} />}
              label="Impresión automática de comandas"
            />
            <TextField
              label="Webhook de impresora"
              value={form.urlWebhookImpresora}
              onChange={(e) => set("urlWebhookImpresora", e.target.value)}
              fullWidth
              placeholder="https://..."
            />
            <TextField
              label="Webhook de notificaciones"
              value={form.urlWebhookNotificaciones}
              onChange={(e) => set("urlWebhookNotificaciones", e.target.value)}
              fullWidth
              placeholder="https://..."
            />
          </Seccion>
        </Box>
      )}

      {/* Barra de guardado */}
      {form && (
        <Box
          sx={{
            position: "fixed",
            left: { xs: 0, md: 260 },
            right: 0,
            bottom: 0,
            px: { xs: 2, sm: 3, md: 4 },
            py: 2,
            bgcolor: "background.paper",
            borderTop: "1px solid",
            borderColor: "divider",
            display: "flex",
            justifyContent: "flex-end",
            gap: 2,
            zIndex: (t) => t.zIndex.appBar - 1,
          }}
        >
          <Button disabled={!modificado || guardando} onClick={() => inicial && setForm(inicial)}>
            Descartar
          </Button>
          <Button
            variant="contained"
            disabled={!modificado || guardando}
            onClick={guardar}
            startIcon={guardando ? <CircularProgress size={16} color="inherit" /> : undefined}
          >
            Guardar cambios
          </Button>
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

function Seccion({
  titulo,
  descripcion,
  children,
}: {
  titulo: string;
  descripcion?: string;
  children: React.ReactNode;
}) {
  return (
    <Box sx={{ p: 3, borderRadius: 3, border: "1px solid", borderColor: "divider" }}>
      <Typography variant="subtitle1" fontWeight={700}>
        {titulo}
      </Typography>
      {descripcion && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {descripcion}
        </Typography>
      )}
      <Box sx={{ display: "flex", flexDirection: "column", gap: 2, mt: descripcion ? 0 : 2 }}>{children}</Box>
    </Box>
  );
}
