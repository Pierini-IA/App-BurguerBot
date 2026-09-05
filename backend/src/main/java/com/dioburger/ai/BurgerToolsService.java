package com.dioburger.ai;

import com.dioburger.channels.support.JsonNav;
import com.dioburger.model.dto.*;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Pedido;
import com.dioburger.service.MenuService;
import com.dioburger.service.PedidoService;
import com.dioburger.service.ReservaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Define las tools del "vertical" hamburguesería y las ejecuta llamando a los
 * servicios de dominio ya existentes ({@link PedidoService}, {@link MenuService},
 * {@link ReservaService}) in-process — sin pasar por HTTP ni por n8n.
 *
 * Como estas llamadas no pasan por el binding de Spring MVC, las validaciones
 * {@code @Valid} de los DTOs no se disparan solas acá: los chequeos mínimos
 * imprescindibles (ej. que el pedido tenga al menos un item) se hacen a mano
 * antes de invocar al servicio.
 *
 * Cada método de ejecución nunca lanza: captura cualquier error y lo devuelve
 * como texto/JSON al modelo, para que el agent loop pueda seguir (y eventualmente
 * explicarle el problema al cliente) en vez de romper el pipeline.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BurgerToolsService {

    private static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final PedidoService pedidoService;
    private final MenuService menuService;
    private final ReservaService reservaService;
    private final ObjectMapper objectMapper;

    // ==================== Definiciones ====================

    public List<ToolDefinition> getDefinitions(boolean commentMode) {
        if (commentMode) {
            return List.of(definicionConsultarDisponibilidad(), definicionConsultarHorariosYUbicacion());
        }
        return List.of(
                definicionConsultarDisponibilidad(),
                definicionConsultarHorariosYUbicacion(),
                definicionCrearPedido(),
                definicionModificarPedido(),
                definicionCancelarPedido(),
                definicionConsultarEstadoPedido(),
                definicionCrearReserva()
        );
    }

    // ==================== Ejecución ====================

    public String execute(String toolName, String argumentsJson, ToolExecutionContext context) {
        try {
            Map<String, Object> args = argumentsJson == null || argumentsJson.isBlank()
                    ? Map.of()
                    : objectMapper.readValue(argumentsJson, Map.class);

            return switch (toolName) {
                case "consultar_disponibilidad" -> consultarDisponibilidad(context);
                case "consultar_horarios_y_ubicacion" -> consultarHorariosYUbicacion(context);
                case "crear_pedido" -> crearPedido(args, context);
                case "modificar_pedido" -> modificarPedido(args, context);
                case "cancelar_pedido" -> cancelarPedido(args, context);
                case "consultar_estado_pedido" -> consultarEstadoPedido(context);
                case "crear_reserva" -> crearReserva(args, context);
                default -> errorJson("Tool desconocida: " + toolName);
            };
        } catch (Exception e) {
            log.error("❌ Error ejecutando tool '{}': {}", toolName, e.getMessage(), e);
            return errorJson(e.getMessage() != null ? e.getMessage() : "Error inesperado ejecutando " + toolName);
        }
    }

    // ==================== Implementaciones ====================

    private String consultarDisponibilidad(ToolExecutionContext context) {
        Map<String, Object> disponibilidad = menuService.obtenerHamburguesasConStock(context.local().getTelefono());
        return toJson(disponibilidad);
    }

    private String consultarHorariosYUbicacion(ToolExecutionContext context) {
        Local local = context.local();
        ConfiguracionLocal config = local.getConfiguracion();

        Map<String, Object> info = Map.of(
                "nombre", local.getNombre(),
                "direccion", local.getDireccion(),
                "horaApertura", config.getHoraApertura().toString(),
                "horaCierre", config.getHoraCierre().toString(),
                "permiteDelivery", config.getPermiteDelivery(),
                "permiteTakeAway", config.getPermiteTakeAway(),
                "permiteReservas", config.getPermiteReservas()
        );
        return toJson(info);
    }

    private String crearPedido(Map<String, Object> args, ToolExecutionContext context) {
        if (context.commentMode()) {
            return errorJson("No se pueden crear pedidos desde un comentario público");
        }

        List<PedidoItemDTO> items = parseItems(args.get("items"));
        if (items.isEmpty()) {
            return errorJson("El pedido debe tener al menos un producto");
        }

        PedidoDTO dto = PedidoDTO.builder()
                .requestId(context.requestSeed() + ":crear-pedido")
                .cliente(ClienteDTO.builder()
                        .nombre(context.nombreCliente() != null ? context.nombreCliente() : "Cliente WhatsApp")
                        .telefono(context.telefonoCliente())
                        .build())
                .modalidad(asString(args, "modalidad"))
                .direccionEnvio(asString(args, "direccion_envio"))
                .medioPago(asString(args, "medio_pago"))
                .items(items)
                .horaPedido(asString(args, "hora_pedido"))
                .build();

        Pedido pedido = pedidoService.crearPedido(dto, context.local().getTelefono());
        return toJson(Map.of(
                "pedidoId", pedido.getId(),
                "estado", pedido.getEstado().name(),
                "total", pedido.getTotal(),
                "modalidad", pedido.getModalidad().name()
        ));
    }

    private String modificarPedido(Map<String, Object> args, ToolExecutionContext context) {
        if (context.commentMode()) {
            return errorJson("No se pueden modificar pedidos desde un comentario público");
        }

        Long pedidoId = asLong(args, "pedido_id");
        if (pedidoId == null) {
            return errorJson("Falta pedido_id");
        }

        List<PedidoItemDTO> items = parseItems(args.get("items"));
        if (items.isEmpty()) {
            return errorJson("El pedido debe tener al menos un producto");
        }

        ModificarPedidoDTO dto = ModificarPedidoDTO.builder()
                .requestId(context.requestSeed() + ":modificar-pedido")
                .items(items)
                .horaPedido(asString(args, "hora_pedido"))
                .direccionEnvio(asString(args, "direccion_envio"))
                .observaciones(asString(args, "observaciones"))
                .build();

        Pedido pedido = pedidoService.modificarPedido(pedidoId, dto, context.local().getTelefono());
        return toJson(Map.of(
                "pedidoId", pedido.getId(),
                "estado", pedido.getEstado().name(),
                "total", pedido.getTotal()
        ));
    }

    private String cancelarPedido(Map<String, Object> args, ToolExecutionContext context) {
        if (context.commentMode()) {
            return errorJson("No se pueden cancelar pedidos desde un comentario público");
        }

        Long pedidoId = asLong(args, "pedido_id");
        if (pedidoId == null) {
            return errorJson("Falta pedido_id");
        }

        CancelarPedidoDTO dto = CancelarPedidoDTO.builder()
                .requestId(context.requestSeed() + ":cancelar-pedido")
                .motivo(asString(args, "motivo"))
                .build();

        Pedido pedido = pedidoService.cancelarPedido(pedidoId, dto, context.local().getTelefono());
        return toJson(Map.of("pedidoId", pedido.getId(), "estado", pedido.getEstado().name()));
    }

    private String consultarEstadoPedido(ToolExecutionContext context) {
        return pedidoService.obtenerUltimoPedidoCliente(context.local().getTelefono(), context.telefonoCliente())
                .map(pedido -> toJson(Map.of(
                        "pedidoId", pedido.getId(),
                        "estado", pedido.getEstado().name(),
                        "modalidad", pedido.getModalidad().name(),
                        "total", pedido.getTotal(),
                        "horaPedido", pedido.getHoraPedido().toString()
                )))
                .orElseGet(() -> toJson(Map.of("mensaje", "No encontré ningún pedido reciente para este número")));
    }

    private String crearReserva(Map<String, Object> args, ToolExecutionContext context) {
        if (context.commentMode()) {
            return errorJson("No se pueden crear reservas desde un comentario público");
        }

        String horaReservaStr = asString(args, "hora_reserva");
        if (horaReservaStr == null) {
            return errorJson("Falta hora_reserva (formato yyyy-MM-dd'T'HH:mm:ss)");
        }

        Integer numeroPersonas = asInteger(args, "numero_personas");
        if (numeroPersonas == null || numeroPersonas <= 0) {
            return errorJson("Falta numero_personas (debe ser mayor a 0)");
        }

        ReservaDTO dto = ReservaDTO.builder()
                .requestId(context.requestSeed() + ":crear-reserva")
                .cliente(ClienteDTO.builder()
                        .nombre(context.nombreCliente() != null ? context.nombreCliente() : "Cliente WhatsApp")
                        .telefono(context.telefonoCliente())
                        .build())
                .horaReserva(LocalDateTime.parse(horaReservaStr, ISO_DATETIME))
                .numeroPersonas(numeroPersonas)
                .observaciones(asString(args, "observaciones"))
                .build();

        ReservaResponseDTO reserva = reservaService.crearReserva(context.local().getTelefono(), dto);
        return toJson(Map.of(
                "reservaId", reserva.getId(),
                "estado", reserva.getEstado(),
                "numeroPersonas", reserva.getNumeroPersonas()
        ));
    }

    // ==================== Helpers de parseo ====================

    private List<PedidoItemDTO> parseItems(Object rawItems) {
        List<Map<String, Object>> items = JsonNav.asListOfMaps(rawItems);
        List<PedidoItemDTO> result = new ArrayList<>();
        if (items == null) {
            return result;
        }
        for (Map<String, Object> item : items) {
            List<Long> extrasIds = new ArrayList<>();
            if (item.get("extras_ids") instanceof List<?> rawExtras) {
                for (Object extraId : rawExtras) {
                    if (extraId instanceof Number number) {
                        extrasIds.add(number.longValue());
                    }
                }
            }
            result.add(PedidoItemDTO.builder()
                    .productoId(asLong(item, "producto_id"))
                    .cantidad(asInteger(item, "cantidad"))
                    .observaciones(asString(item, "observaciones"))
                    .extrasIds(extrasIds)
                    .build());
        }
        return result;
    }

    private String asString(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value != null ? value.toString() : null;
    }

    private Long asLong(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value instanceof Number number ? number.longValue() : null;
    }

    private Integer asInteger(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value instanceof Number number ? number.intValue() : null;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{\"error\":\"No se pudo serializar la respuesta\"}";
        }
    }

    private String errorJson(String message) {
        return toJson(Map.of("error", message != null ? message : "Error desconocido ejecutando la tool"));
    }

    // ==================== Definiciones de las tools (JSON schema) ====================

    private ToolDefinition definicionConsultarDisponibilidad() {
        return new ToolDefinition(
                "consultar_disponibilidad",
                "Consulta qué hamburguesas están disponibles ahora mismo según el stock de ingredientes.",
                Map.of("type", "object", "properties", Map.of(), "required", List.of())
        );
    }

    private ToolDefinition definicionConsultarHorariosYUbicacion() {
        return new ToolDefinition(
                "consultar_horarios_y_ubicacion",
                "Consulta horarios de atención, dirección del local y modalidades disponibles (delivery/retiro/reservas).",
                Map.of("type", "object", "properties", Map.of(), "required", List.of())
        );
    }

    private ToolDefinition definicionCrearPedido() {
        Map<String, Object> itemSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "producto_id", Map.of("type", "integer", "description", "ID del producto (según el menú)"),
                        "cantidad", Map.of("type", "integer", "description", "Cantidad de unidades"),
                        "observaciones", Map.of("type", "string", "description", "Ej: sin cebolla, bien cocida"),
                        "extras_ids", Map.of("type", "array", "items", Map.of("type", "integer"))
                ),
                "required", List.of("producto_id", "cantidad")
        );

        Map<String, Object> properties = Map.of(
                "modalidad", Map.of("type", "string", "enum", List.of("DELIVERY", "RETIRAR")),
                "direccion_envio", Map.of("type", "string", "description", "Obligatoria si modalidad es DELIVERY"),
                "medio_pago", Map.of("type", "string", "enum",
                        List.of("EFECTIVO", "TRANSFERENCIA", "TARJETA_DEBITO", "TARJETA_CREDITO", "QR")),
                "items", Map.of("type", "array", "items", itemSchema),
                "hora_pedido", Map.of("type", "string", "description", "Hora deseada, formato HH:mm (opcional)")
        );

        return new ToolDefinition(
                "crear_pedido",
                "Crea un nuevo pedido para el cliente que está escribiendo, una vez confirmados todos los datos.",
                Map.of("type", "object", "properties", properties, "required", List.of("modalidad", "medio_pago", "items"))
        );
    }

    private ToolDefinition definicionModificarPedido() {
        Map<String, Object> itemSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "producto_id", Map.of("type", "integer"),
                        "cantidad", Map.of("type", "integer"),
                        "observaciones", Map.of("type", "string")
                ),
                "required", List.of("producto_id", "cantidad")
        );

        Map<String, Object> properties = Map.of(
                "pedido_id", Map.of("type", "integer", "description", "ID del pedido a modificar (debe estar PENDIENTE)"),
                "items", Map.of("type", "array", "items", itemSchema, "description", "Reemplaza todos los items anteriores"),
                "hora_pedido", Map.of("type", "string"),
                "direccion_envio", Map.of("type", "string"),
                "observaciones", Map.of("type", "string")
        );

        return new ToolDefinition(
                "modificar_pedido",
                "Modifica un pedido existente del cliente (solo si está en estado PENDIENTE).",
                Map.of("type", "object", "properties", properties, "required", List.of("pedido_id", "items"))
        );
    }

    private ToolDefinition definicionCancelarPedido() {
        Map<String, Object> properties = Map.of(
                "pedido_id", Map.of("type", "integer"),
                "motivo", Map.of("type", "string")
        );
        return new ToolDefinition(
                "cancelar_pedido",
                "Cancela un pedido existente del cliente (solo PENDIENTE o EN_PREPARACION, según anticipación mínima).",
                Map.of("type", "object", "properties", properties, "required", List.of("pedido_id"))
        );
    }

    private ToolDefinition definicionConsultarEstadoPedido() {
        return new ToolDefinition(
                "consultar_estado_pedido",
                "Consulta el estado del pedido más reciente del cliente que está escribiendo.",
                Map.of("type", "object", "properties", Map.of(), "required", List.of())
        );
    }

    private ToolDefinition definicionCrearReserva() {
        Map<String, Object> properties = Map.of(
                "hora_reserva", Map.of("type", "string", "description", "Fecha y hora ISO, formato yyyy-MM-dd'T'HH:mm:ss"),
                "numero_personas", Map.of("type", "integer"),
                "observaciones", Map.of("type", "string")
        );
        return new ToolDefinition(
                "crear_reserva",
                "Reserva una mesa para el cliente que está escribiendo.",
                Map.of("type", "object", "properties", properties, "required", List.of("hora_reserva", "numero_personas"))
        );
    }
}
