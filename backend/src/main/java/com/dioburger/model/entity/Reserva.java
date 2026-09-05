package com.dioburger.model.entity;

import com.dioburger.model.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa una reserva de mesa(s) para un cliente.
 * Una reserva puede abarcar múltiples mesas para grupos grandes.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    /**
     * Identificador único de la reserva.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cliente que realizó la reserva.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Mesas asignadas a esta reserva.
     * Puede ser una o múltiples mesas unidas para grupos grandes.
     */
    @ManyToMany
    @JoinTable(
        name = "reserva_mesas",
        joinColumns = @JoinColumn(name = "reserva_id"),
        inverseJoinColumns = @JoinColumn(name = "mesa_id")
    )
    @Builder.Default
    private Set<Mesa> mesas = new HashSet<>();

    /**
     * Fecha y hora de la reserva.
     */
    @Column(nullable = false)
    private LocalDateTime horaReserva;

    /**
     * Número de personas para la reserva.
     */
    @Column(nullable = false)
    private Integer numeroPersonas;

    /**
     * Estado de la reserva (CONFIRMADA, OCUPADA, FINALIZADA, CANCELADA).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    /**
     * Gasto total registrado al finalizar la reserva (para facturación).
     */
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal gastoTotal = BigDecimal.ZERO;

    /**
     * Observaciones adicionales de la reserva (ej: "ventana si es posible").
     */
    @Column(length = 500)
    private String observaciones;

    /**
     * ID de la petición (idempotencia).
     * Evita duplicación de reservas en caso de reintentos.
     */
    @Column(unique = true, nullable = false)
    private String requestId;
}
