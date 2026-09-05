/**
 * API Service para Reservas.
 * Maneja operaciones CRUD de reservas.
 */

import { apiClient } from "./axios";
import { Reserva, ReservaDTO, ReservaResponseDTO, ReservaQueryParams } from "@/types/api";

export const reservasApi = {
  /**
   * Lista reservas del día actual.
   */
  getDelDia: async (telefonoLocal: string): Promise<Reserva[]> => {
    const { data } = await apiClient.get<Reserva[]>("/local/reservas", {
      params: { telefonoLocal },
    });
    return data;
  },

  /**
   * Crea una nueva reserva.
   */
  create: async (telefonoLocal: string, reserva: ReservaDTO): Promise<ReservaResponseDTO> => {
    const { data } = await apiClient.post<ReservaResponseDTO>("/local/reserva", reserva, { params: { telefonoLocal } });
    return data;
  },

  /**
   * Obtiene una reserva por ID.
   */
  getById: async (id: number, telefonoLocal: string): Promise<ReservaResponseDTO> => {
    const { data } = await apiClient.get<ReservaResponseDTO>(`/local/reservas/${id}`, { params: { telefonoLocal } });
    return data;
  },

  /**
   * Registra el gasto de una reserva.
   */
  registrarGasto: async (id: number, telefonoLocal: string, gastoTotal: number): Promise<Reserva> => {
    const { data } = await apiClient.patch<Reserva>(`/local/reservas/${id}/gasto`, null, {
      params: { telefonoLocal, gastoTotal },
    });
    return data;
  },

  /**
   * Cancela una reserva.
   */
  cancelar: async (id: number, telefonoLocal: string): Promise<Reserva> => {
    const { data } = await apiClient.delete<Reserva>(`/local/reservas/${id}`, {
      params: { telefonoLocal },
    });
    return data;
  },

  /**
   * Obtiene reservas por rango de fechas.
   */
  getByRango: async (params: ReservaQueryParams): Promise<Reserva[]> => {
    const { data } = await apiClient.get<Reserva[]>("/local/reservas/rango", {
      params,
    });
    return data;
  },
};
