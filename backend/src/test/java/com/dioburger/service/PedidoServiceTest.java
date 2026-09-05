package com.dioburger.service;

import com.dioburger.exception.NotFoundException;
import com.dioburger.exception.StockInsuficienteException;
import com.dioburger.model.dto.ClienteDTO;
import com.dioburger.model.dto.PedidoDTO;
import com.dioburger.model.dto.PedidoItemDTO;
import com.dioburger.model.entity.*;
import com.dioburger.model.enums.*;
import com.dioburger.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PedidoService.
 * Valida la lógica crítica de creación de pedidos:
 * - Idempotencia (requestId)
 * - Validaciones de local, cliente
 * - Validación de stock
 * - Cálculo de totales
 * - Integración con servicios externos
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService - Tests Unitarios")
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private StockService stockService;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private PrinterService printerService;

    @Mock
    private WebhookDeliveryService webhookDeliveryService;

    @Mock
    private LocalService localService;

    @InjectMocks
    private PedidoService pedidoService;

    // Datos de ejemplo
    private Local local;
    private ConfiguracionLocal configuracion;
    private Cliente cliente;
    private Producto producto;
    private PedidoDTO pedidoDTO;

    @BeforeEach
    void setUp() {
        // Configuración del local
        configuracion = ConfiguracionLocal.builder()
                .id(1L)
                .permiteDelivery(true)
                .permiteTakeAway(true)
                .horaApertura(LocalTime.of(9, 0))
                .horaCierre(LocalTime.of(23, 0))
                .build();

        local = Local.builder()
                .id(1L)
                .nombre("Local Test")
                .telefono("549349366512")
                .configuracion(configuracion)
                .planSuscripcion(PlanSuscripcion.PREMIUM)
                .build();

        // Cliente de ejemplo
        cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .telefono("549123456789")
                .build();

        // Producto de ejemplo
        producto = Producto.builder()
                .id(1L)
                .nombre("Hamburguesa Clásica")
                .precio(BigDecimal.valueOf(5000))
                .local(local)
                .build();

        // DTO de pedido
        ClienteDTO clienteDTO = ClienteDTO.builder()
                .nombre("Juan Pérez")
                .telefono("549123456789")
                .build();

        PedidoItemDTO itemDTO = PedidoItemDTO.builder()
                .productoId(1L)
                .cantidad(2)
                .observaciones("Sin cebolla")
                .build();

        pedidoDTO = PedidoDTO.builder()
                .requestId("REQ-12345")
                .cliente(clienteDTO)
                .modalidad("DELIVERY")
                .medioPago("EFECTIVO")
                .direccionEnvio("Av. Siempre Viva 123")
                .items(List.of(itemDTO))
                .build();
    }

    // ===============================
    // Tests Happy Path
    // ===============================

    @Test
    @DisplayName("Crear pedido exitosamente - Debe retornar pedido guardado")
    void crearPedido_happyPath_retornaPedidoGuardado() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(any(Producto.class))).thenReturn(true);
        
        Pedido pedidoEsperado = new Pedido();
        pedidoEsperado.setId(1L);
        pedidoEsperado.setTotal(BigDecimal.valueOf(10000)); // 2 x 5000
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoEsperado);

        // When
        Pedido resultado = pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(stockService, times(1)).descontarStock(anyList());
        verify(webSocketService, times(1)).emitirPedido(any(Pedido.class));
    }

    @Test
    @DisplayName("Crear pedido DELIVERY - Debe solicitar repartidor")
    void crearPedido_deliveryMode_solicitaRepartidor() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(any(Producto.class))).thenReturn(true);

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(1L);
        pedidoGuardado.setModalidad(Modalidad.DELIVERY);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        verify(webhookDeliveryService, times(1)).solicitarAsignacionRepartidor(any(Pedido.class));
    }

    @Test
    @DisplayName("Crear pedido RETIRAR - No debe solicitar repartidor")
    void crearPedido_retirarMode_noSolicitaRepartidor() {
        // Given
        pedidoDTO.setModalidad("RETIRAR");
        
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(any(Producto.class))).thenReturn(true);

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(1L);
        pedidoGuardado.setModalidad(Modalidad.RETIRAR);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        verify(webhookDeliveryService, never()).solicitarAsignacionRepartidor(any(Pedido.class));
    }

    // ===============================
    // Tests Idempotencia (requestId)
    // ===============================

    @Test
    @DisplayName("Pedido con requestId duplicado - Debe retornar pedido existente (idempotencia)")
    void crearPedido_requestIdDuplicado_retornaPedidoExistente() {
        // Given
        Pedido pedidoExistente = new Pedido();
        pedidoExistente.setId(1L);
        pedidoExistente.setRequestId("REQ-12345");
        when(pedidoRepository.findByRequestId("REQ-12345")).thenReturn(Optional.of(pedidoExistente));

        // When
        Pedido resultado = pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("REQ-12345", resultado.getRequestId());
        
        // No debe crear nuevo pedido
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(localService, never()).buscarPorTelefono(anyString());
    }

    @Test
    @DisplayName("Pedido con requestId único - Debe crear nuevo pedido")
    void crearPedido_requestIdUnico_creaNuevoPedido() {
        // Given
        when(pedidoRepository.findByRequestId("REQ-12345")).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(any(Producto.class))).thenReturn(true);

        Pedido pedidoNuevo = new Pedido();
        pedidoNuevo.setId(2L);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoNuevo);

        // When
        Pedido resultado = pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    // ===============================
    // Tests Validación de Local
    // ===============================

    @Test
    @DisplayName("Local no encontrado - Debe lanzar NotFoundException")
    void crearPedido_localNoEncontrado_lanzaNotFoundException() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono("549999999999"))
                .thenThrow(new NotFoundException("Local no encontrado"));

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            pedidoService.crearPedido(pedidoDTO, "549999999999");
        });

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // ===============================
    // Tests Validación de Producto
    // ===============================

    @Test
    @DisplayName("Producto no encontrado - Debe lanzar NotFoundException")
    void crearPedido_productoNoEncontrado_lanzaNotFoundException() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            pedidoService.crearPedido(pedidoDTO, "549349366512");
        });

        assertTrue(exception.getMessage().contains("Producto no encontrado"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Producto de otro local - Debe lanzar IllegalArgumentException")
    void crearPedido_productoDeOtroLocal_lanzaIllegalArgumentException() {
        // Given
        Local otroLocal = Local.builder()
                .id(2L)
                .nombre("Otro Local")
                .telefono("549111111111")
                .build();

        Producto productoOtroLocal = Producto.builder()
                .id(1L)
                .nombre("Hamburguesa")
                .local(otroLocal)
                .build();

        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoOtroLocal));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.crearPedido(pedidoDTO, "549349366512");
        });

        assertTrue(exception.getMessage().contains("no pertenece al local"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // ===============================
    // Tests Validación de Stock
    // ===============================

    @Test
    @DisplayName("Stock insuficiente - Debe lanzar StockInsuficienteException")
    void crearPedido_stockInsuficiente_lanzaStockInsuficienteException() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(producto)).thenReturn(false);

        // When & Then
        StockInsuficienteException exception = assertThrows(StockInsuficienteException.class, () -> {
            pedidoService.crearPedido(pedidoDTO, "549349366512");
        });

        assertTrue(exception.getMessage().contains("no tiene stock suficiente"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Stock disponible - Debe crear pedido y descontar stock")
    void crearPedido_stockDisponible_creaPedidoYDescuentaStock() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(producto)).thenReturn(true);

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(1L);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        verify(stockService, times(1)).descontarStock(anyList());
        verify(stockService, times(1)).actualizarDisponibilidadProductos(local);
    }

    // ===============================
    // Tests Cálculo de Total
    // ===============================

    @Test
    @DisplayName("Calcular total correctamente - 2 items x $5000 = $10000")
    void crearPedido_calculaTotalCorrectamente() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(any(Producto.class))).thenReturn(true);

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            // Verificar que el total sea correcto: 2 x 5000 = 10000
            assertEquals(0, BigDecimal.valueOf(10000).compareTo(pedido.getTotal()),
                    "El total debe ser 10000 (2 x 5000)");
            return pedido;
        });

        // When
        pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    // ===============================
    // Tests Integración con Servicios
    // ===============================

    @Test
    @DisplayName("Enviar pedido a cocina vía WebSocket - Debe invocar webSocketService")
    void crearPedido_enviaPedidoACocina() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(any(Producto.class))).thenReturn(true);

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(1L);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        verify(webSocketService, times(1)).emitirPedido(pedidoGuardado);
    }

    @Test
    @DisplayName("Enviar ticket a impresora - Debe invocar printerService")
    void crearPedido_enviaTicketAImpresora() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(any(Producto.class))).thenReturn(true);

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(1L);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        verify(printerService, times(1)).enviarTicket(pedidoGuardado);
    }

    @Test
    @DisplayName("Error en WebSocket no debe afectar creación de pedido")
    void crearPedido_errorWebSocket_pedidoSeCreaIgual() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(any(Producto.class))).thenReturn(true);

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(1L);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // Simular error en WebSocket
        doThrow(new RuntimeException("WebSocket error")).when(webSocketService).emitirPedido(any());

        // When
        Pedido resultado = pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    // ===============================
    // Tests Cliente
    // ===============================

    @Test
    @DisplayName("Cliente existente - Debe usar cliente de BD")
    void crearPedido_clienteExistente_usaClienteBD() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono("549123456789")).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(any(Producto.class))).thenReturn(true);

        Pedido pedidoGuardado = new Pedido();
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        verify(clienteRepository, times(1)).findByTelefono("549123456789");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Cliente nuevo - Debe crear cliente en BD")
    void crearPedido_clienteNuevo_creaClienteBD() {
        // Given
        when(pedidoRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(clienteRepository.findByTelefono("549123456789")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
        when(stockService.verificarDisponibilidad(any(Producto.class))).thenReturn(true);

        Pedido pedidoGuardado = new Pedido();
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        pedidoService.crearPedido(pedidoDTO, "549349366512");

        // Then
        verify(clienteRepository, times(1)).findByTelefono("549123456789");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }
}
