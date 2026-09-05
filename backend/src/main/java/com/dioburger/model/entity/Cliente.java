package com.dioburger.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un cliente.
 * Los clientes se identifican únicamente por su teléfono.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    /**
     * Identificador único del cliente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del cliente.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Teléfono del cliente (único).
     * Se usa para identificar al cliente en pedidos y reservas.
     */
    @Column(nullable = false, unique = true)
    private String telefono;
}
