package com.dioburger.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para login exitoso.
 * Contiene el token JWT y la información del usuario.
 * 
 * @author Dio Burger Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {

    /**
     * Token JWT generado.
     */
    private String token;

    /**
     * Tipo de token (siempre "Bearer").
     */
    @Builder.Default
    private String type = "Bearer";

    /**
     * Username del usuario autenticado.
     */
    private String username;

    /**
     * Rol del usuario.
     */
    private String rol;

    /**
     * Teléfono del local al que pertenece el usuario.
     */
    private String telefonoLocal;
}
