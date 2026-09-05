package com.dioburger.controller;

import com.dioburger.exception.NotFoundException;
import com.dioburger.mapper.PedidoMapper;
import com.dioburger.model.dto.*;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Pedido;
import com.dioburger.model.enums.Feature;
import com.dioburger.model.enums.Modalidad;
import com.dioburger.repository.LocalRepository;
import com.dioburger.repository.PedidoRepository;
import com.dioburger.security.RequiresFeature;
import com.dioburger.service.CatalogoService;
import com.dioburger.service.LocalService;
import com.dioburger.service.MenuService;
import com.dioburger.service.PedidoService;
import com.dioburger.service.ReservaService;
import com.dioburger.service.WebSocketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * API REST genérica del bot (menú, pedidos, reservas, asignación de repartidores).
 * El flujo real de WhatsApp/comentarios de Meta ya no pasa por acá: llega directo
 * a {@link MetaWebhookController}, que invoca al motor de IA del paquete
 * {@code com.dioburger.ai} in-process. Estos endpoints se mantienen como API
 * genérica para testing manual (Postman/Swagger) e integraciones futuras.
 *
 * @author Dio Burger Team
 * @version 2.0
 */
@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
@Slf4j
public class BotController {

    private final CatalogoService catalogoService;
    private final MenuService menuService;
    private final PedidoService pedidoService;
    private final PedidoMapper pedidoMapper;
    private final ReservaService reservaService;
    private final PedidoRepository pedidoRepository;
    private final WebSocketService webSocketService;
    private final LocalService localService;
    @SuppressWarnings("unused")
    private final LocalRepository localRepository;  // Mantener para compatibilidad con AOP

    /**
     * Obtiene el menú completo del local agrupado por categorías.
     * El bot de WhatsApp ya no llama a este endpoint (usa {@code CatalogoService}
     * directo, in-process); se mantiene para testing manual e integraciones futuras.
     *
     * Endpoint: GET /api/bot/menu/{telefonoLocal}
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @return MenuCompletoDTO con productos agrupados por categorías, extras, horarios y modalidades
     *
     * @apiNote Ejemplo de uso:
     * GET /api/bot/menu/+5491187654321
     *
     * Response:
     * {
     *   "local": {
     *     "nombre": "Dio Burger Palermo",
     *     "direccion": "Av. Córdoba 1234",
     *     "telefono": "+5491187654321"
     *   },
     *   "categorias": [
     *     {
     *       "id": 1,
     *       "nombre": "Hamburguesas",
     *       "productos": [
     *         {
     *           "id": 1,
     *           "nombre": "Clásica",
     *           "descripcion": "Burger simple con doble cheddar...",
     *           "precio": 9500.0,
     *           "disponible": true,
     *           "extrasDisponibles": [
     *             {
     *               "id": 1,
     *               "nombre": "Queso Extra",
     *               "precioAdicional": 500.0,
     *               "esObligatorio": false
     *             }
     *           ]
     *         }
     *       ]
     *     }
     *   ],
     *   "horariosSugeridos": ["20:00", "20:15", "20:30"],
     *   "modalidadesPermitidas": ["DELIVERY", "RETIRAR"],
     *   "permiteReservas": true
     * }
     */
    @GetMapping("/menu/{telefonoLocal}")
    @RequiresFeature(Feature.BOT_WHATSAPP)
    public ResponseEntity<MenuCompletoDTO> obtenerMenu(
            @PathVariable String telefonoLocal) {

        log.info("🤖 Bot solicita menú para local: {}", telefonoLocal);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        MenuCompletoDTO menu = catalogoService.obtenerCatalogoCompleto(telefonoLocal);

        log.info("✅ Menú generado exitosamente para {}: {} categorías disponibles",
                telefonoLocal, menu.getCategorias().size());

        return ResponseEntity.ok(menu);
    }

    /**
     * Crea un nuevo pedido desde el bot de WhatsApp.
     * El pedido se procesa de forma transaccional e idempotente.
     *
     * Endpoint: POST /api/bot/pedido/{telefonoLocal}
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @param dto Datos del pedido con requestId para idempotencia
     * @return PedidoResponseDTO con los datos del pedido creado
     *
     * @apiNote Ejemplo de uso:
     * POST /api/bot/pedido/+5491187654321
     *
     * Request:
     * {
     *   "requestId": "uuid-generado-por-n8n",
     *   "cliente": {
     *     "nombre": "Adrian",
     *     "telefono": "+5491123456789"
     *   },
     *   "modalidad": "DELIVERY",
     *   "direccionEnvio": "Roque Saenz Peña 551",
     *   "medioPago": "TRANSFERENCIA",
     *   "items": [
     *     {
     *       "productoId": 2,
     *       "cantidad": 2,
     *       "observaciones": "sin lechuga"
     *     }
     *   ],
     *   "horaPedido": "20:15"
     * }
     *
     * Response:
     * {
     *   "id": 123,
     *   "requestId": "uuid-generado-por-n8n",
     *   "estado": "PENDIENTE",
     *   "total": 22000.0,
     *   "horaPedido": "2025-10-21T20:15:00",
     *   "modalidad": "DELIVERY",
     *   "medioPago": "TRANSFERENCIA",
     *   "estadoPago": "PENDIENTE"
     * }
     */
    @PostMapping("/pedido/{telefonoLocal}")
    @RequiresFeature(Feature.BOT_WHATSAPP)
    public ResponseEntity<PedidoResponseDTO> crearPedido(
            @PathVariable String telefonoLocal,
            @Valid @RequestBody PedidoDTO dto) {

        log.info("🤖 Bot crea pedido para local {}: requestId={}",
                telefonoLocal, dto.getRequestId());

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        Pedido pedido = pedidoService.crearPedido(dto, telefonoLocal);
        PedidoResponseDTO response = pedidoMapper.toResponseDTO(pedido);

        log.info("✅ Pedido {} creado exitosamente. Total: ${}",
                pedido.getId(), pedido.getTotal());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Crea una nueva reserva de mesa desde el bot de WhatsApp.
     * Asigna automáticamente las mesas necesarias según la cantidad de personas.
     *
     * Endpoint: POST /api/bot/reserva/{telefonoLocal}
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @param dto Datos de la reserva con requestId para idempotencia
     * @return ReservaResponseDTO con los datos de la reserva creada
     *
     * @apiNote Ejemplo de uso:
     * POST /api/bot/reserva/+5491187654321
     *
     * Request:
     * {
     *   "requestId": "uuid-reserva-n8n",
     *   "cliente": {
     *     "nombre": "Adrian",
     *     "telefono": "+5491123456789"
     *   },
     *   "horaReserva": "2025-10-21T21:00:00",
     *   "numeroPersonas": 4,
     *   "observaciones": "Cumpleaños, necesitamos mesa cerca de la ventana"
     * }
     *
     * Response:
     * {
     *   "id": 45,
     *   "cliente": {
     *     "nombre": "Adrian",
     *     "telefono": "+5491123456789"
     *   },
     *   "horaReserva": "2025-10-21T21:00:00",
     *   "numeroPersonas": 4,
     *   "mesas": [
     *     { "id": 5, "numero": 5, "capacidad": 4, "disponible": false }
     *   ],
     *   "estado": "CONFIRMADA",
     *   "gastoTotal": 0.0,
     *   "observaciones": "Cumpleaños, necesitamos mesa cerca de la ventana"
     * }
     */
    @PostMapping("/reserva/{telefonoLocal}")
    @RequiresFeature(Feature.SISTEMA_RESERVAS)
    public ResponseEntity<ReservaResponseDTO> crearReserva(
            @PathVariable String telefonoLocal,
            @Valid @RequestBody ReservaDTO dto) {

        log.info("🤖 Bot crea reserva para local {}", telefonoLocal);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        ReservaResponseDTO response = reservaService.crearReserva(telefonoLocal, dto);

        log.info("✅ Reserva {} creada exitosamente para {} personas. Mesas asignadas: {}",
                response.getId(), response.getNumeroPersonas(), response.getMesas().size());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Modifica un pedido existente desde el bot de WhatsApp.
     * Permite cambiar items, horario, dirección, etc.
     * 
     * IMPORTANTE: Solo se pueden modificar pedidos en estado PENDIENTE.
     *
     * Endpoint: PUT /api/bot/pedido/{telefonoLocal}/{pedidoId}
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @param pedidoId ID del pedido a modificar
     * @param dto Datos de modificación
     * @return PedidoResponseDTO con los datos del pedido modificado
     *
     * @apiNote Ejemplo de uso:
     * PUT /api/bot/pedido/+5491187654321/123
     *
     * Request:
     * {
     *   "requestId": "uuid-modificacion-generado-por-n8n",
     *   "items": [
     *     {
     *       "productoId": 3,
     *       "cantidad": 1,
     *       "observaciones": "sin cebolla"
     *     }
     *   ],
     *   "horaPedido": "21:00",
     *   "direccionEnvio": "Nueva dirección 123",
     *   "observaciones": "Modificado: cambio de productos"
     * }
     *
     * Response:
     * {
     *   "id": 123,
     *   "requestId": "uuid-original",
     *   "estado": "PENDIENTE",
     *   "total": 12000.0,
     *   "horaPedido": "2025-10-21T21:00:00",
     *   "modalidad": "DELIVERY",
     *   "medioPago": "TRANSFERENCIA",
     *   "estadoPago": "PENDIENTE"
     * }
     */
    @PutMapping("/pedido/{telefonoLocal}/{pedidoId}")
    @RequiresFeature(Feature.BOT_WHATSAPP)
    public ResponseEntity<PedidoResponseDTO> modificarPedido(
            @PathVariable String telefonoLocal,
            @PathVariable Long pedidoId,
            @Valid @RequestBody com.dioburger.model.dto.ModificarPedidoDTO dto) {

        log.info("🤖 Bot modifica pedido {} del local {}", pedidoId, telefonoLocal);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        Pedido pedido = pedidoService.modificarPedido(pedidoId, dto, telefonoLocal);
        PedidoResponseDTO response = pedidoMapper.toResponseDTO(pedido);

        log.info("✅ Pedido {} modificado exitosamente. Nuevo total: ${}",
                pedido.getId(), pedido.getTotal());

        return ResponseEntity.ok(response);
    }

    /**
     * Cancela un pedido existente desde el bot de WhatsApp.
     * Valida que haya tiempo de anticipación suficiente según la configuración del local.
     * 
     * IMPORTANTE: Solo se pueden cancelar pedidos en estado PENDIENTE o EN_PREPARACION.
     * El stock de ingredientes se restaura automáticamente.
     *
     * Endpoint: DELETE /api/bot/pedido/{telefonoLocal}/{pedidoId}
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @param pedidoId ID del pedido a cancelar
     * @param dto Datos de cancelación (requestId y motivo opcional)
     * @return PedidoResponseDTO con los datos del pedido cancelado
     *
     * @apiNote Ejemplo de uso:
     * DELETE /api/bot/pedido/+5491187654321/123
     *
     * Request:
     * {
     *   "requestId": "uuid-cancelacion-generado-por-n8n",
     *   "motivo": "El cliente ya no puede asistir"
     * }
     *
     * Response:
     * {
     *   "id": 123,
     *   "requestId": "uuid-original",
     *   "estado": "CANCELADO",
     *   "total": 22000.0,
     *   "horaPedido": "2025-10-21T20:15:00",
     *   "modalidad": "DELIVERY",
     *   "medioPago": "TRANSFERENCIA",
     *   "estadoPago": "PENDIENTE"
     * }
     *
     * @throws com.dioburger.exception.CancelacionNoPermitidaException 
     *         si no hay anticipación suficiente
     */
    @DeleteMapping("/pedido/{telefonoLocal}/{pedidoId}")
    @RequiresFeature(Feature.BOT_WHATSAPP)
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(
            @PathVariable String telefonoLocal,
            @PathVariable Long pedidoId,
            @Valid @RequestBody com.dioburger.model.dto.CancelarPedidoDTO dto) {

        log.info("🤖 Bot cancela pedido {} del local {}: motivo={}",
                pedidoId, telefonoLocal, dto.getMotivo());

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        Pedido pedido = pedidoService.cancelarPedido(pedidoId, dto, telefonoLocal);
        PedidoResponseDTO response = pedidoMapper.toResponseDTO(pedido);

        log.info("✅ Pedido {} cancelado exitosamente",
                pedido.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los detalles de una reserva específica.
     *
     * Endpoint: GET /api/bot/reserva/{telefonoLocal}/{reservaId}
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @param reservaId ID de la reserva a consultar
     * @return ReservaResponseDTO con los datos de la reserva
     *
     * @apiNote Ejemplo de uso:
     * GET /api/bot/reserva/+5491187654321/45
     *
     * Response:
     * {
     *   "id": 45,
     *   "cliente": {
     *     "nombre": "Adrian",
     *     "telefono": "+5491123456789"
     *   },
     *   "horaReserva": "2025-10-21T21:00:00",
     *   "numeroPersonas": 4,
     *   "mesas": [
     *     { "id": 5, "numero": 5, "capacidad": 4, "disponible": false }
     *   ],
     *   "estado": "CONFIRMADA",
     *   "gastoTotal": 0.0,
     *   "observaciones": "Cumpleaños, necesitamos mesa cerca de la ventana"
     * }
     */
    @GetMapping("/reserva/{telefonoLocal}/{reservaId}")
    @RequiresFeature(Feature.SISTEMA_RESERVAS)
    public ResponseEntity<ReservaResponseDTO> obtenerReserva(
            @PathVariable String telefonoLocal,
            @PathVariable Long reservaId) {

        log.info("🤖 Bot consulta reserva {} para local {}", reservaId, telefonoLocal);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        ReservaResponseDTO response = reservaService.obtenerReservaPorId(telefonoLocal, reservaId);

        log.info("✅ Reserva {} encontrada. Estado: {}", reservaId, response.getEstado());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene las hamburguesas disponibles según el stock actual de ingredientes.
     * Este endpoint es consumido por el bot para mostrar SOLO las hamburguesas
     * que se pueden preparar en este momento, con la cantidad máxima disponible.
     *
     * Endpoint: GET /api/bot/hamburguesas-disponibles/{telefonoLocal}
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @return Lista de hamburguesas con nombre, descripción, precio y cantidad disponible
     *
     * @apiNote Ejemplo de uso:
     * GET /api/bot/hamburguesas-disponibles/+5491187654321
     *
     * Response:
     * {
     *   "local": {
     *     "nombre": "Dio Burger Palermo",
     *     "telefono": "+5491187654321"
     *   },
     *   "hamburguesas": [
     *     {
     *       "id": 1,
     *       "nombre": "Clásica",
     *       "descripcion": "Burger simple con doble cheddar...",
     *       "precio": 9500.0,
     *       "cantidadDisponible": 22
     *     },
     *     {
     *       "id": 6,
     *       "nombre": "BBQ",
     *       "descripcion": "Burger con panceta...",
     *       "precio": 11500.0,
     *       "cantidadDisponible": 0
     *     }
     *   ],
     *   "totalHamburguesas": 10,
     *   "hamburguesasConStock": 9,
     *   "hamburguesasAgotadas": 1
     * }
     */
    @GetMapping("/hamburguesas-disponibles/{telefonoLocal}")
    @RequiresFeature(Feature.BOT_WHATSAPP)
    public ResponseEntity<java.util.Map<String, Object>> obtenerHamburguesasDisponibles(
            @PathVariable String telefonoLocal) {

        log.info("🍔 Bot consulta hamburguesas disponibles para local: {}", telefonoLocal);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        var response = menuService.obtenerHamburguesasConStock(telefonoLocal);

        log.info("✅ {} hamburguesas disponibles de {} totales", 
                response.get("hamburguesasConStock"), 
                response.get("totalHamburguesas"));

        return ResponseEntity.ok(response);
    }

    /**
     * Asigna un repartidor a un pedido DELIVERY.
     * Este endpoint es llamado por n8n después de encontrar un repartidor disponible.
     *
     * Endpoint: POST /api/bot/pedido/{telefonoLocal}/{pedidoId}/asignar-repartidor
     *
     * @param telefonoLocal Teléfono único del local (Multi-Tenant ID)
     * @param pedidoId ID del pedido a asignar repartidor
     * @param dto Datos del repartidor asignado
     * @return Confirmación de asignación
     *
     * @apiNote Ejemplo de uso:
     * POST /api/bot/pedido/+5491187654321/123/asignar-repartidor
     *
     * Request:
     * {
     *   "repartidorId": "repartidor-456",
     *   "repartidorNombre": "Juan Pérez",
     *   "repartidorTelefono": "+549111234567",
     *   "urlTracking": "https://tracking.app/entrega/abc123"
     * }
     *
     * Response:
     * {
     *   "mensaje": "Repartidor asignado exitosamente",
     *   "pedidoId": 123,
     *   "repartidor": "Juan Pérez"
     * }
     */
    @PostMapping("/pedido/{telefonoLocal}/{pedidoId}/asignar-repartidor")
    @RequiresFeature(Feature.ASIGNACION_REPARTIDORES)
    public ResponseEntity<java.util.Map<String, Object>> asignarRepartidor(
            @PathVariable String telefonoLocal,
            @PathVariable Long pedidoId,
            @Valid @RequestBody AsignarRepartidorDTO dto) {

        log.info("🚚 n8n asigna repartidor {} al pedido {}", dto.getRepartidorNombre(), pedidoId);

        // Obtener local para validación de plan (requerido por @RequiresFeature)
        @SuppressWarnings("unused")
        Local local = localService.buscarPorTelefono(telefonoLocal);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado con ID: " + pedidoId));

        // Validar que el pedido pertenezca al local correcto
        if (!pedido.getLocal().getTelefono().equals(telefonoLocal)) {
            log.error("❌ Pedido {} no pertenece al local {}", pedidoId, telefonoLocal);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "Pedido no pertenece a este local"));
        }

        // Validar que sea un pedido DELIVERY
        if (pedido.getModalidad() != Modalidad.DELIVERY) {
            log.error("❌ Pedido {} no es DELIVERY, es {}", pedidoId, pedido.getModalidad());
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "Solo se pueden asignar repartidores a pedidos DELIVERY"));
        }

        // Asignar repartidor
        pedido.setRepartidorId(dto.getRepartidorId());
        pedido.setRepartidorNombre(dto.getRepartidorNombre());
        pedido.setRepartidorTelefono(dto.getRepartidorTelefono());
        pedido.setUrlTrackingDelivery(dto.getUrlTracking());
        pedido.setHoraAsignacionRepartidor(LocalDateTime.now());

        pedidoRepository.save(pedido);

        log.info("✅ Repartidor {} asignado al pedido {}", dto.getRepartidorNombre(), pedidoId);

        // Notificar al panel del empleado vía WebSocket
        try {
            webSocketService.notificarRepartidorAsignado(pedido);
        } catch (Exception e) {
            log.error("❌ Error notificando asignación de repartidor: {}", e.getMessage());
        }

        return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Repartidor asignado exitosamente",
                "pedidoId", pedidoId,
                "repartidor", dto.getRepartidorNombre(),
                "telefono", dto.getRepartidorTelefono(),
                "asignadoEn", LocalDateTime.now().toString()
        ));
    }
}


