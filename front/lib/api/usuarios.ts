import { apiClient } from "./axios";
import { Usuario, UsuarioCreateData, Rol } from "@/types/usuario";

/**
 * Datos para actualizar usuario
 */
export interface UsuarioUpdateData {
  username?: string;
  password?: string;
  rol?: Rol;
  telefonoLocal?: string;
}

/**
 * API Service para gestión de Usuarios
 * Endpoints del SuperAdmin
 */
export const usuariosApi = {
  /**
   * Obtener todos los usuarios
   */
  getAll: async (): Promise<Usuario[]> => {
    const { data } = await apiClient.get<Usuario[]>("/superadmin/usuarios");
    return data;
  },

  /**
   * Obtener usuarios por local
   */
  getByLocal: async (localId: number): Promise<Usuario[]> => {
    const { data } = await apiClient.get<Usuario[]>(`/superadmin/usuarios/local/${localId}`);
    return data;
  },

  /**
   * Obtener un usuario por ID
   */
  getById: async (id: number): Promise<Usuario> => {
    const { data } = await apiClient.get<Usuario>(`/superadmin/usuarios/${id}`);
    return data;
  },

  /**
   * Crear un nuevo usuario
   */
  create: async (userData: UsuarioCreateData): Promise<Usuario> => {
    const { data } = await apiClient.post<Usuario>("/superadmin/usuarios", userData);
    return data;
  },

  /**
   * Actualizar un usuario existente
   */
  update: async (id: number, userData: UsuarioUpdateData): Promise<Usuario> => {
    const { data } = await apiClient.put<Usuario>(`/superadmin/usuarios/${id}`, userData);
    return data;
  },

  /**
   * Eliminar un usuario
   */
  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/superadmin/usuarios/${id}`);
  },

  /**
   * Activar/Desactivar un usuario
   */
  toggleActive: async (id: number): Promise<Usuario> => {
    const { data } = await apiClient.patch<Usuario>(`/superadmin/usuarios/${id}/toggle-active`);
    return data;
  },

  /**
   * Cambiar contraseña de un usuario
   */
  changePassword: async (id: number, newPassword: string): Promise<void> => {
    await apiClient.patch(`/superadmin/usuarios/${id}/password`, { password: newPassword });
  },
};
