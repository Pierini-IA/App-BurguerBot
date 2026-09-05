package com.dioburger.model.dto;

import com.dioburger.model.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de un nuevo usuario por SUPERADMIN.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDTO {

    /**
     * Nombre de usuario único.
     */
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 4, max = 50, message = "El username debe tener entre 4 y 50 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "El username solo puede contener letras, números y guiones bajos"
    )
    private String username;

    /**
     * Contraseña del usuario (será hasheada).
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    /**
     * Rol del usuario.
     */
    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    /**
     * Teléfono del local al que pertenece (requerido solo si no es SUPERADMIN).
     */
    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "El teléfono debe estar en formato internacional"
    )
    private String telefonoLocal;
}
