package com.dioburger.service;

import com.dioburger.model.entity.Pedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para gestionar webhooks de asignación automática de repartidores.
 * Se comunica con n8n para solicitar y confirmar repartidores para pedidos DELIVERY.
 *
 * @author Dio Burger Team
 * @version 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryService {

    private final WebhookService webhookService;

    /**
     * Solicita la asignación automática de un repartidor a n8n.
     * Envía los datos del pedido para que n8n busque un repartidor disponible.
     * 
     * Este método es asíncrono y no espera respuesta (fire and forget).
     * La confirmación de asignación llega posteriormente via endpoint POST.
     *
     * @param pedido Pedido DELIVERY que necesita repartidor
     */
    public void solicitarAsignacionRepartidor(Pedido pedido) {
        String webhookUrl = pedido.getLocal()
                .getConfiguracion()
                .getUrlWebhookAsignacionDelivery();

        // Validar URL del webhook
        if (!webhookService.validarWebhookUrl(webhookUrl, pedido.getLocal().getNombre())) {
            return;
        }

        log.info("📞 Solicitando repartidor para pedido {} a n8n", pedido.getId());

        // Construir payload para n8n
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", "SOLICITUD_REPARTIDOR");
        payload.put("pedidoId", pedido.getId());
        payload.put("direccionDestino", pedido.getDireccionEnvio());
        payload.put("horaPedido", pedido.getHoraPedido().toString());
        payload.put("total", pedido.getTotal());

        // Información del cliente
        Map<String, String> clienteInfo = new HashMap<>();
        clienteInfo.put("nombre", pedido.getCliente().getNombre());
        clienteInfo.put("telefono", pedido.getCliente().getTelefono());
        payload.put("cliente", clienteInfo);

        // Información del local (origen)
        Map<String, String> localInfo = new HashMap<>();
        localInfo.put("nombre", pedido.getLocal().getNombre());
        localInfo.put("direccion", pedido.getLocal().getDireccion());
        localInfo.put("telefono", pedido.getLocal().getTelefono());
        payload.put("local", localInfo);

        // Items resumidos (opcional, para que el repartidor sepa qué lleva)
        payload.put("cantidadItems", pedido.getItems().size());

        // Enviar webhook asíncrono usando servicio centralizado
        webhookService.enviarAsync(webhookUrl, payload, "SOLICITUD_REPARTIDOR", pedido.getId());
    }

    /**
     * Notifica a n8n cuando un pedido DELIVERY cambia de estado.
     * Esto permite que n8n actualice el estado en su sistema de tracking.
     *
     * @param pedido Pedido que cambió de estado
     * @param nuevoEstado Nuevo estado del pedido
     */
    public void notificarCambioEstadoDelivery(Pedido pedido, String nuevoEstado) {
        String webhookUrl = pedido.getLocal()
                .getConfiguracion()
                .getUrlWebhookAsignacionDelivery();

        // Validar URL del webhook
        if (!webhookService.validarWebhookUrl(webhookUrl, pedido.getLocal().getNombre())) {
            return;
        }

        log.info("📢 Notificando cambio de estado a n8n: pedido {} -> {}", 
                pedido.getId(), nuevoEstado);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", "CAMBIO_ESTADO_DELIVERY");
        payload.put("pedidoId", pedido.getId());
        payload.put("nuevoEstado", nuevoEstado);
        payload.put("repartidorId", pedido.getRepartidorId());

        // Enviar webhook usando servicio centralizado
        webhookService.enviarAsync(webhookUrl, payload, "CAMBIO_ESTADO_DELIVERY", pedido.getId());
    }
}
