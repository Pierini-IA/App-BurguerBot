/**
 * Roles de usuario en el sistema (coincide con backend)
 */
export enum Rol {
  SUPERADMIN = "ROLE_SUPERADMIN",
  ADMIN = "ROLE_ADMIN",
  COCINA = "ROLE_COCINA",
}

/**
 * Interface de Usuario (coincide con UsuarioDTO del backend)
 */
export interface Usuario {
  id?: number; // Opcional porque el JWT no devuelve id
  username: string;
  rol: Rol;
  localId?: number; // Solo para ADMIN y COCINA
  localNombre?: string; // Solo para ADMIN y COCINA
  telefonoLocal?: string; // Solo para ADMIN y COCINA
}

/**
 * Credenciales de login (coincide con backend)
 */
export interface LoginCredentials {
  username: string;
  password: string;
}

/**
 * Respuesta de login desde el backend (coincide con JwtResponseDTO)
 */
export interface LoginResponse {
  token: string;
  type: string; // "Bearer"
  username: string;
  rol: string; // "ROLE_SUPERADMIN", "ROLE_ADMIN", "ROLE_COCINA"
  telefonoLocal?: string;
}

/**
 * Datos para crear usuario (coincide con UsuarioCreateDTO)
 */
export interface UsuarioCreateData {
  username: string;
  password: string;
  rol: Rol;
  telefonoLocal?: string; // Requerido si rol no es SUPERADMIN
}
