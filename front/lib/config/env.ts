/**
 * Configuración centralizada de variables de entorno
 * Siempre usar este archivo para acceder a las variables de entorno
 */
export const env = {
  apiUrl: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api",
  wsUrl: process.env.NEXT_PUBLIC_WS_URL || "http://localhost:8080",
  whatsappNumber: process.env.NEXT_PUBLIC_WHATSAPP_NUMBER || "549349366512",
  appName: process.env.NEXT_PUBLIC_APP_NAME || "Dio Burger",
  appVersion: process.env.NEXT_PUBLIC_APP_VERSION || "1.0.0",
  enablePwa: process.env.NEXT_PUBLIC_ENABLE_PWA === "true",
  enableDarkMode: process.env.NEXT_PUBLIC_ENABLE_DARK_MODE === "true",
};
