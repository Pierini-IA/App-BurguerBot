/**
 * Tipos relacionados con pedidos y reservas
 */

import { Producto, Extra } from "./producto";
import { Local } from "./local";

/**
 * Estados de un pedido
 */
export enum EstadoPedido {
  PENDIENTE = "PENDIENTE",
  CONFIRMADO = "CONFIRMADO",
  EN_PREPARACION = "EN_PREPARACION",
  LISTO = "LISTO",
  EN_CAMINO = "EN_CAMINO",
  ENTREGADO = "ENTREGADO",
  CANCELADO = "CANCELADO",
}

/**
 * Tipos de pedido
 */
export enum TipoPedido {
  DELIVERY = "DELIVERY",
  TAKE_AWAY = "TAKE_AWAY",
}

/**
 * Cliente
 */
export interface Cliente {
  id: number;
  nombre: string;
  telefono: string;
  email?: string;
  direccion?: string;
  localId: number;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Producto en un pedido
 */
export interface ProductoPedido {
  id: number;
  productoId: number;
  producto?: Producto;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  extras?: ExtraPedido[];
  observaciones?: string;
}

/**
 * Extra en un pedido
 */
export interface ExtraPedido {
  id: number;
  extraId: number;
  extra?: Extra;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

/**
 * Pedido
 */
export interface Pedido {
  id: number;
  localId: number;
  local?: Local;
  clienteId: number;
  cliente: Cliente;
  tipo: TipoPedido;
  estado: EstadoPedido;
  productos: ProductoPedido[];
  total: number;
  direccionEntrega?: string;
  horaEstimadaEntrega?: string;
  observaciones?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Datos para crear un pedido
 */
export interface PedidoFormData {
  clienteId: number;
  tipo: TipoPedido;
  direccionEntrega?: string;
  observaciones?: string;
  productos: {
    productoId: number;
    cantidad: number;
    extras?: {
      extraId: number;
      cantidad: number;
    }[];
    observaciones?: string;
  }[];
}

/**
 * Estados de una reserva
 */
export enum EstadoReserva {
  PENDIENTE = "PENDIENTE",
  CONFIRMADA = "CONFIRMADA",
  CANCELADA = "CANCELADA",
  COMPLETADA = "COMPLETADA",
}

/**
 * Mesa
 */
export interface Mesa {
  id: number;
  numero: number;
  capacidad: number;
  localId: number;
  activa: boolean;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Reserva
 */
export interface Reserva {
  id: number;
  localId: number;
  local?: Local;
  clienteId: number;
  cliente: Cliente;
  mesaId: number;
  mesa?: Mesa;
  fechaHora: string; // ISO datetime string
  cantidadPersonas: number;
  estado: EstadoReserva;
  observaciones?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Datos para crear una reserva
 */
export interface ReservaFormData {
  clienteId: number;
  mesaId: number;
  fechaHora: string;
  cantidadPersonas: number;
  observaciones?: string;
}
