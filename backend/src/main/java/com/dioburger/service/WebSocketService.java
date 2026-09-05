package com.dioburger.service;

import com.dioburger.model.entity.Pedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio para emitir eventos en tiempo real vía WebSocket.
 * Comunica nuevos pedidos a la pantalla de cocina del local.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Emite un nuevo pedido al topic específico del local.
     * Los clientes WebSocket suscritos al topic recibirán el pedido.
     *
     * Canal: /topic/pedidos/{telefonoLocal}
     *
     * @param pedido Pedido a emitir
     */
    public void emitirPedido(Pedido pedido) {
        String telefonoLocal = pedido.getLocal().getTelefono();
        String topic = "/topic/pedidos/" + telefonoLocal;

        log.info("📡 Emitiendo pedido {} al topic {}", pedido.getId(), topic);

        try {
            messagingTemplate.convertAndSend(topic, pedido);
            log.info("✅ Pedido {} emitido exitosamente vía WebSocket", pedido.getId());
        } catch (Exception e) {
            log.error("❌ Error al emitir pedido {} vía WebSocket: {}",
                    pedido.getId(), e.getMessage(), e);
        }
    }

    /**
     * Emite una actualización de estado de pedido.
     *
     * @param pedido Pedido actualizado
     */
    public void emitirActualizacionPedido(Pedido pedido) {
        String telefonoLocal = pedido.getLocal().getTelefono();
        String topic = "/topic/pedidos/" + telefonoLocal + "/actualizaciones";

        log.info("📡 Emitiendo actualización de pedido {} al topic {}", pedido.getId(), topic);

        try {
            messagingTemplate.convertAndSend(topic, pedido);
            log.info("✅ Actualización emitida exitosamente");
        } catch (Exception e) {
            log.error("❌ Error al emitir actualización: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifica que un pedido fue modificado.
     *
     * @param pedido Pedido modificado
     */
    public void notificarPedidoModificado(Pedido pedido) {
        String telefonoLocal = pedido.getLocal().getTelefono();
        String topic = "/topic/pedidos/" + telefonoLocal + "/modificados";

        log.info("✏️ Notificando modificación del pedido {} al topic {}", pedido.getId(), topic);

        try {
            messagingTemplate.convertAndSend(topic, pedido);
            log.info("✅ Modificación notificada exitosamente");
        } catch (Exception e) {
            log.error("❌ Error al notificar modificación: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifica que un pedido fue cancelado.
     *
     * @param pedido Pedido cancelado
     */
    public void notificarPedidoCancelado(Pedido pedido) {
        String telefonoLocal = pedido.getLocal().getTelefono();
        String topic = "/topic/pedidos/" + telefonoLocal + "/cancelados";

        log.info("❌ Notificando cancelación del pedido {} al topic {}", pedido.getId(), topic);

        try {
            messagingTemplate.convertAndSend(topic, pedido);
            log.info("✅ Cancelación notificada exitosamente");
        } catch (Exception e) {
            log.error("❌ Error al notificar cancelación: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifica que se asignó un repartidor a un pedido DELIVERY.
     * El panel del empleado recibirá la actualización en tiempo real.
     *
     * @param pedido Pedido con repartidor asignado
     */
    public void notificarRepartidorAsignado(Pedido pedido) {
        String telefonoLocal = pedido.getLocal().getTelefono();
        String topic = "/topic/pedidos/" + telefonoLocal + "/repartidor-asignado";

        log.info("🚚 Notificando asignación de repartidor para pedido {} al topic {}", 
                pedido.getId(), topic);

        try {
            messagingTemplate.convertAndSend(topic, pedido);
            log.info("✅ Asignación de repartidor notificada exitosamente");
        } catch (Exception e) {
            log.error("❌ Error al notificar asignación de repartidor: {}", e.getMessage(), e);
        }
    }
}

