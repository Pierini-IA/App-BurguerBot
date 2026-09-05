/**
 * Context de Multi-Tenancy.
 *
 * Expone el local del usuario autenticado (ADMIN / COCINA), su plan y las
 * funcionalidades habilitadas. Los datos se piden una sola vez a
 * `GET /api/local/mi-local` al montar el provider.
 *
 * Para SUPERADMIN no hay "local propio": el context queda sin `miLocal` y
 * `hasFeature` devuelve `true` (ve todo).
 */

"use client";

import React, { createContext, useCallback, useContext, useEffect, useState, ReactNode } from "react";
import { Box, Alert, Button, CircularProgress } from "@mui/material";
import { miLocalApi } from "@/lib/api/miLocal";
import { getErrorMessage } from "@/lib/api/axios";
import { MiLocal, PlanSuscripcion } from "@/types/local";
import { useAuth } from "@/lib/hooks/useAuth";
import { Rol } from "@/types/usuario";

interface LocalContextValue {
  /** Datos del local del usuario. `null` para SUPERADMIN o mientras carga. */
  miLocal: MiLocal | null;
  /** `true` mientras se resuelve `mi-local`. */
  loading: boolean;
  /** Mensaje de error si falló la carga. */
  error: string | null;
  /** Vuelve a pedir `mi-local`. */
  refetch: () => void;

  // Atajos derivados (compatibilidad con el uso anterior del context)
  telefonoLocal: string | null;
  nombreLocal: string | null;
  localId: number | null;
  planSuscripcion: PlanSuscripcion | null;
  planActivo: boolean;

  /**
   * Indica si el local tiene habilitada una `Feature` del backend.
   * @param feature nombre del enum `Feature` (ej. "SISTEMA_RESERVAS", "REPORTES_AVANZADOS")
   */
  hasFeature: (feature: string) => boolean;
}

const LocalContext = createContext<LocalContextValue | null>(null);

interface LocalProviderProps {
  children: ReactNode;
  /**
   * Si es `true` (default) muestra un loader a pantalla completa mientras
   * carga. Si es `false`, renderiza los children de una y estos deben
   * tolerar `miLocal === null`.
   */
  bloquearHastaCargar?: boolean;
}

export const LocalProvider: React.FC<LocalProviderProps> = ({ children, bloquearHastaCargar = true }) => {
  const { user } = useAuth();
  const esSuperadmin = user?.rol === Rol.SUPERADMIN;
  const debeCargar = !!user && !esSuperadmin;

  const [miLocal, setMiLocal] = useState<MiLocal | null>(null);
  const [loading, setLoading] = useState(debeCargar);
  const [error, setError] = useState<string | null>(null);

  const cargar = useCallback(async () => {
    if (!debeCargar) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const data = await miLocalApi.get();
      setMiLocal(data);
    } catch (e) {
      setError(getErrorMessage(e) || "No se pudo cargar la información del local");
    } finally {
      setLoading(false);
    }
  }, [debeCargar]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const hasFeature = useCallback(
    (feature: string): boolean => {
      if (esSuperadmin) return true;
      if (!miLocal || !miLocal.planActivo) return false;
      return miLocal.features.includes(feature);
    },
    [esSuperadmin, miLocal]
  );

  const value: LocalContextValue = {
    miLocal,
    loading,
    error,
    refetch: cargar,
    telefonoLocal: miLocal?.telefono ?? user?.telefonoLocal ?? null,
    nombreLocal: miLocal?.nombre ?? null,
    localId: miLocal?.localId ?? null,
    planSuscripcion: miLocal?.planSuscripcion ?? null,
    planActivo: miLocal?.planActivo ?? false,
    hasFeature,
  };

  if (bloquearHastaCargar && loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "60vh" }}>
        <CircularProgress size={48} />
      </Box>
    );
  }

  if (bloquearHastaCargar && error) {
    return (
      <Box sx={{ display: "flex", flexDirection: "column", gap: 2, alignItems: "center", mt: 8 }}>
        <Alert severity="error">{error}</Alert>
        <Button variant="outlined" onClick={cargar}>
          Reintentar
        </Button>
      </Box>
    );
  }

  return <LocalContext.Provider value={value}>{children}</LocalContext.Provider>;
};

/**
 * Hook para acceder al context de Local.
 * @throws si se usa fuera de `LocalProvider`
 */
export const useLocal = (): LocalContextValue => {
  const context = useContext(LocalContext);
  if (!context) {
    throw new Error("useLocal debe usarse dentro de <LocalProvider>");
  }
  return context;
};

/**
 * HOC para proteger un componente detrás de una feature del plan.
 */
export const withFeature = <P extends object>(
  Component: React.ComponentType<P>,
  requiredFeature: string,
  fallback?: React.ReactNode
) => {
  const Wrapped = (props: P) => {
    const { hasFeature, planSuscripcion } = useLocal();

    if (!hasFeature(requiredFeature)) {
      if (fallback) return <>{fallback}</>;
      return (
        <Box sx={{ p: 4, textAlign: "center" }}>
          <Alert severity="info">
            Esta funcionalidad requiere un plan superior. Plan actual: <strong>{planSuscripcion ?? "—"}</strong>
          </Alert>
        </Box>
      );
    }

    return <Component {...props} />;
  };
  Wrapped.displayName = `withFeature(${Component.displayName || Component.name || "Component"})`;
  return Wrapped;
};
