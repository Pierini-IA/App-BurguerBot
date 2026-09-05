package com.dioburger.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Un turno de texto de una conversación del bot con un cliente.
 *
 * Sirve para que el agente recuerde de qué venían hablando: sin esto cada
 * mensaje se procesa aislado y el cliente tiene que mandar el pedido completo
 * de una sola vez.
 *
 * Solo se guardan los turnos de texto (rol "user" o "assistant"). Las llamadas
 * a herramientas y sus resultados no se persisten a propósito: la API de OpenAI
 * exige que cada tool_call venga seguida de su resultado dentro del mismo
 * pedido, así que reconstruir secuencias parciales desde la base rompería el
 * contrato y el modelo devolvería error.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Entity
@Table(name = "conversacion_mensajes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversacionMensaje {

    /**
     * Identificador único del mensaje.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Local dueño de la conversación (Multi-Tenant).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id", nullable = false)
    private Local local;

    /**
     * Identificador del cliente en el canal. Para WhatsApp, su número.
     */
    @Column(nullable = false, length = 64)
    private String remitente;

    /**
     * Quién habló: "user" (el cliente) o "assistant" (el bot).
     */
    @Column(nullable = false, length = 16)
    private String rol;

    /**
     * El texto del mensaje.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    /**
     * Cuándo se registró.
     */
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void alGuardar() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }
}
