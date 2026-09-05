package com.dioburger.model.dto;

import com.dioburger.model.enums.PlanSuscripcion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO con la información del local del usuario autenticado.
 *
 * <p>Devuelto por {@code GET /api/local/mi-local}. Permite al frontend
 * (paneles de ADMIN y COCINA) conocer su propio local, el plan de
 * suscripción vigente y las funcionalidades habilitadas, sin exponer
 * el listado global de locales (que es exclusivo de SUPERADMIN).</p>
 *
 * <p>No incluye secretos (tokens de la Graph API): para editar las
 * credenciales de canal se usa el endpoint específico de configuración.</p>
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiLocalDTO {

    /** ID interno del local. */
    private Long localId;

    /** Nombre comercial del local. */
    private String nombre;

    /** Dirección física del local. */
    private String direccion;

    /** Teléfono del local (identificador multi-tenant). */
    private String telefono;

    /** Plan de suscripción contratado. */
    private PlanSuscripcion planSuscripcion;

    /** Nombre legible del plan (ej. "Premium"). */
    private String planNombre;

    /** Indica si el plan está activo (pago al día). */
    private Boolean planActivo;

    /** Fecha de vencimiento del plan. {@code null} = indefinido. */
    private LocalDate fechaFinPlan;

    /**
     * Nombres de las {@code Feature} habilitadas para este local segun su plan.
     * Si el plan está inactivo la lista viene vacía.
     * El frontend usa estos strings para mostrar/ocultar secciones.
     */
    private List<String> features;

    // ---- Resumen de configuración operativa (sin secretos) ----

    /** Hora de apertura del local. */
    private LocalTime horaApertura;

    /** Hora de cierre del local. */
    private LocalTime horaCierre;

    /** Si el local acepta pedidos para retirar. */
    private Boolean permiteTakeAway;

    /** Si el local acepta pedidos con envío. */
    private Boolean permiteDelivery;

    /** Si el local acepta reservas de mesa. */
    private Boolean permiteReservas;

    /** Si la impresión automática de comandas está activa. */
    private Boolean impresionActiva;

    /** Si el canal de WhatsApp tiene credenciales cargadas. */
    private Boolean whatsappConfigurado;
}
