package com.dioburger.controller;

import com.dioburger.model.entity.Pedido;
import com.dioburger.model.enums.EstadoPedido;
import com.dioburger.model.enums.Modalidad;
import com.dioburger.repository.PedidoRepository;
import com.dioburger.service.WebSocketService;
import com.dioburger.service.WebhookNotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones de cocina.
 * Accesible para usuarios con rol ROLE_ADMIN o ROLE_COCINA.
 * 
 * Endpoints:
 * - GET /api/cocina/pedidos - Listar pedidos activos
 * - PATCH /api/cocina/pedidos/{id}/estado - Cambiar estado de pedido
 * - GET /api/cocina/pedidos/historial - Historial de pedidos
 * 
 * @author Dio Burger Team
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/cocina")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'COCINA')")
public class CocinaController extends BaseController {

    private final PedidoRepository pedidoRepository;
    private final WebSocketService webSocketService;
    private final WebhookNotificacionService webhookNotificacionService;

    /**
     * Lista los pedidos activos (no entregados ni cancelados).
     * 
     * @param telefonoLocal Teléfono del local
     * @return Lista de pedidos activos
     */
    @GetMapping("/pedidos")
    public ResponseEntity<List<Pedido>> listarPedidosActivos(
            @RequestParam String telefonoLocal) {
        
        logOperacionInicio("Cocina lista pedidos activos", telefonoLocal);

        var local = obtenerLocal(telefonoLocal);

        // Pedidos activos: PENDIENTE, CONFIRMADO, EN_PREPARACION, LISTO
        List<Pedido> pedidosActivos = pedidoRepository.findByLocalAndEstadoIn(
                local,
                List.of(
                        EstadoPedido.PENDIENTE,
                        EstadoPedido.CONFIRMADO,
                        EstadoPedido.EN_PREPARACION,
                        EstadoPedido.LISTO
                )
        );

        logOperacionExito("Pedidos activos encontrados", String.valueOf(pedidosActivos.size()));

        return ResponseEntity.ok(pedidosActivos);
    }

    /**
     * Cambia el estado de un pedido.
     * Notifica el cambio vía WebSocket a los suscriptores.
     * 
     * @param id ID del pedido
     * @param nuevoEstado Nuevo estado del pedido
     * @return Pedido actualizado
     */
    @PatchMapping("/pedidos/{id}/estado")
    public ResponseEntity<Pedido> cambiarEstadoPedido(
            @PathVariable Long id,
            @RequestParam EstadoPedido nuevoEstado) {
        
        log.info("👨‍🍳 Cocina cambia estado del pedido {} a {}", id, nuevoEstado);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(nuevoEstado);

        Pedido pedidoActualizado = pedidoRepository.save(pedido);

        log.info("✅ Pedido {} actualizado: {} → {}", 
                id, estadoAnterior, nuevoEstado);

        // Emitir actualización vía WebSocket
        try {
            webSocketService.emitirActualizacionPedido(pedidoActualizado);
            log.debug("📡 Actualización de pedido emitida vía WebSocket");
        } catch (Exception e) {
            log.warn("⚠️ Error al emitir actualización WebSocket: {}", e.getMessage());
            // No fallar la operación si WebSocket falla
        }

        return ResponseEntity.ok(pedidoActualizado);
    }

    /**
     * Obtiene el historial completo de pedidos de un local.
     * Incluye pedidos entregados y cancelados.
     * 
     * @param telefonoLocal Teléfono del local
     * @return Lista de todos los pedidos
     */
    @GetMapping("/pedidos/historial")
    public ResponseEntity<List<Pedido>> obtenerHistorialPedidos(
            @RequestParam String telefonoLocal) {
        
        log.info("👨‍🍳 Cocina obtiene historial de pedidos del local: {}", telefonoLocal);

        var local = localService.buscarPorTelefono(telefonoLocal);

        List<Pedido> todosPedidos = pedidoRepository.findByLocalOrderByHoraPedidoDesc(local);

        log.info("✅ {} pedidos en historial", todosPedidos.size());

        return ResponseEntity.ok(todosPedidos);
    }

    /**
     * Obtiene los detalles completos de un pedido específico.
     * 
     * @param id ID del pedido
     * @return Pedido con todos sus items
     */
    @GetMapping("/pedidos/{id}")
    public ResponseEntity<Pedido> obtenerDetallePedido(@PathVariable Long id) {
        
        log.info("👨‍🍳 Cocina obtiene detalle del pedido: {}", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        log.info("✅ Pedido {} encontrado - Estado: {}", id, pedido.getEstado());

        return ResponseEntity.ok(pedido);
    }

    /**
     * Marca un pedido como "EN_PREPARACION".
     * Atajo conveniente para iniciar la preparación.
     * 
     * @param id ID del pedido
     * @return Pedido actualizado
     */
    @PostMapping("/pedidos/{id}/iniciar-preparacion")
    public ResponseEntity<Pedido> iniciarPreparacion(@PathVariable Long id) {
        
        log.info("👨‍🍳 Cocina inicia preparación del pedido: {}", id);

        return cambiarEstadoPedido(id, EstadoPedido.EN_PREPARACION);
    }

    /**
     * Marca un pedido como "LISTO".
     * Atajo conveniente para marcar como listo para entrega.
     * 
     * Si el pedido es TAKE_AWAY (RETIRAR), envía notificación webhook a n8n
     * para que avise al cliente que puede retirar su pedido.
     * 
     * @param id ID del pedido
     * @return Pedido actualizado
     */
    @PostMapping("/pedidos/{id}/marcar-listo")
    public ResponseEntity<Pedido> marcarListo(@PathVariable Long id) {
        
        log.info("👨‍🍳 Cocina marca pedido como listo: {}", id);

        ResponseEntity<Pedido> response = cambiarEstadoPedido(id, EstadoPedido.LISTO);
        
        // Enviar notificación si es TAKE_AWAY
        Pedido pedido = response.getBody();
        if (pedido != null && pedido.getModalidad() == Modalidad.RETIRAR) {
            webhookNotificacionService.notificarPedidoListo(pedido);
        }

        return response;
    }

    /**
     * Marca un pedido como "EN_CAMINO".
     * Solo aplica para pedidos DELIVERY.
     * Envía notificación webhook a n8n para avisar al cliente.
     * 
     * @param id ID del pedido
     * @return Pedido actualizado
     */
    @PostMapping("/pedidos/{id}/marcar-en-camino")
    public ResponseEntity<Pedido> marcarEnCamino(@PathVariable Long id) {
        
        log.info("🚗 Cocina marca pedido en camino: {}", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getModalidad() != Modalidad.DELIVERY) {
            log.warn("⚠️ Intento de marcar EN_CAMINO un pedido que no es DELIVERY");
            return ResponseEntity.badRequest().build();
        }

        ResponseEntity<Pedido> response = cambiarEstadoPedido(id, EstadoPedido.EN_CAMINO);
        
        // Enviar notificación
        Pedido pedidoActualizado = response.getBody();
        if (pedidoActualizado != null) {
            webhookNotificacionService.notificarPedidoEnCamino(pedidoActualizado);
        }

        return response;
    }

    /**
     * Marca un pedido como "ENTREGADO".
     * Atajo conveniente para finalizar el pedido.
     * 
     * @param id ID del pedido
     * @return Pedido actualizado
     */
    @PostMapping("/pedidos/{id}/entregar")
    public ResponseEntity<Pedido> entregarPedido(@PathVariable Long id) {
        
        log.info("👨‍🍳 Cocina entrega pedido: {}", id);

        return cambiarEstadoPedido(id, EstadoPedido.ENTREGADO);
    }
}
