package com.dioburger.service;

import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Pedido;
import com.dioburger.model.enums.EstadoPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para WebSocketService.
 * 
 * Cobertura:
 * - Emisión de pedidos nuevos
 * - Actualización de estado de pedidos
 * - Notificación de modificaciones
 * - Notificación de cancelaciones
 * - Notificación de asignación de repartidor
 * - Manejo de errores (resiliencia)
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketService - Tests Unitarios")
class WebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketService webSocketService;

    private Local local;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        // Configurar local
        local = new Local();
        local.setId(1L);
        local.setNombre("Dio Burger Central");
        local.setTelefono("549349366512");

        // Configurar pedido
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setLocal(local);
        pedido.setEstado(EstadoPedido.PENDIENTE);
    }

    // ==================== EMITIR PEDIDO ====================

    @Test
    @DisplayName("emitirPedido - Emite correctamente al topic del local")
    void emitirPedido_pedidoValido_emiteCorrectamente() {
        // Arrange
        String expectedTopic = "/topic/pedidos/549349366512";
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Act
        webSocketService.emitirPedido(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedTopic), eq(pedido));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("emitirPedido - Maneja excepción sin propagar (resiliencia)")
    void emitirPedido_errorEnEmision_noLanzaException() {
        // Arrange
        doThrow(new RuntimeException("WebSocket connection error"))
                .when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Act - No debe lanzar excepción
        webSocketService.emitirPedido(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(anyString(), eq(pedido));
        // El servicio debe continuar funcionando a pesar del error
    }

    @Test
    @DisplayName("emitirPedido - Múltiples pedidos con diferentes locales")
    void emitirPedido_diferentesLocales_emiteATopicsCorrectos() {
        // Arrange
        Local local2 = new Local();
        local2.setTelefono("549999999999");

        Pedido pedido2 = new Pedido();
        pedido2.setId(2L);
        pedido2.setLocal(local2);

        // Act
        webSocketService.emitirPedido(pedido);
        webSocketService.emitirPedido(pedido2);

        // Assert
        verify(messagingTemplate).convertAndSend(eq("/topic/pedidos/549349366512"), eq(pedido));
        verify(messagingTemplate).convertAndSend(eq("/topic/pedidos/549999999999"), eq(pedido2));
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Object.class));
    }

    // ==================== EMITIR ACTUALIZACIÓN ====================

    @Test
    @DisplayName("emitirActualizacionPedido - Emite al topic de actualizaciones")
    void emitirActualizacionPedido_pedidoValido_emiteCorrectamente() {
        // Arrange
        pedido.setEstado(EstadoPedido.EN_PREPARACION);
        String expectedTopic = "/topic/pedidos/549349366512/actualizaciones";

        // Act
        webSocketService.emitirActualizacionPedido(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedTopic), eq(pedido));
    }

    @Test
    @DisplayName("emitirActualizacionPedido - Maneja excepción sin propagar")
    void emitirActualizacionPedido_errorEnEmision_noLanzaException() {
        // Arrange
        doThrow(new RuntimeException("Network error"))
                .when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Act
        webSocketService.emitirActualizacionPedido(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(anyString(), eq(pedido));
    }

    // ==================== NOTIFICAR PEDIDO MODIFICADO ====================

    @Test
    @DisplayName("notificarPedidoModificado - Emite al topic de modificados")
    void notificarPedidoModificado_pedidoValido_emiteCorrectamente() {
        // Arrange
        String expectedTopic = "/topic/pedidos/549349366512/modificados";

        // Act
        webSocketService.notificarPedidoModificado(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedTopic), eq(pedido));
    }

    @Test
    @DisplayName("notificarPedidoModificado - Maneja excepción sin propagar")
    void notificarPedidoModificado_errorEnEmision_noLanzaException() {
        // Arrange
        doThrow(new RuntimeException("Connection timeout"))
                .when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Act
        webSocketService.notificarPedidoModificado(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(anyString(), eq(pedido));
    }

    // ==================== NOTIFICAR PEDIDO CANCELADO ====================

    @Test
    @DisplayName("notificarPedidoCancelado - Emite al topic de cancelados")
    void notificarPedidoCancelado_pedidoValido_emiteCorrectamente() {
        // Arrange
        pedido.setEstado(EstadoPedido.CANCELADO);
        String expectedTopic = "/topic/pedidos/549349366512/cancelados";

        // Act
        webSocketService.notificarPedidoCancelado(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedTopic), eq(pedido));
    }

    @Test
    @DisplayName("notificarPedidoCancelado - Maneja excepción sin propagar")
    void notificarPedidoCancelado_errorEnEmision_noLanzaException() {
        // Arrange
        doThrow(new RuntimeException("Socket closed"))
                .when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Act
        webSocketService.notificarPedidoCancelado(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(anyString(), eq(pedido));
    }

    // ==================== NOTIFICAR REPARTIDOR ASIGNADO ====================

    @Test
    @DisplayName("notificarRepartidorAsignado - Emite al topic de repartidor-asignado")
    void notificarRepartidorAsignado_pedidoValido_emiteCorrectamente() {
        // Arrange
        String expectedTopic = "/topic/pedidos/549349366512/repartidor-asignado";

        // Act
        webSocketService.notificarRepartidorAsignado(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedTopic), eq(pedido));
    }

    @Test
    @DisplayName("notificarRepartidorAsignado - Maneja excepción sin propagar")
    void notificarRepartidorAsignado_errorEnEmision_noLanzaException() {
        // Arrange
        doThrow(new RuntimeException("Message too large"))
                .when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Act
        webSocketService.notificarRepartidorAsignado(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(anyString(), eq(pedido));
    }

    // ==================== TESTS DE INTEGRACIÓN DE TOPICS ====================

    @Test
    @DisplayName("Topics - Diferentes eventos usan diferentes canales")
    void topics_diferentesEventos_usanDiferentesCanales() {
        // Arrange & Act
        webSocketService.emitirPedido(pedido);
        webSocketService.emitirActualizacionPedido(pedido);
        webSocketService.notificarPedidoModificado(pedido);
        webSocketService.notificarPedidoCancelado(pedido);
        webSocketService.notificarRepartidorAsignado(pedido);

        // Assert - 5 emisiones a 5 topics diferentes
        verify(messagingTemplate).convertAndSend(eq("/topic/pedidos/549349366512"), any(Object.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/pedidos/549349366512/actualizaciones"), any(Object.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/pedidos/549349366512/modificados"), any(Object.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/pedidos/549349366512/cancelados"), any(Object.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/pedidos/549349366512/repartidor-asignado"), any(Object.class));
        verify(messagingTemplate, times(5)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Resiliencia - Múltiples fallos no afectan ejecuciones posteriores")
    void resiliencia_multplesFallos_servicioSigueFuncionando() {
        // Arrange - Primer pedido falla
        doThrow(new RuntimeException("Error 1"))
                .doNothing() // Segundo pedido exitoso
                .doThrow(new RuntimeException("Error 2"))
                .doNothing() // Cuarto pedido exitoso
                .when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Act - 4 intentos de emisión
        webSocketService.emitirPedido(pedido);
        webSocketService.emitirPedido(pedido);
        webSocketService.emitirPedido(pedido);
        webSocketService.emitirPedido(pedido);

        // Assert - Los 4 intentos se ejecutaron (no se propagó excepción)
        verify(messagingTemplate, times(4)).convertAndSend(anyString(), eq(pedido));
    }

    @Test
    @DisplayName("Resiliencia - Excepción null pointer no afecta servicio")
    void resiliencia_nullPointerException_servicioSigueFuncionando() {
        // Arrange
        doThrow(new NullPointerException("Template configuration error"))
                .when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Act
        webSocketService.emitirPedido(pedido);
        webSocketService.emitirActualizacionPedido(pedido);

        // Assert - Ambos intentos se ejecutaron sin propagar excepción
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Object.class));
    }

    // ==================== TESTS DE FORMATO DE TOPIC ====================

    @Test
    @DisplayName("Topic Format - Base topic incluye teléfono del local")
    void topicFormat_baseTopic_incluyeTelefonoLocal() {
        // Arrange
        String expectedTopic = "/topic/pedidos/549349366512";

        // Act
        webSocketService.emitirPedido(pedido);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedTopic), any(Object.class));
    }

    @Test
    @DisplayName("Topic Format - Todos los subtopics mantienen estructura consistente")
    void topicFormat_todosSubtopics_estructuraConsistente() {
        // Act
        webSocketService.emitirActualizacionPedido(pedido);
        webSocketService.notificarPedidoModificado(pedido);
        webSocketService.notificarPedidoCancelado(pedido);
        webSocketService.notificarRepartidorAsignado(pedido);

        // Assert - Todos siguen el patrón /topic/pedidos/{telefono}/{evento}
        verify(messagingTemplate).convertAndSend(
                eq("/topic/pedidos/549349366512/actualizaciones"), any(Object.class));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/pedidos/549349366512/modificados"), any(Object.class));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/pedidos/549349366512/cancelados"), any(Object.class));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/pedidos/549349366512/repartidor-asignado"), any(Object.class));
    }
}
