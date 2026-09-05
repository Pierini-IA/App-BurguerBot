/**
 * Planes de suscripción disponibles
 */
export enum PlanSuscripcion {
  BASICO = "BASICO",
  ESTANDAR = "ESTANDAR",
  PREMIUM = "PREMIUM",
}

/**
 * Interface de Local (coincide con backend)
 */
export interface Local {
  id: number;
  nombre: string;
  direccion: string;
  telefono: string;
  planSuscripcion?: PlanSuscripcion;
  planActivo?: boolean;
  fechaInicioPlan?: string; // ISO date string
  fechaFinPlan?: string | null; // ISO date string o null si no expira
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Información del local del usuario autenticado.
 * Coincide con `MiLocalDTO` del backend (`GET /api/local/mi-local`).
 * `features` son los nombres de las `Feature` habilitadas según el plan.
 */
export interface MiLocal {
  localId: number;
  nombre: string;
  direccion: string;
  telefono: string;
  planSuscripcion: PlanSuscripcion;
  planNombre: string;
  planActivo: boolean;
  fechaFinPlan?: string | null;
  features: string[];
  horaApertura?: string | null;
  horaCierre?: string | null;
  permiteTakeAway?: boolean | null;
  permiteDelivery?: boolean | null;
  permiteReservas?: boolean | null;
  impresionActiva?: boolean | null;
  whatsappConfigurado?: boolean | null;
}

/**
 * Configuración operativa del local del usuario autenticado.
 * Coincide con `ConfiguracionLocalDTO` del backend
 * (`GET`/`PUT /api/local/mi-local/configuracion`).
 *
 * Los tokens de Meta NO viajan en el GET: llegan los flags `*Configurado`.
 * En el PUT, un token vacío significa "no cambiar".
 */
export interface MiConfiguracion {
  horaApertura?: string | null;
  horaCierre?: string | null;
  intervaloMinutosPedidos?: number | null;
  maxPedidosPorIntervalo?: number | null;
  horaAperturaReservas?: string | null;
  horaCierreReservas?: string | null;
  intervaloMinutosReservas?: number | null;
  maxReservasPorIntervalo?: number | null;
  minutosAnticipacionCancelacion?: number | null;
  permiteDelivery?: boolean | null;
  permiteTakeAway?: boolean | null;
  permiteReservas?: boolean | null;
  impresionActiva?: boolean | null;
  urlWebhookImpresora?: string | null;
  urlWebhookNotificaciones?: string | null;
  urlWebhookAsignacionDelivery?: string | null;
  waPhoneId?: string | null;
  fbPageId?: string | null;
  // solo escritura (no vienen en el GET)
  waAccessToken?: string | null;
  igToken?: string | null;
  fbPageAccessToken?: string | null;
  // solo lectura (se ignoran en el PUT)
  waConfigurado?: boolean;
  igConfigurado?: boolean;
  fbConfigurado?: boolean;
}

/**
 * Datos para crear/editar un Local
 */
export interface LocalFormData {
  nombre: string;
  direccion: string;
  telefono: string;
  planSuscripcion?: PlanSuscripcion;
  planActivo?: boolean;
  fechaInicioPlan?: string;
  fechaFinPlan?: string | null;
}

/**
 * Configuración de un Local
 */
export interface ConfiguracionLocal {
  id: number;
  localId: number;

  // Horarios de pedidos
  horaApertura: string; // "09:00:00"
  horaCierre: string; // "23:00:00"
  intervaloMinutosPedidos: number;
  maxPedidosPorIntervalo: number;

  // Horarios de reservas
  horaAperturaReservas: string;
  horaCierreReservas: string;
  intervaloMinutosReservas: number;
  maxReservasPorIntervalo: number;

  // Tipos de servicio
  permiteDelivery: boolean;
  permiteTakeAway: boolean;
  permiteReservas: boolean;

  // Integraciones
  impresionActiva: boolean;
  urlWebhookImpresora?: string;
  urlWebhookNotificaciones?: string;

  // Cancelaciones
  permiteCancelacionCliente: boolean;
  tiempoMaximoCancelacionMinutos: number;
}

/**
 * Datos para actualizar configuración
 */
export interface ConfiguracionLocalFormData {
  horaApertura: string;
  horaCierre: string;
  intervaloMinutosPedidos: number;
  maxPedidosPorIntervalo: number;
  horaAperturaReservas: string;
  horaCierreReservas: string;
  intervaloMinutosReservas: number;
  maxReservasPorIntervalo: number;
  permiteDelivery: boolean;
  permiteTakeAway: boolean;
  permiteReservas: boolean;
  impresionActiva: boolean;
  urlWebhookImpresora?: string;
  urlWebhookNotificaciones?: string;
  permiteCancelacionCliente: boolean;
  tiempoMaximoCancelacionMinutos: number;
}
