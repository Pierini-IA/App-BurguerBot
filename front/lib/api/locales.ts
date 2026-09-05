import { apiClient } from "./axios";
import { Local, LocalFormData, ConfiguracionLocal, ConfiguracionLocalFormData } from "@/types/local";

/**
 * API Service para gestión de Locales
 * Endpoints del SuperAdmin
 */
export const localesApi = {
  /**
   * Obtener todos los locales
   */
  getAll: async (): Promise<Local[]> => {
    const { data } = await apiClient.get<Local[]>("/superadmin/locales");
    return data;
  },

  /**
   * Obtener un local por ID
   */
  getById: async (id: number): Promise<Local> => {
    const { data } = await apiClient.get<Local>(`/superadmin/locales/${id}`);
    return data;
  },

  /**
   * Crear un nuevo local
   */
  create: async (localData: LocalFormData): Promise<Local> => {
    const { data } = await apiClient.post<Local>("/superadmin/locales", localData);
    return data;
  },

  /**
   * Actualizar un local existente
   */
  update: async (id: number, localData: Partial<LocalFormData>): Promise<Local> => {
    const { data } = await apiClient.put<Local>(`/superadmin/locales/${id}`, localData);
    return data;
  },

  /**
   * Eliminar un local
   */
  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/superadmin/locales/${id}`);
  },

  /**
   * Obtener configuración de un local
   */
  getConfig: async (localId: number): Promise<ConfiguracionLocal> => {
    const { data } = await apiClient.get<ConfiguracionLocal>(`/superadmin/locales/${localId}/config`);
    return data;
  },

  /**
   * Actualizar configuración de un local
   */
  updateConfig: async (
    localId: number,
    configData: Partial<ConfiguracionLocalFormData>
  ): Promise<ConfiguracionLocal> => {
    const { data } = await apiClient.put<ConfiguracionLocal>(`/superadmin/locales/${localId}/config`, configData);
    return data;
  },
};
