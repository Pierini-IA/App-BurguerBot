package com.dioburger.controller;

import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Pedido;
import com.dioburger.model.enums.EstadoPedido;
import com.dioburger.repository.PedidoRepository;
import com.dioburger.service.LocalService;
import com.dioburger.service.WebSocketService;
import com.dioburger.service.WebhookNotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para CocinaController.
 * Valida los endpoints de gestión de pedidos desde cocina.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests de CocinaController")
class CocinaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoRepository pedidoRepository;

    @MockBean
    private LocalService localService;

    @MockBean
    private WebSocketService webSocketService;

    @MockBean
    private WebhookNotificacionService webhookNotificacionService;

    private Local local;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        // Local de prueba
        local = Local.builder()
                .id(1L)
                .nombre("Dio Burger Centro")
                .telefono("+5491187654321")
                .direccion("Av. Principal 123")
                .build();

        // Pedido de prueba
        pedido = Pedido.builder()
                .id(1L)
                .estado(EstadoPedido.PENDIENTE)
                .local(local)
                .build();
    }

    @Test
    @DisplayName("GET /api/cocina/pedidos - Listar pedidos activos exitoso")
    @WithMockUser(roles = "COCINA")
    void testListarPedidosActivosExitoso() throws Exception {
        // Arrange
        List<Pedido> pedidosActivos = Arrays.asList(pedido);
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(pedidoRepository.findByLocalAndEstadoIn(any(Local.class), anyList()))
                .thenReturn(pedidosActivos);

        // Act & Assert
        mockMvc.perform(get("/api/cocina/pedidos")
                        .param("telefonoLocal", "+5491187654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));

        verify(localService, times(1)).buscarPorTelefono("+5491187654321");
        verify(pedidoRepository, times(1)).findByLocalAndEstadoIn(any(Local.class), anyList());
    }

    @Test
    @DisplayName("GET /api/cocina/pedidos - Sin autenticación retorna 403")
    void testListarPedidosSinAuth() throws Exception {
        mockMvc.perform(get("/api/cocina/pedidos")
                        .param("telefonoLocal", "+5491187654321"))
                .andExpect(status().isForbidden());

        verify(localService, never()).buscarPorTelefono(anyString());
        verify(pedidoRepository, never()).findByLocalAndEstadoIn(any(Local.class), anyList());
    }

    @Test
    @DisplayName("GET /api/cocina/pedidos - Con rol ADMIN también funciona")
    @WithMockUser(roles = "ADMIN")
    void testListarPedidosConRolAdmin() throws Exception {
        // Arrange
        List<Pedido> pedidosActivos = Arrays.asList(pedido);
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(pedidoRepository.findByLocalAndEstadoIn(any(Local.class), anyList()))
                .thenReturn(pedidosActivos);

        // Act & Assert
        mockMvc.perform(get("/api/cocina/pedidos")
                        .param("telefonoLocal", "+5491187654321"))
                .andExpect(status().isOk());

        verify(localService, times(1)).buscarPorTelefono("+5491187654321");
    }

    @Test
    @DisplayName("GET /api/cocina/pedidos - Con rol USER retorna 403")
    @WithMockUser(roles = "USER")
    void testListarPedidosConRolUser() throws Exception {
        mockMvc.perform(get("/api/cocina/pedidos")
                        .param("telefonoLocal", "+5491187654321"))
                .andExpect(status().isForbidden());

        verify(localService, never()).buscarPorTelefono(anyString());
    }

    @Test
    @DisplayName("PATCH /api/cocina/pedidos/{id}/estado - Cambiar estado exitoso")
    @WithMockUser(roles = "COCINA")
    void testCambiarEstadoPedidoExitoso() throws Exception {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        doNothing().when(webSocketService).emitirActualizacionPedido(any(Pedido.class));

        // Act & Assert
        mockMvc.perform(patch("/api/cocina/pedidos/1/estado")
                        .param("nuevoEstado", "EN_PREPARACION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(webSocketService, times(1)).emitirActualizacionPedido(any(Pedido.class));
    }

    @Test
    @DisplayName("PATCH /api/cocina/pedidos/{id}/estado - Pedido no encontrado")
    @WithMockUser(roles = "COCINA")
    void testCambiarEstadoPedidoNoEncontrado() throws Exception {
        // Arrange
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(patch("/api/cocina/pedidos/999/estado")
                        .param("nuevoEstado", "EN_PREPARACION"))
                .andExpect(status().is5xxServerError());

        verify(pedidoRepository, times(1)).findById(999L);
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(webSocketService, never()).emitirActualizacionPedido(any(Pedido.class));
    }

    @Test
    @DisplayName("PATCH /api/cocina/pedidos/{id}/estado - Estados múltiples")
    @WithMockUser(roles = "COCINA")
    void testCambiarEstadosMultiples() throws Exception {
        // Test CONFIRMADO
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        doNothing().when(webSocketService).emitirActualizacionPedido(any(Pedido.class));

        mockMvc.perform(patch("/api/cocina/pedidos/1/estado")
                        .param("nuevoEstado", "CONFIRMADO"))
                .andExpect(status().isOk());

        // Test LISTO
        mockMvc.perform(patch("/api/cocina/pedidos/1/estado")
                        .param("nuevoEstado", "LISTO"))
                .andExpect(status().isOk());

        // Test ENTREGADO
        mockMvc.perform(patch("/api/cocina/pedidos/1/estado")
                        .param("nuevoEstado", "ENTREGADO"))
                .andExpect(status().isOk());

        verify(pedidoRepository, times(3)).findById(1L);
        verify(pedidoRepository, times(3)).save(any(Pedido.class));
        verify(webSocketService, times(3)).emitirActualizacionPedido(any(Pedido.class));
    }

    @Test
    @DisplayName("PATCH /api/cocina/pedidos/{id}/estado - WebSocket falla pero continúa")
    @WithMockUser(roles = "COCINA")
    void testCambiarEstadoWebSocketFalla() throws Exception {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        doThrow(new RuntimeException("WebSocket error"))
                .when(webSocketService).emitirActualizacionPedido(any(Pedido.class));

        // Act & Assert - El endpoint debe seguir funcionando aunque WebSocket falle
        mockMvc.perform(patch("/api/cocina/pedidos/1/estado")
                        .param("nuevoEstado", "EN_PREPARACION"))
                .andExpect(status().isOk());

        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(webSocketService, times(1)).emitirActualizacionPedido(any(Pedido.class));
    }

    @Test
    @DisplayName("GET /api/cocina/pedidos - Lista vacía cuando no hay pedidos")
    @WithMockUser(roles = "COCINA")
    void testListarPedidosVacio() throws Exception {
        // Arrange
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(pedidoRepository.findByLocalAndEstadoIn(any(Local.class), anyList()))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/cocina/pedidos")
                        .param("telefonoLocal", "+5491187654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(localService, times(1)).buscarPorTelefono("+5491187654321");
        verify(pedidoRepository, times(1)).findByLocalAndEstadoIn(any(Local.class), anyList());
    }
}
