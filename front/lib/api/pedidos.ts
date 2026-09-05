/**
 * API Service para Pedidos.
 * Maneja operaciones de consulta y gestión de pedidos.
 */

import { apiClient } from "./axios";
import { Pedido, PedidoQueryParams } from "@/types/api";

export const pedidosApi = {
  /**
   * Lista pedidos del día actual.
   */
  getDelDia: async (telefonoLocal: string): Promise<Pedido[]> => {
    const { data } = await apiClient.get<Pedido[]>("/local/pedidos", {
      params: { telefonoLocal },
    });
    return data;
  },

  /**
   * Obtiene un pedido por ID.
   */
  getById: async (id: number, telefonoLocal: string): Promise<Pedido> => {
    const { data } = await apiClient.get<Pedido>(`/local/pedidos/${id}`, {
      params: { telefonoLocal },
    });
    return data;
  },

  /**
   * Obtiene pedidos por rango de fechas.
   */
  getByRango: async (params: PedidoQueryParams): Promise<Pedido[]> => {
    const { data } = await apiClient.get<Pedido[]>("/local/pedidos/rango", {
      params,
    });
    return data;
  },
};
