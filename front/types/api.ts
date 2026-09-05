// ================================
// 🎯 API Types - Dio Burger Admin
// ================================

/**
 * Tipos completos para la API del panel de administración.
 * Basados en la documentación del backend (ADMIN-API-DOCUMENTATION.md)
 */

// ========== ENUMS ==========

export enum Rol {
  SUPERADMIN = "ROLE_SUPERADMIN",
  ADMIN = "ROLE_ADMIN",
  COCINA = "ROLE_COCINA",
}

export enum EstadoPedido {
  PENDIENTE = "PENDIENTE",
  CONFIRMADO = "CONFIRMADO",
  EN_PREPARACION = "EN_PREPARACION",
  LISTO = "LISTO",
  EN_CAMINO = "EN_CAMINO",
  ENTREGADO = "ENTREGADO",
  CANCELADO = "CANCELADO",
}

export enum Modalidad {
  DELIVERY = "DELIVERY",
  RETIRAR = "RETIRAR",
}

export enum MedioPago {
  EFECTIVO = "EFECTIVO",
  TRANSFERENCIA = "TRANSFERENCIA",
  TARJETA_DEBITO = "TARJETA_DEBITO",
  TARJETA_CREDITO = "TARJETA_CREDITO",
  QR = "QR",
}

export enum EstadoPago {
  PENDIENTE = "PENDIENTE",
  PAGADO = "PAGADO",
  RECHAZADO = "RECHAZADO",
}

export enum EstadoReserva {
  PENDIENTE = "PENDIENTE",
  CONFIRMADA = "CONFIRMADA",
  CANCELADA = "CANCELADA",
  FINALIZADA = "FINALIZADA",
}

export enum PlanSuscripcion {
  FREE = "FREE",
  BASIC = "BASIC",
  PREMIUM = "PREMIUM",
  ENTERPRISE = "ENTERPRISE",
}

export enum UnidadMedida {
  UNIDADES = "UNIDADES",
  GRAMOS = "GRAMOS",
  LITROS = "LITROS",
}

export enum Categoria {
  HAMBURGUESAS = "HAMBURGUESAS",
  PAPAS = "PAPAS",
  BEBIDAS = "BEBIDAS",
  POSTRES = "POSTRES",
  EXTRAS = "EXTRAS",
}

// ========== INGREDIENTES ==========

export interface Ingrediente {
  id: number;
  nombre: string;
  stockActual: number;
  stockMinimo: number;
  unidadMedida: UnidadMedida;
  disponible: boolean;
  localId: number;
}

export interface CreateIngredienteDTO {
  nombre: string;
  stockActual: number;
  stockMinimo: number;
  unidadMedida: UnidadMedida;
  disponible: boolean;
}

// ========== PRODUCTOS ==========

export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: string;
  disponible: boolean;
  destacado: boolean;
  imagenUrl?: string;
  tiempoPreparacion?: number; // minutos
  ingredientes: Ingrediente[];
  localId: number;
}

export interface CreateProductoDTO {
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: string;
  disponible: boolean;
  destacado: boolean;
  imagenUrl?: string;
  tiempoPreparacion?: number;
  ingredientesIds: number[];
}

export type UpdateProductoDTO = Partial<CreateProductoDTO>;

// ========== MESAS ==========

export interface Mesa {
  id: number;
  numero: number;
  capacidad: number;
  ocupada: boolean;
  localId: number;
  reservaActual?: Reserva;
}

export interface CreateMesaDTO {
  numero: number;
  capacidad: number;
  ocupada: boolean;
}

export type UpdateMesaDTO = Partial<CreateMesaDTO>;

// ========== CATEGORÍAS ==========

export interface CategoriaDTO {
  id: number;
  nombre: string;
  descripcion?: string;
  orden: number;
  activo: boolean;
  localId: number;
  localNombre: string;
  cantidadProductos: number;
  cantidadExtras: number;
}

export interface CreateCategoriaDTO {
  nombre: string;
  descripcion?: string;
  orden: number;
  activo: boolean;
}

export type UpdateCategoriaDTO = Partial<CreateCategoriaDTO>;

// ========== EXTRAS ==========

export interface ExtraDTO {
  id: number;
  nombre: string;
  descripcion?: string;
  precioAdicional: number;
  activo: boolean;
  localId: number;
  localNombre: string;
  categoriaId?: number;
  categoriaNombre?: string;
  esObligatorio: boolean;
}

export interface CreateExtraDTO {
  nombre: string;
  descripcion?: string;
  precioAdicional: number;
  activo: boolean;
  categoriaId?: number;
  esObligatorio: boolean;
}

export type UpdateExtraDTO = Partial<CreateExtraDTO>;

// ========== LOCALES ==========

export interface LocalDTO {
  id: number;
  nombre: string;
  direccion: string;
  telefono: string;
  planSuscripcion: PlanSuscripcion;
  planActivo: boolean;
  fechaInicioPlan?: string; // ISO date
  fechaFinPlan?: string; // ISO date
}

export interface CreateLocalDTO {
  nombre: string;
  direccion: string;
  telefono: string;
  planSuscripcion: PlanSuscripcion;
  planActivo: boolean;
  fechaInicioPlan?: string;
  fechaFinPlan?: string;
}

export type UpdateLocalDTO = Partial<CreateLocalDTO>;

// ========== USUARIOS ==========

export interface Usuario {
  id: number;
  username: string;
  rol: Rol;
  telefonoLocal?: string;
  enabled: boolean;
  createdAt: string;
}

export interface UsuarioCreateDTO {
  username: string;
  password: string;
  rol: Rol;
  telefonoLocal?: string;
}

// ========== CLIENTES ==========

export interface Cliente {
  id: number;
  nombre: string;
  telefono: string;
  direccion?: string;
}

export interface ClienteDTO {
  nombre: string;
  telefono: string;
  direccion?: string;
}

// ========== PEDIDOS ==========

/** Extra elegido para un item del pedido (tal como lo serializa el backend). */
export interface PedidoItemExtra {
  extra?: { id: number; nombre: string; precioAdicional?: number };
}

export interface PedidoItem {
  id: number;
  producto?: Producto;
  cantidad: number;
  /** Notas de cocina del item, ej. "sin cebolla", "punto jugoso". */
  observaciones?: string;
  extrasSeleccionados?: PedidoItemExtra[];
  // Campos de precio: solo presentes en algunas respuestas.
  precioUnitario?: number;
  subtotal?: number;
  extras?: ExtraDTO[];
}

export enum OrigenPedido {
  BOT = "BOT",
  LOCAL = "LOCAL",
}

export interface Pedido {
  id: number;
  cliente?: Cliente;
  modalidad: Modalidad;
  estado: EstadoPedido;
  items: PedidoItem[];
  total?: number;
  direccionEnvio?: string;
  medioPago?: MedioPago;
  estadoPago?: EstadoPago;
  origenPedido?: OrigenPedido;
  horaPedido: string; // ISO datetime
  horaEstimadaEntrega?: string;
  requestId?: string;
  localId?: number;
  repartidorNombre?: string;
}

export interface PedidoItemDTO {
  productoId: number;
  cantidad: number;
  extrasIds?: number[];
}

export interface PedidoDTO {
  requestId: string;
  cliente: ClienteDTO;
  modalidad: Modalidad;
  direccionEnvio?: string;
  medioPago: MedioPago;
  items: PedidoItemDTO[];
  horaPedido?: string; // "HH:mm"
}

// ========== RESERVAS ==========

export interface MesaDTO {
  id: number;
  numero: number;
  capacidad: number;
  ocupada: boolean;
  localId: number;
}

export interface Reserva {
  id: number;
  cliente: Cliente;
  horaReserva: string; // ISO datetime
  numeroPersonas: number;
  mesas: Mesa[];
  estado: EstadoReserva;
  gastoTotal?: number;
  observaciones?: string;
  requestId: string;
}

export interface ReservaDTO {
  cliente: ClienteDTO;
  horaReserva: string; // ISO datetime
  numeroPersonas: number;
  observaciones?: string;
  requestId: string;
}

export interface ReservaResponseDTO {
  id: number;
  cliente: ClienteDTO;
  horaReserva: string;
  numeroPersonas: number;
  mesas: MesaDTO[];
  estado: EstadoReserva;
  gastoTotal?: number;
  observaciones?: string;
  fechaCreacion: string;
}

// ========== REPORTES ==========

export interface ReporteVentasDTO {
  fecha: string; // "2025-01-15"
  periodo: string; // "Miércoles 15/01"
  cantidadPedidos: number;
  totalVentas: number;
  promedioTicket: number;
}

export interface TopProducto {
  productoId: number;
  nombre: string;
  cantidadVendida: number;
  totalVentas: number;
  porcentajeVentas: number;
}

export interface PedidosPorEstado {
  PENDIENTE: number;
  CONFIRMADO: number;
  EN_PREPARACION: number;
  LISTO: number;
  ENTREGADO: number;
  CANCELADO: number;
}

export interface VentasPorModalidad {
  DELIVERY: number;
  RETIRAR: number;
}

export interface VentasPorMedioPago {
  EFECTIVO: number;
  TRANSFERENCIA: number;
  TARJETA_DEBITO: number;
  TARJETA_CREDITO: number;
  QR: number;
}

export interface DashboardKPIs {
  totalVentas: number;
  cantidadPedidos: number;
  promedioTicket: number;
  pedidosPorEstado: PedidosPorEstado;
  ventasPorModalidad: VentasPorModalidad;
  ventasPorMedioPago: VentasPorMedioPago;
  productosVendidos: number;
  reservasActivas: number;
  mesasOcupadas: number;
}

export interface EstadisticasDelDia {
  totalPedidos: number;
  totalReservas: number;
  ingresosPedidos: number;
  ingresosReservas: number;
  ingresosTotal: number;
}

export interface PeriodoComparacion {
  fechaInicio: string;
  fechaFin: string;
  totalVentas: number;
  cantidadPedidos: number;
  promedioTicket: number;
}

export interface VariacionesPeriodos {
  ventasAbsoluta: number;
  ventasPorcentual: number;
  pedidosAbsoluta: number;
  pedidosPorcentual: number;
  ticketAbsoluta: number;
  ticketPorcentual: number;
}

export interface ComparacionPeriodos {
  periodo1: PeriodoComparacion;
  periodo2: PeriodoComparacion;
  variaciones: VariacionesPeriodos;
}

// ========== API RESPONSES ==========

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

export interface ApiError {
  error: string;
  message: string;
  status?: number;
  validationErrors?: Record<string, string[]>;
  feature?: string;
  planActual?: PlanSuscripcion;
  planesRequeridos?: PlanSuscripcion[];
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// ========== QUERY PARAMS ==========

export interface PedidoQueryParams {
  telefonoLocal: string;
  fechaInicio?: string; // yyyy-MM-dd
  fechaFin?: string; // yyyy-MM-dd
}

export interface ReservaQueryParams {
  telefonoLocal: string;
  fechaInicio?: string; // yyyy-MM-dd
  fechaFin?: string; // yyyy-MM-dd
}

export interface CategoriaQueryParams {
  telefonoLocal: string;
  soloActivas?: boolean;
}

export interface ExtraQueryParams {
  telefonoLocal: string;
  soloActivos?: boolean;
  categoriaId?: number;
}

export interface ReportesQueryParams {
  telefonoLocal: string;
  fechaInicio: string; // yyyy-MM-dd
  fechaFin: string; // yyyy-MM-dd
  limit?: number;
}

export interface ComparacionQueryParams {
  telefonoLocal: string;
  periodo1Inicio: string;
  periodo1Fin: string;
  periodo2Inicio: string;
  periodo2Fin: string;
}
