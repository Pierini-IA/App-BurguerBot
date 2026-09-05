package com.dioburger.service;

import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Pedido;
import com.dioburger.model.entity.PedidoItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para enviar tickets de pedidos a una impresora remota vía webhook.
 * Usa WebClient (reactivo) para no bloquear el hilo principal.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrinterService {

    private final WebClient webClient;

    /**
     * Envía un ticket de pedido a la impresora configurada del local.
     * Si la impresión no está activa o no hay URL configurada, no hace nada.
     *
     * @param pedido Pedido a imprimir
     */
    public void enviarTicket(Pedido pedido) {
        ConfiguracionLocal config = pedido.getLocal().getConfiguracion();

        // Verificar si la impresión está activa
        if (config == null || !config.getImpresionActiva()) {
            log.debug("Impresión desactivada para local {}", pedido.getLocal().getNombre());
            return;
        }

        String webhookUrl = config.getUrlWebhookImpresora();

        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("⚠️ URL de webhook no configurada para local {}",
                    pedido.getLocal().getNombre());
            return;
        }

        log.info("🖨️ Enviando ticket del pedido {} a impresora: {}",
                pedido.getId(), webhookUrl);

        // Construir el payload del ticket
        Map<String, Object> payload = construirPayloadTicket(pedido);

        // Enviar de forma asíncrona
        webClient.post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> {
                    log.info("✅ Ticket del pedido {} enviado exitosamente (HTTP {})",
                            pedido.getId(), response.getStatusCode());
                })
                .doOnError(error -> {
                    log.error("❌ Error al enviar ticket del pedido {}: {}",
                            pedido.getId(), error.getMessage(), error);
                })
                .onErrorResume(error -> {
                    // No propagar el error, solo loguearlo
                    return Mono.empty();
                })
                .subscribe(); // Dispara la petición asíncrona
    }

    /**
     * Construye el payload JSON del ticket para enviar a la impresora.
     *
     * @param pedido Pedido a convertir en ticket
     * @return Map con los datos del ticket
     */
    private Map<String, Object> construirPayloadTicket(Pedido pedido) {
        Map<String, Object> ticket = new HashMap<>();

        // Información del pedido
        ticket.put("pedidoId", pedido.getId());
        ticket.put("estado", pedido.getEstado().name());
        ticket.put("modalidad", pedido.getModalidad().name());
        ticket.put("total", pedido.getTotal());
        ticket.put("horaPedido", pedido.getHoraPedido().toString());

        // Información del cliente
        Map<String, String> cliente = new HashMap<>();
        cliente.put("nombre", pedido.getCliente().getNombre());
        cliente.put("telefono", pedido.getCliente().getTelefono());
        ticket.put("cliente", cliente);

        // Dirección (si es delivery)
        if (pedido.getDireccionEnvio() != null) {
            ticket.put("direccionEnvio", pedido.getDireccionEnvio());
        }

        // Items del pedido
        List<Map<String, Object>> items = pedido.getItems().stream()
                .map(this::convertirItemAMap)
                .collect(Collectors.toList());
        ticket.put("items", items);

        // Información del local
        Map<String, String> local = new HashMap<>();
        local.put("nombre", pedido.getLocal().getNombre());
        local.put("telefono", pedido.getLocal().getTelefono());
        ticket.put("local", local);

        return ticket;
    }

    /**
     * Convierte un PedidoItem a Map para el payload.
     *
     * @param item Item del pedido
     * @return Map con datos del item
     */
    private Map<String, Object> convertirItemAMap(PedidoItem item) {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("producto", item.getProducto().getNombre());
        itemMap.put("cantidad", item.getCantidad());
        
        if (item.getObservaciones() != null && !item.getObservaciones().isBlank()) {
            itemMap.put("observaciones", item.getObservaciones());
        }
        
        return itemMap;
    }
}
