package com.dioburger.service;

import com.dioburger.exception.HorarioNoDisponibleException;
import com.dioburger.exception.NotFoundException;
import com.dioburger.exception.StockInsuficienteException;
import com.dioburger.model.dto.PedidoDTO;
import com.dioburger.model.entity.*;
import com.dioburger.model.enums.*;
import com.dioburger.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar el ciclo completo de pedidos.
 * Implementa la lógica de negocio para crear, validar y procesar pedidos.
 *
 * @author Dio Burger Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final StockService stockService;
    private final WebSocketService webSocketService;
    private final PrinterService printerService;
    private final WebhookDeliveryService webhookDeliveryService;
    private final LocalService localService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Crea un nuevo pedido de forma transaccional e idempotente.
     * Proceso:
     * 1. Verificar idempotencia (requestId)
     * 2. Validar local y cliente
     * 3. Validar horario disponible
     * 4. Validar stock de ingredientes
     * 5. Crear pedido y descontar stock
     * 6. Emitir a cocina (WebSocket)
     * 7. Enviar a impresora (Webhook)
     *
     * @param dto Datos del pedido
     * @param telefonoLocal Teléfono del local (Multi-Tenant)
     * @return Pedido creado
     * @throws NotFoundException si el local no existe
     * @throws HorarioNoDisponibleException si el horario no está disponible
     * @throws StockInsuficienteException si no hay stock suficiente
     */
    @Transactional
    public Pedido crearPedido(PedidoDTO dto, String telefonoLocal) {
        return crearPedido(dto, telefonoLocal, OrigenPedido.BOT);
    }

    /**
     * Crea un pedido indicando su origen (BOT o LOCAL/panel).
     *
     * @param dto           Datos del pedido
     * @param telefonoLocal Teléfono del local (Multi-Tenant)
     * @param origen        Origen del pedido
     * @return Pedido creado
     */
    @Transactional
    public Pedido crearPedido(PedidoDTO dto, String telefonoLocal, OrigenPedido origen) {
        log.info("🍔 Creando pedido con requestId: {} para local: {} (origen: {})",
                dto.getRequestId(), telefonoLocal, origen);

        // 1. Verificar idempotencia
        Pedido pedidoExistente = pedidoRepository.findByRequestId(dto.getRequestId())
                .orElse(null);

        if (pedidoExistente != null) {
            log.info("⚠️ Pedido con requestId {} ya existe (idempotencia). Devolviendo pedido existente",
                    dto.getRequestId());
            return pedidoExistente;
        }

        // 2. Buscar local
        Local local = localService.buscarPorTelefono(telefonoLocal);

        // 3. Validar modalidad permitida
        validarModalidad(dto.getModalidad(), local.getConfiguracion());

        // 4. Obtener o crear cliente
        Cliente cliente = obtenerOCrearCliente(dto.getCliente());

        // 5. Validar horario si se especificó
        if (dto.getHoraPedido() != null && !dto.getHoraPedido().isBlank()) {
            validarHorario(dto.getHoraPedido(), local);
        }

        // 6. Crear items del pedido y calcular total
        List<PedidoItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var itemDTO : dto.getItems()) {
            Producto producto = productoRepository.findById(itemDTO.getProductoId())
                    .orElseThrow(() -> new NotFoundException(
                            "Producto no encontrado con ID: " + itemDTO.getProductoId()));

            // Verificar que el producto pertenezca al local
            if (!producto.getLocal().getId().equals(local.getId())) {
                throw new IllegalArgumentException(
                        "El producto " + producto.getNombre() + " no pertenece al local");
            }

            // Verificar disponibilidad de stock
            if (!stockService.verificarDisponibilidad(producto)) {
                throw new StockInsuficienteException(
                        "El producto " + producto.getNombre() + " no tiene stock suficiente");
            }

            PedidoItem item = PedidoItem.builder()
                    .producto(producto)
                    .cantidad(itemDTO.getCantidad())
                    .observaciones(itemDTO.getObservaciones())
                    .build();

            items.add(item);

            // Calcular subtotal
            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(itemDTO.getCantidad()));
            total = total.add(subtotal);
        }

        // 7. Crear el pedido
        Pedido pedido = Pedido.builder()
                .local(local)
                .cliente(cliente)
                .modalidad(Modalidad.valueOf(dto.getModalidad().toUpperCase()))
                .estado(EstadoPedido.PENDIENTE)
                .medioPago(MedioPago.valueOf(dto.getMedioPago().toUpperCase()))
                .estadoPago(EstadoPago.PENDIENTE)
                .origenPedido(origen)
                .total(total)
                .direccionEnvio(dto.getDireccionEnvio())
                .horaPedido(LocalDateTime.now())
                .requestId(dto.getRequestId())
                .build();

        // Asociar items al pedido
        for (PedidoItem item : items) {
            item.setPedido(pedido);
        }
        pedido.setItems(items);

        // 8. Guardar en base de datos
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        log.info("✅ Pedido {} creado exitosamente. Total: ${}", 
                pedidoGuardado.getId(), pedidoGuardado.getTotal());

        // 9. Descontar stock de ingredientes
        try {
            stockService.descontarStock(items);
            log.info("✅ Stock descontado para pedido {}", pedidoGuardado.getId());
        } catch (Exception e) {
            log.error("❌ Error al descontar stock para pedido {}: {}",
                    pedidoGuardado.getId(), e.getMessage());
            throw e; // Rollback de la transacción
        }

        // 10. Actualizar disponibilidad de productos
        stockService.actualizarDisponibilidadProductos(local);

        // 11. Emitir a cocina vía WebSocket (no bloquea)
        try {
            webSocketService.emitirPedido(pedidoGuardado);
        } catch (Exception e) {
            log.error("❌ Error al emitir pedido vía WebSocket: {}", e.getMessage(), e);
            // No hacer rollback, el pedido está guardado
        }

        // 12. Enviar a impresora vía webhook (asíncrono)
        try {
            printerService.enviarTicket(pedidoGuardado);
        } catch (Exception e) {
            log.error("❌ Error al enviar ticket a impresora: {}", e.getMessage(), e);
            // No hacer rollback, el pedido está guardado
        }

        // 13. Si es DELIVERY, solicitar asignación de repartidor a n8n
        if (pedidoGuardado.getModalidad() == Modalidad.DELIVERY) {
            try {
                webhookDeliveryService.solicitarAsignacionRepartidor(pedidoGuardado);
                log.info("🚚 Solicitud de repartidor enviada para pedido DELIVERY {}", 
                        pedidoGuardado.getId());
            } catch (Exception e) {
                log.error("❌ Error al solicitar repartidor: {}", e.getMessage(), e);
                // No hacer rollback, el repartidor se puede asignar manualmente
            }
        }

        return pedidoGuardado;
    }

    /**
     * Valida que la modalidad solicitada esté permitida por el local.
     *
     * @param modalidad Modalidad solicitada
     * @param config Configuración del local
     * @throws IllegalArgumentException si la modalidad no está permitida
     */
    private void validarModalidad(String modalidad, ConfiguracionLocal config) {
        if (modalidad.equalsIgnoreCase("DELIVERY") && !config.getPermiteDelivery()) {
            throw new IllegalArgumentException(
                    "El local no acepta pedidos con modalidad DELIVERY");
        }

        if (modalidad.equalsIgnoreCase("RETIRAR") && !config.getPermiteTakeAway()) {
            throw new IllegalArgumentException(
                    "El local no acepta pedidos con modalidad RETIRAR");
        }
    }

    /**
     * Valida que el horario solicitado esté disponible.
     *
     * @param horaPedido Hora solicitada (formato "HH:mm")
     * @param local Local donde se hace el pedido
     * @throws HorarioNoDisponibleException si el horario no está disponible
     */
    private void validarHorario(String horaPedido, Local local) {
        ConfiguracionLocal config = local.getConfiguracion();

        // Parsear hora
        LocalTime hora = LocalTime.parse(horaPedido, TIME_FORMATTER);

        // Verificar que esté dentro del horario de atención
        if (hora.isBefore(config.getHoraApertura()) || hora.isAfter(config.getHoraCierre())) {
            throw new HorarioNoDisponibleException(
                    String.format("El horario %s está fuera del horario de atención (%s - %s)",
                            horaPedido,
                            config.getHoraApertura().format(TIME_FORMATTER),
                            config.getHoraCierre().format(TIME_FORMATTER)));
        }

        // Verificar cuántos pedidos hay en ese slot
        LocalDateTime inicioSlot = LocalDateTime.now().with(hora);
        LocalDateTime finSlot = inicioSlot.plusMinutes(config.getIntervaloMinutosPedidos());

        Long pedidosEnSlot = pedidoRepository.countPedidosEnIntervalo(
                local, inicioSlot, finSlot);

        if (pedidosEnSlot >= config.getMaxPedidosPorIntervalo()) {
            throw new HorarioNoDisponibleException(
                    String.format("El horario %s ya está completo (%d/%d pedidos)",
                            horaPedido, pedidosEnSlot, config.getMaxPedidosPorIntervalo()));
        }
    }

    /**
     * Modifica un pedido existente.
     * Proceso:
     * 1. Validar que el pedido exista y esté en estado modificable (PENDIENTE)
     * 2. Restaurar stock de los items anteriores
     * 3. Validar stock de los nuevos items
     * 4. Descontar stock de los nuevos items
     * 5. Actualizar el pedido
     * 6. Notificar via WebSocket
     * 
     * @param pedidoId ID del pedido a modificar
     * @param dto Datos de modificación
     * @param telefonoLocal Teléfono del local (Multi-Tenant)
     * @return Pedido modificado
     * @throws NotFoundException si el pedido no existe
     * @throws IllegalStateException si el pedido no está en estado modificable
     */
    @Transactional
    public Pedido modificarPedido(Long pedidoId, com.dioburger.model.dto.ModificarPedidoDTO dto, String telefonoLocal) {
        log.info("✏️ Modificando pedido {} con requestId: {}", pedidoId, dto.getRequestId());

        // 1. Buscar el pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado con ID: " + pedidoId));

        // 2. Validar que pertenezca al local correcto
        if (!pedido.getLocal().getTelefono().equals(telefonoLocal)) {
            throw new IllegalArgumentException("El pedido no pertenece a este local");
        }

        // 3. Validar estado modificable (solo PENDIENTE puede modificarse)
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden modificar pedidos en estado PENDIENTE. Estado actual: " + pedido.getEstado());
        }

        // 4. Restaurar stock de items anteriores
        log.info("♻️ Restaurando stock de items anteriores");
        for (PedidoItem item : pedido.getItems()) {
            stockService.restaurarStock(item.getProducto(), item.getCantidad());
        }

        // 5. Validar y crear nuevos items
        List<PedidoItem> nuevosItems = new ArrayList<>();
        BigDecimal nuevoTotal = BigDecimal.ZERO;

        for (var itemDTO : dto.getItems()) {
            Producto producto = productoRepository.findById(itemDTO.getProductoId())
                    .orElseThrow(() -> new NotFoundException(
                            "Producto no encontrado con ID: " + itemDTO.getProductoId()));

            // Verificar stock
            if (!stockService.verificarStock(producto, itemDTO.getCantidad())) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto: " + producto.getNombre());
            }

            PedidoItem nuevoItem = PedidoItem.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(itemDTO.getCantidad())
                    .observaciones(itemDTO.getObservaciones())
                    .build();

            nuevosItems.add(nuevoItem);
            nuevoTotal = nuevoTotal.add(producto.getPrecio().multiply(BigDecimal.valueOf(itemDTO.getCantidad())));
        }

        // 6. Descontar stock de nuevos items
        log.info("📦 Descontando stock de nuevos items");
        for (PedidoItem item : nuevosItems) {
            stockService.descontarStock(item.getProducto(), item.getCantidad());
        }

        // 7. Actualizar pedido
        pedido.getItems().clear();
        pedido.getItems().addAll(nuevosItems);
        pedido.setTotal(nuevoTotal);

        // Actualizar otros campos si se proporcionaron
        if (dto.getHoraPedido() != null && !dto.getHoraPedido().isBlank()) {
            validarHorario(dto.getHoraPedido(), pedido.getLocal());
            pedido.setHoraPedido(LocalDateTime.of(
                    LocalDateTime.now().toLocalDate(),
                    LocalTime.parse(dto.getHoraPedido(), TIME_FORMATTER)
            ));
        }

        if (dto.getDireccionEnvio() != null) {
            pedido.setDireccionEnvio(dto.getDireccionEnvio());
        }

        // Nota: Las observaciones se guardan en cada PedidoItem

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // 8. Notificar via WebSocket
        webSocketService.notificarPedidoModificado(pedidoGuardado);

        log.info("✅ Pedido {} modificado exitosamente. Nuevo total: ${}", 
                pedidoId, nuevoTotal);

        return pedidoGuardado;
    }

    /**
     * Cancela un pedido existente.
     * Proceso:
     * 1. Validar que el pedido exista
     * 2. Validar que esté en estado cancelable (PENDIENTE o EN_PREPARACION)
     * 3. Validar tiempo de anticipación mínimo
     * 4. Restaurar stock de los items
     * 5. Cambiar estado a CANCELADO
     * 6. Notificar via WebSocket
     * 
     * @param pedidoId ID del pedido a cancelar
     * @param dto Datos de cancelación
     * @param telefonoLocal Teléfono del local (Multi-Tenant)
     * @return Pedido cancelado
     * @throws NotFoundException si el pedido no existe
     * @throws IllegalStateException si el pedido no está en estado cancelable
     * @throws com.dioburger.exception.CancelacionNoPermitidaException si no hay anticipación suficiente
     */
    @Transactional
    public Pedido cancelarPedido(Long pedidoId, com.dioburger.model.dto.CancelarPedidoDTO dto, String telefonoLocal) {
        log.info("❌ Cancelando pedido {} con requestId: {}", pedidoId, dto.getRequestId());

        // 1. Buscar el pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado con ID: " + pedidoId));

        // 2. Validar que pertenezca al local correcto
        if (!pedido.getLocal().getTelefono().equals(telefonoLocal)) {
            throw new IllegalArgumentException("El pedido no pertenece a este local");
        }

        // 3. Validar estado cancelable
        if (pedido.getEstado() != EstadoPedido.PENDIENTE && 
            pedido.getEstado() != EstadoPedido.EN_PREPARACION) {
            throw new IllegalStateException(
                    "Solo se pueden cancelar pedidos en estado PENDIENTE o EN_PREPARACION. Estado actual: " 
                    + pedido.getEstado());
        }

        // 4. Validar tiempo de anticipacion.
        // Solo aplica si el local configuro una anticipacion minima positiva: 0 (o
        // null) significa "sin restriccion". Sin este corte la regla es imposible de
        // cumplir para los pedidos inmediatos, que son la mayoria: crearPedido guarda
        // horaPedido = ahora, asi que el tiempo restante es negativo desde el primer
        // minuto y ni siquiera con la config en 0 se podia cancelar.
        ConfiguracionLocal config = pedido.getLocal().getConfiguracion();
        Integer anticipacionMinima = config != null ? config.getMinutosAnticipacionCancelacion() : null;

        if (anticipacionMinima != null && anticipacionMinima > 0) {
            LocalDateTime ahora = LocalDateTime.now();
            long minutosAnticipacion = java.time.Duration.between(ahora, pedido.getHoraPedido()).toMinutes();

            if (minutosAnticipacion < anticipacionMinima) {
                throw new com.dioburger.exception.CancelacionNoPermitidaException(
                        String.format("No se puede cancelar el pedido. Se requiere mínimo %d minutos de anticipación. " +
                                "Tiempo restante: %d minutos",
                                anticipacionMinima,
                                minutosAnticipacion));
            }
        }

        // 5. Restaurar stock
        log.info("♻️ Restaurando stock de todos los items del pedido");
        for (PedidoItem item : pedido.getItems()) {
            stockService.restaurarStock(item.getProducto(), item.getCantidad());
        }

        // 6. Cambiar estado a CANCELADO
        pedido.setEstado(EstadoPedido.CANCELADO);
        
        // Nota: El motivo de cancelación se podría guardar en un campo de auditoría
        // Por ahora solo se registra en los logs

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // 7. Notificar via WebSocket
        webSocketService.notificarPedidoCancelado(pedidoGuardado);

        log.info("✅ Pedido {} cancelado exitosamente. Motivo: {}", 
                pedidoId, dto.getMotivo() != null ? dto.getMotivo() : "No especificado");

        return pedidoGuardado;
    }

    /**
     * Obtiene el pedido más reciente de un cliente en un local.
     * Usado por el bot para responder "¿en qué estado está mi pedido?".
     *
     * @param telefonoLocal Teléfono del local (Multi-Tenant)
     * @param telefonoCliente Teléfono del cliente
     * @return el pedido más reciente del cliente, si existe alguno
     */
    public Optional<Pedido> obtenerUltimoPedidoCliente(String telefonoLocal, String telefonoCliente) {
        Local local = localService.buscarPorTelefono(telefonoLocal);
        return pedidoRepository.findByLocalAndCliente_TelefonoOrderByHoraPedidoDesc(local, telefonoCliente)
                .stream()
                .findFirst();
    }

    /**
     * Obtiene un cliente existente o crea uno nuevo.
     *
     * @param clienteDTO Datos del cliente
     * @return Cliente existente o recién creado
     */
    private Cliente obtenerOCrearCliente(com.dioburger.model.dto.ClienteDTO clienteDTO) {
        return clienteRepository.findByTelefono(clienteDTO.getTelefono())
                .orElseGet(() -> {
                    Cliente nuevoCliente = Cliente.builder()
                            .nombre(clienteDTO.getNombre())
                            .telefono(clienteDTO.getTelefono())
                            .build();
                    Cliente guardado = clienteRepository.save(nuevoCliente);
                    log.info("✅ Nuevo cliente creado: {} ({})",
                            guardado.getNombre(), guardado.getTelefono());
                    return guardado;
                });
    }
}

