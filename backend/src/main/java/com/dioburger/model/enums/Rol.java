package com.dioburger.model.enums;

/**
 * Enum que representa los roles de usuario en el sistema.
 * Define los niveles de acceso y permisos.
 * 
 * @author Dio Burger Team
 * @version 1.1.0
 */
public enum Rol {
    /**
     * Super Administrador con acceso global al sistema.
     * Puede crear y gestionar locales. No está asociado a un local específico.
     */
    ROLE_SUPERADMIN,
    
    /**
     * Administrador del local con acceso completo.
     */
    ROLE_ADMIN,
    
    /**
     * Personal de cocina con acceso limitado a gestión de pedidos.
     */
    ROLE_COCINA
}
