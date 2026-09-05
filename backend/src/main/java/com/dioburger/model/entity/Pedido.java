package com.dioburger.model.entity;

import com.dioburger.model.enums.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un pedido realizado por un cliente.
 * Los pedidos pueden originarse desde el bot (WhatsApp) o desde el panel del local.
 * Soporta asignación automática de repartidores para pedidos DELIVERY.
 * 
 * @author Dio Burger Team
 * @version 2.0.0
 */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pedido {

    /**
     * Identificador único del pedido.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Local al que pertenece este pedido.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id", nullable = false)
    @JsonIgnoreProperties({"configuracion", "productos", "ingredientes", "mesas", "hibernateLazyInitializer", "handler"})
    private Local local;

    /**
     * Cliente que realizó el pedido.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Modalidad del pedido (DELIVERY o RETIRAR).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    /**
     * Estado del pedido (PENDIENTE, CONFIRMADO, EN_PREPARACION, LISTO, EN_CAMINO, ENTREGADO, CANCELADO).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    /**
     * Medio de pago utilizado (EFECTIVO, TRANSFERENCIA, TARJETA_DEBITO, TARJETA_CREDITO, QR).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedioPago medioPago;

    /**
     * Estado del pago (PENDIENTE, PAGADO, RECHAZADO).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    /**
     * Origen del pedido (BOT o LOCAL).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigenPedido origenPedido;

    /**
     * Monto total del pedido.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * Dirección de envío (solo si modalidad es DELIVERY).
     */
    @Column(length = 500)
    private String direccionEnvio;

    /**
     * Fecha y hora en que se realizó el pedido.
     */
    @Column(nullable = false)
    private LocalDateTime horaPedido;

    /**
     * ID de la petición (idempotencia).
     * Evita duplicación de pedidos en caso de reintentos.
     */
    @Column(unique = true, nullable = false)
    private String requestId;

    /**
     * ID del repartidor asignado (viene de n8n).
     * Solo aplica para pedidos DELIVERY.
     */
    @Column(name = "repartidor_id", length = 100)
    private String repartidorId;

    /**
     * Nombre del repartidor asignado.
     */
    @Column(name = "repartidor_nombre", length = 200)
    private String repartidorNombre;

    /**
     * Teléfono del repartidor para contacto directo.
     */
    @Column(name = "repartidor_telefono", length = 20)
    private String repartidorTelefono;

    /**
     * Momento en que se asignó el repartidor automáticamente.
     */
    @Column(name = "hora_asignacion_repartidor")
    private LocalDateTime horaAsignacionRepartidor;

    /**
     * URL para trackear el pedido en tiempo real.
     * Proporcionada por el sistema de delivery de n8n.
     */
    @Column(name = "url_tracking_delivery", length = 500)
    private String urlTrackingDelivery;

    /**
     * Items del pedido (productos y cantidades).
     */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PedidoItem> items = new ArrayList<>();
}
