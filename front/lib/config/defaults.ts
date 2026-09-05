/**
 * Configuración por defecto de la aplicación
 */
export const defaults = {
  // Paginación
  pageSize: 10,
  maxPageSize: 100,

  // Timeouts
  requestTimeout: 30000, // 30 segundos
  websocketReconnectDelay: 3000, // 3 segundos

  // Validaciones
  minPasswordLength: 6,
  maxFileSize: 5 * 1024 * 1024, // 5MB

  // UI
  snackbarAutoHideDuration: 5000, // 5 segundos
  debounceDelay: 300, // 300ms para búsquedas

  // Timeouts para pedidos
  pedidoAlertaMinutos: 20, // Alerta si el pedido lleva más de 20 min

  // Refresh intervals
  pollingInterval: 10000, // 10 segundos para polling de respaldo

  // LocalStorage keys
  storage: {
    token: "dio_burger_token",
    user: "dio_burger_user",
    theme: "dio_burger_theme",
    preferences: "dio_burger_preferences",
  },
};
