package com.dioburger.model.entity;

import com.dioburger.model.enums.Rol;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un usuario del sistema (empleado del local).
 * Los usuarios tienen roles que determinan sus permisos (ADMIN o COCINA).
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Local al que pertenece este usuario.
     * NULL para SUPERADMIN (sin local específico) — ver migración V6.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id")
    @JsonIgnoreProperties({"usuarios", "pedidos", "reservas", "configuracion", "productos", "ingredientes", "mesas"})
    private Local local;

    /**
     * Nombre de usuario único.
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Contraseña hasheada con BCrypt.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Rol del usuario (ROLE_ADMIN o ROLE_COCINA).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
}
