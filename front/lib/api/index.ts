/**
 * Exportación centralizada de todos los servicios API.
 * Facilita imports limpios en componentes.
 *
 * @example
 * import { productosApi, pedidosApi, reportesApi } from '@/lib/api';
 */

export { authApi } from "./auth";
export { productosApi } from "./productos";
export { ingredientesApi } from "./ingredientes";
export { categoriasApi } from "./categorias";
export { extrasApi } from "./extras";
export { localesApi } from "./locales";
export { miLocalApi } from "./miLocal";
export { configuracionApi } from "./configuracion";
export { usuariosApi } from "./usuarios";
export { mesasApi } from "./mesas";
export { pedidosApi } from "./pedidos";
export { reservasApi } from "./reservas";
export { reportesApi } from "./reportes";
export { cocinaApi } from "./cocina";
export { estadisticasApi } from "./estadisticas";

// Helpers de axios
export { apiClient, getErrorMessage, isAuthError, isFeatureError, isConflictError } from "./axios";
