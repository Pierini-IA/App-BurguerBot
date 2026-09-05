/**
 * API Service para Reportes y Analytics.
 * Requiere feature REPORTES_AVANZADOS (plan PREMIUM).
 */

import { apiClient } from "./axios";
import {
  DashboardKPIs,
  ReporteVentasDTO,
  TopProducto,
  ComparacionPeriodos,
  ReportesQueryParams,
  ComparacionQueryParams,
} from "@/types/api";

export const reportesApi = {
  /**
   * Obtiene KPIs del dashboard.
   */
  getDashboard: async (telefonoLocal: string, fechaInicio: string, fechaFin: string): Promise<DashboardKPIs> => {
    const { data } = await apiClient.get<DashboardKPIs>("/reportes/dashboard", {
      params: { telefonoLocal, fechaInicio, fechaFin },
    });
    return data;
  },

  /**
   * Obtiene reporte de ventas diarias.
   */
  getVentasDiarias: async (params: ReportesQueryParams): Promise<ReporteVentasDTO[]> => {
    const { data } = await apiClient.get<ReporteVentasDTO[]>("/reportes/ventas/diarias", { params });
    return data;
  },

  /**
   * Obtiene reporte de ventas semanales.
   */
  getVentasSemanales: async (telefonoLocal: string, año: number, mes: number): Promise<ReporteVentasDTO[]> => {
    const { data } = await apiClient.get<ReporteVentasDTO[]>("/reportes/ventas/semanales", {
      params: { telefonoLocal, año, mes },
    });
    return data;
  },

  /**
   * Obtiene reporte de ventas mensuales.
   */
  getVentasMensuales: async (telefonoLocal: string, año: number): Promise<ReporteVentasDTO[]> => {
    const { data } = await apiClient.get<ReporteVentasDTO[]>("/reportes/ventas/mensuales", {
      params: { telefonoLocal, año },
    });
    return data;
  },

  /**
   * Obtiene top productos más vendidos.
   */
  getTopProductos: async (params: ReportesQueryParams): Promise<TopProducto[]> => {
    const { data } = await apiClient.get<TopProducto[]>("/reportes/productos/top", { params });
    return data;
  },

  /**
   * Compara dos períodos de tiempo.
   */
  compararPeriodos: async (params: ComparacionQueryParams): Promise<ComparacionPeriodos> => {
    const { data } = await apiClient.get<ComparacionPeriodos>("/reportes/ventas/comparacion", { params });
    return data;
  },
};
