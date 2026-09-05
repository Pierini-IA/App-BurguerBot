import axios from "axios";
import { env } from "@/lib/config/env";
import { defaults } from "@/lib/config/defaults";
import { ApiError } from "@/types/api";

/**
 * Lee el `telefonoLocal` del usuario autenticado desde el store persistido
 * de Zustand (`auth-storage` en localStorage).
 *
 * Se lee de localStorage en vez de importar el store para evitar un ciclo
 * de imports (`authStore` -> `authApi` -> `axios`).
 *
 * @returns el teléfono del local, o `null` si el usuario es SUPERADMIN / no hay sesión.
 */
export const getTelefonoLocal = (): string | null => {
  if (typeof window === "undefined") return null;
  try {
    const raw = localStorage.getItem("auth-storage");
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    return parsed?.state?.user?.telefonoLocal ?? null;
  } catch {
    return null;
  }
};

/**
 * Cliente Axios configurado con:
 * - Base URL del backend
 * - Timeout de 30 segundos
 * - Interceptor de request para agregar JWT
 * - Interceptor de response para manejar errores comunes
 */
export const apiClient = axios.create({
  baseURL: env.apiUrl,
  timeout: defaults.requestTimeout,
  headers: {
    "Content-Type": "application/json",
  },
});

// Interceptor de Request - Agregar JWT + telefonoLocal (multi-tenant)
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(defaults.storage.token);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // El backend exige `telefonoLocal` como query param en casi todos los
    // endpoints de /api/admin/** y /api/local/**. Lo inyectamos acá para que
    // los api clients no tengan que pasarlo a mano en cada llamada.
    const url = config.url ?? "";
    const esEndpointDeSuperadmin = url.includes("/superadmin");
    const esEndpointDeAuth = url.includes("/auth/");
    const yaTieneParam =
      config.params &&
      ("telefonoLocal" in config.params) &&
      config.params.telefonoLocal;

    if (!esEndpointDeSuperadmin && !esEndpointDeAuth && !yaTieneParam) {
      const telefonoLocal = getTelefonoLocal();
      if (telefonoLocal) {
        config.params = { ...(config.params ?? {}), telefonoLocal };
      }
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor de Response - Manejar errores comunes
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // 401 Unauthorized: Token expirado o inválido
    if (error.response?.status === 401) {
      console.error("❌ Token expirado o inválido");
      localStorage.removeItem(defaults.storage.token);
      localStorage.removeItem(defaults.storage.user);

      // Redirigir a login solo si no estamos ya ahí
      if (typeof window !== "undefined" && !window.location.pathname.includes("/login")) {
        window.location.href = "/login";
      }
    }

    // 403 Forbidden: Falta de permisos o feature no disponible
    if (error.response?.status === 403) {
      const apiError = error.response.data as ApiError;
      console.error("❌ Acceso denegado:", apiError.message);

      // Si es un error de feature no disponible, mostrar información
      if (apiError.feature) {
        console.warn(
          `Feature requerida: ${apiError.feature} | Plan actual: ${
            apiError.planActual
          } | Planes requeridos: ${apiError.planesRequeridos?.join(", ")}`
        );
      }
    }

    // 409 Conflict: Duplicados
    if (error.response?.status === 409) {
      console.error("❌ Conflicto:", error.response.data.message);
    }

    // 422 Unprocessable Entity: Lógica de negocio fallida
    if (error.response?.status === 422) {
      console.error("❌ Error de lógica de negocio:", error.response.data.message);
    }

    return Promise.reject(error);
  }
);

/**
 * Helper para extraer mensaje de error de la respuesta.
 */
export const getErrorMessage = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const apiError = error.response?.data as ApiError;
    return apiError?.message || error.message || "Error desconocido";
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "Error desconocido";
};

/**
 * Helper para verificar si un error es de autenticación.
 */
export const isAuthError = (error: unknown): boolean => {
  if (axios.isAxiosError(error)) {
    return error.response?.status === 401;
  }
  return false;
};

/**
 * Helper para verificar si un error es de feature no disponible.
 */
export const isFeatureError = (error: unknown): boolean => {
  if (axios.isAxiosError(error)) {
    const apiError = error.response?.data as ApiError;
    return error.response?.status === 403 && !!apiError?.feature;
  }
  return false;
};

/**
 * Helper para verificar si un error es de conflicto (duplicado).
 */
export const isConflictError = (error: unknown): boolean => {
  if (axios.isAxiosError(error)) {
    return error.response?.status === 409;
  }
  return false;
};
