package com.dioburger.model.dto;

import com.dioburger.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un usuario en las respuestas.
 * Incluye datos básicos del usuario sin exponer información sensible.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    
    /**
     * ID del usuario.
     */
    private Long id;
    
    /**
     * Nombre de usuario.
     */
    private String username;
    
    /**
     * Rol del usuario.
     */
    private Rol rol;
    
    /**
     * ID del local asociado (puede ser null para SUPERADMIN).
     */
    private Long localId;
    
    /**
     * Nombre del local asociado (puede ser null para SUPERADMIN).
     */
    private String localNombre;
    
    /**
     * Teléfono del local asociado (puede ser null para SUPERADMIN).
     */
    private String telefonoLocal;
}
