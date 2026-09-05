package com.dioburger.service;

import com.dioburger.model.entity.Pedido;
import com.dioburger.model.enums.Modalidad;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para enviar notificaciones a n8n via webhooks.
 * Notifica al cliente cuando su pedido está listo o en camino.
 * 
 * @author Dio Burger Team
 * @version 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookNotificacionService {

    private final WebhookService webhookService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Notifica al cliente que su pedido está listo para retirar.
     * Solo aplica para modalidad TAKE_AWAY (RETIRAR).
     * 
     * @param pedido Pedido que está listo
     */
    public void notificarPedidoListo(Pedido pedido) {
        String webhookUrl = pedido.getLocal().getConfiguracion().getUrlWebhookNotificaciones();

        // Validar URL del webhook
        if (!webhookService.validarWebhookUrl(webhookUrl, pedido.getLocal().getNombre())) {
            return;
        }

        if (pedido.getModalidad() != Modalidad.RETIRAR) {
            log.debug("Pedido {} no es RETIRAR, no se envía notificación de 'listo'", pedido.getId());
            return;
        }

        Map<String, Object> payload = construirPayload(pedido, "LISTO_PARA_RETIRAR");
        webhookService.enviarAsync(webhookUrl, payload, "NOTIFICACION_LISTO", pedido.getId());
    }

    /**
     * Notifica al cliente que su pedido está en camino.
     * Solo aplica para modalidad DELIVERY.
     * 
     * @param pedido Pedido en camino
     */
    public void notificarPedidoEnCamino(Pedido pedido) {
        String webhookUrl = pedido.getLocal().getConfiguracion().getUrlWebhookNotificaciones();

        // Validar URL del webhook
        if (!webhookService.validarWebhookUrl(webhookUrl, pedido.getLocal().getNombre())) {
            return;
        }

        if (pedido.getModalidad() != Modalidad.DELIVERY) {
            log.debug("Pedido {} no es DELIVERY, no se envía notificación de 'en camino'", pedido.getId());
            return;
        }

        Map<String, Object> payload = construirPayload(pedido, "EN_CAMINO");
        webhookService.enviarAsync(webhookUrl, payload, "NOTIFICACION_EN_CAMINO", pedido.getId());
    }

    /**
     * Construye el payload para enviar al webhook de n8n.
     * 
     * @param pedido Pedido a notificar
     * @param tipoNotificacion Tipo de notificación (LISTO_PARA_RETIRAR, EN_CAMINO)
     * @return Mapa con los datos del pedido
     */
    private Map<String, Object> construirPayload(Pedido pedido, String tipoNotificacion) {
        Map<String, Object> payload = new HashMap<>();

        // Datos básicos
        payload.put("tipoNotificacion", tipoNotificacion);
        payload.put("pedidoId", pedido.getId());
        payload.put("estado", pedido.getEstado().name());

        // Datos del cliente
        Map<String, String> cliente = new HashMap<>();
        cliente.put("nombre", pedido.getCliente().getNombre());
        cliente.put("telefono", pedido.getCliente().getTelefono());
        payload.put("cliente", cliente);

        // Datos del local
        Map<String, String> local = new HashMap<>();
        local.put("nombre", pedido.getLocal().getNombre());
        local.put("direccion", pedido.getLocal().getDireccion());
        local.put("telefono", pedido.getLocal().getTelefono());
        payload.put("local", local);

        // Datos del pedido
        payload.put("modalidad", pedido.getModalidad().name());
        payload.put("total", pedido.getTotal());
        payload.put("horaPedido", pedido.getHoraPedido().format(TIME_FORMATTER));

        if (pedido.getModalidad() == Modalidad.DELIVERY && pedido.getDireccionEnvio() != null) {
            payload.put("direccionEnvio", pedido.getDireccionEnvio());
        }

        // Items del pedido (resumen)
        String itemsResumen = pedido.getItems().stream()
                .map(item -> String.format("%dx %s", item.getCantidad(), item.getProducto().getNombre()))
                .collect(Collectors.joining(", "));
        payload.put("items", itemsResumen);

        return payload;
    }
}
