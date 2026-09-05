/**
 * API Service para Estadísticas.
 * Obtiene resúmenes y KPIs del local.
 */

import { apiClient } from "./axios";
import { EstadisticasDelDia } from "@/types/api";

export const estadisticasApi = {
  /**
   * Obtiene estadísticas del día actual.
   * Incluye: pedidos, reservas, ingresos totales.
   */
  getDelDia: async (telefonoLocal: string): Promise<EstadisticasDelDia> => {
    const { data } = await apiClient.get<EstadisticasDelDia>("/local/estadisticas/dia", { params: { telefonoLocal } });
    return data;
  },
};
