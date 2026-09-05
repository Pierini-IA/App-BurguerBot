package com.dioburger.model.entity;

import com.dioburger.model.enums.PlanSuscripcion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un local de la red Dio Burger.
 * Cada local tiene un teléfono único que actúa como identificador Multi-Tenant.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "locales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Local {

    /**
     * Identificador único del local.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del local.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Dirección física del local.
     */
    @Column(nullable = false)
    private String direccion;

    /**
     * Teléfono del local (Multi-Tenant ID).
     * Este campo es único y se usa para identificar el local en las peticiones.
     */
    @Column(unique = true, nullable = false)
    private String telefono;

    /**
     * Configuración específica del local.
     */
    @OneToOne(mappedBy = "local", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ConfiguracionLocal configuracion;

    /**
     * Lista de productos del menú del local.
     */
    @OneToMany(mappedBy = "local", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();

    /**
     * Lista de ingredientes disponibles en el local.
     */
    @OneToMany(mappedBy = "local", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ingrediente> ingredientes = new ArrayList<>();

    /**
     * Lista de mesas del local (si permite reservas).
     */
    @OneToMany(mappedBy = "local", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Mesa> mesas = new ArrayList<>();

    // =============================================
    // CAMPOS DE PLAN DE SUSCRIPCIÓN (v2.2.0)
    // =============================================

    /**
     * Plan de suscripción del local.
     * Determina las funcionalidades disponibles (BASICO, ESTANDAR, PREMIUM).
     * Por defecto es PREMIUM para mantener compatibilidad con locales existentes.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_suscripcion", nullable = false, length = 20)
    @Builder.Default
    private PlanSuscripcion planSuscripcion = PlanSuscripcion.PREMIUM;

    /**
     * Indica si el plan de suscripción está activo.
     * Si es false, el local no puede usar el sistema hasta renovar.
     * Por defecto es true para locales nuevos y existentes.
     */
    @Column(name = "plan_activo", nullable = false)
    @Builder.Default
    private Boolean planActivo = true;

    /**
     * Fecha de inicio del plan actual.
     * Útil para tracking y reportes de antigüedad del cliente.
     */
    @Column(name = "fecha_inicio_plan")
    private LocalDate fechaInicioPlan;

    /**
     * Fecha de fin del plan actual (vencimiento).
     * Si es null, el plan no tiene fecha de expiración (indefinido).
     * Si está presente y se pasa la fecha, se debe desactivar el plan.
     */
    @Column(name = "fecha_fin_plan")
    private LocalDate fechaFinPlan;
}
