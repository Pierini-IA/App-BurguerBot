/**
 * API Service para Cocina.
 * Maneja operaciones de gestión de pedidos desde el panel de cocina.
 */

import { apiClient } from "./axios";
import { Pedido, EstadoPedido } from "@/types/api";

export const cocinaApi = {
  /**
   * Lista pedidos activos (no entregados ni cancelados).
   */
  getPedidosActivos: async (telefonoLocal: string): Promise<Pedido[]> => {
    const { data } = await apiClient.get<Pedido[]>("/cocina/pedidos", {
      params: { telefonoLocal },
    });
    return data;
  },

  /**
   * Obtiene detalle de un pedido específico.
   */
  getDetallePedido: async (id: number): Promise<Pedido> => {
    const { data } = await apiClient.get<Pedido>(`/cocina/pedidos/${id}`);
    return data;
  },

  /**
   * Cambia el estado de un pedido.
   */
  cambiarEstado: async (id: number, nuevoEstado: EstadoPedido): Promise<Pedido> => {
    const { data } = await apiClient.patch<Pedido>(`/cocina/pedidos/${id}/estado`, null, { params: { nuevoEstado } });
    return data;
  },

  /**
   * Atajo: Inicia preparación de un pedido (estado = EN_PREPARACION).
   */
  iniciarPreparacion: async (id: number): Promise<Pedido> => {
    const { data } = await apiClient.post<Pedido>(`/cocina/pedidos/${id}/iniciar-preparacion`);
    return data;
  },

  /**
   * Atajo: Marca un pedido como listo (estado = LISTO).
   * Envía notificación si es TAKE_AWAY.
   */
  marcarListo: async (id: number): Promise<Pedido> => {
    const { data } = await apiClient.post<Pedido>(`/cocina/pedidos/${id}/marcar-listo`);
    return data;
  },

  /**
   * Atajo: Marca un pedido en camino (estado = EN_CAMINO).
   * Solo para pedidos DELIVERY. Envía notificación.
   */
  marcarEnCamino: async (id: number): Promise<Pedido> => {
    const { data } = await apiClient.post<Pedido>(`/cocina/pedidos/${id}/marcar-en-camino`);
    return data;
  },

  /**
   * Atajo: Entrega un pedido (estado = ENTREGADO).
   */
  entregarPedido: async (id: number): Promise<Pedido> => {
    const { data } = await apiClient.post<Pedido>(`/cocina/pedidos/${id}/entregar`);
    return data;
  },

  /**
   * Obtiene historial completo de pedidos.
   */
  getHistorial: async (telefonoLocal: string): Promise<Pedido[]> => {
    const { data } = await apiClient.get<Pedido[]>("/cocina/pedidos/historial", {
      params: { telefonoLocal },
    });
    return data;
  },
};
