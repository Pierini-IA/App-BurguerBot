import { apiClient } from "./axios";
import { LoginCredentials, LoginResponse } from "@/types/usuario";
import { defaults } from "@/lib/config/defaults";

/**
 * API Service para autenticación
 * Maneja login, logout, registro y obtención de usuario actual
 */
export const authApi = {
  /**
   * Login con email o teléfono + password
   * @param credentials - Email/teléfono y contraseña
   * @returns Token JWT y datos del usuario
   */
  login: async (credentials: LoginCredentials): Promise<LoginResponse> => {
    const { data } = await apiClient.post<LoginResponse>("/auth/login", credentials);
    return data;
  },

  /**
   * Logout - Limpia localStorage
   * En el futuro podría invalidar el token en backend
   */
  logout: (): void => {
    localStorage.removeItem(defaults.storage.token);
    localStorage.removeItem(defaults.storage.user);
  },
};
