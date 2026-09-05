package com.dioburger.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Entidad que representa la configuración operativa de un local.
 * Define horarios, modalidades permitidas y configuración de comunicación.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "configuracion_local")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionLocal {

    /**
     * Identificador único de la configuración.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Local al que pertenece esta configuración.
     * No se serializa: se corta el ciclo Local &lt;-&gt; ConfiguracionLocal.
     */
    @OneToOne
    @JoinColumn(name = "local_id", unique = true, nullable = false)
    @JsonIgnore
    private Local local;

    // === Configuración de Pedidos ===

    /**
     * Hora de apertura para pedidos.
     */
    @Column(nullable = false)
    private LocalTime horaApertura;

    /**
     * Hora de cierre para pedidos.
     */
    @Column(nullable = false)
    private LocalTime horaCierre;

    /**
     * Intervalo de tiempo (en minutos) entre slots de pedidos.
     * Ejemplo: 15 minutos = slots cada 15 min (20:00, 20:15, 20:30...).
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer intervaloMinutosPedidos = 15;

    /**
     * Máximo número de pedidos permitidos por intervalo de tiempo.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxPedidosPorIntervalo = 5;

    // === Configuración de Reservas ===

    /**
     * Hora de apertura para reservas (puede ser diferente a pedidos).
     */
    @Column(nullable = false)
    private LocalTime horaAperturaReservas;

    /**
     * Hora de cierre para reservas.
     */
    @Column(nullable = false)
    private LocalTime horaCierreReservas;

    /**
     * Intervalo de tiempo (en minutos) entre slots de reservas.
     * Ejemplo: 30 minutos = slots cada 30 min (20:00, 20:30, 21:00...).
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer intervaloMinutosReservas = 30;

    /**
     * Máximo número de reservas permitidas por intervalo de tiempo.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxReservasPorIntervalo = 3;

    // === Modalidades Permitidas ===

    /**
     * Indica si el local permite delivery.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean permiteDelivery = false;

    /**
     * Indica si el local permite retiro en el local (take away).
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean permiteTakeAway = true;

    /**
     * Indica si el local permite reservas de mesas.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean permiteReservas = false;

    // === Configuración de Cancelación ===

    /**
     * Tiempo mínimo (en minutos) de anticipación requerido para cancelar un pedido.
     * Si un pedido se intenta cancelar con menos anticipación, se rechaza la cancelación.
     * Ejemplo: 30 minutos = el pedido debe cancelarse al menos 30 min antes del horaPedido.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer minutosAnticipacionCancelacion = 30;

    // === Configuración de Comunicación ===

    /**
     * Indica si la impresión automática de tickets está activa.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean impresionActiva = false;

    /**
     * URL del webhook de la impresora para envío de tickets.
     */
    @Column(length = 500)
    private String urlWebhookImpresora;

    /**
     * URL del webhook de n8n para notificaciones de pedidos.
     * Se usa para notificar cuando un pedido está listo o en camino.
     */
    @Column(length = 500)
    private String urlWebhookNotificaciones;

    /**
     * URL del webhook de n8n para asignación automática de repartidores.
     * Se llama cuando se crea un pedido DELIVERY para obtener un repartidor disponible.
     */
    @Column(name = "url_webhook_asignacion_delivery", length = 500)
    private String urlWebhookAsignacionDelivery;

    // === Credenciales Meta (WhatsApp / Instagram / Facebook) ===
    // Cada local tiene sus propias credenciales de Meta Business API.
    // Nunca se hardcodean ni se comparten entre locales (multi-tenant).

    /**
     * Phone Number ID de WhatsApp Business API (Meta) asignado a este local.
     */
    @Column(name = "wa_phone_id", length = 100)
    private String waPhoneId;

    /**
     * Access token de WhatsApp Business API (Meta) para enviar mensajes.
     */
    @Column(name = "wa_access_token", length = 500)
    private String waAccessToken;

    /**
     * Access token de Instagram Graph API para responder comentarios de posts.
     */
    @Column(name = "ig_token", length = 500)
    private String igToken;

    /**
     * ID de la página de Facebook vinculada a este local.
     */
    @Column(name = "fb_page_id", length = 100)
    private String fbPageId;

    /**
     * Access token de la página de Facebook para responder comentarios de posts.
     */
    @Column(name = "fb_page_access_token", length = 500)
    private String fbPageAccessToken;
}
