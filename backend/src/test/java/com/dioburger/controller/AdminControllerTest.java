package com.dioburger.controller;

import com.dioburger.model.entity.Ingrediente;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Producto;
import com.dioburger.model.entity.Receta;
import com.dioburger.model.enums.UnidadMedida;
import com.dioburger.repository.IngredienteRepository;
import com.dioburger.repository.ProductoRepository;
import com.dioburger.service.LocalService;
import com.dioburger.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para AdminController.
 * Valida los endpoints de administración de productos e ingredientes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests de AdminController")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoRepository productoRepository;

    @MockBean
    private IngredienteRepository ingredienteRepository;

    @MockBean
    private LocalService localService;

    @MockBean
    private StockService stockService;

    private Local local;
    private Producto producto;
    private Ingrediente ingrediente;

    @BeforeEach
    void setUp() {
        // Local de prueba
        local = Local.builder()
                .id(1L)
                .nombre("Dio Burger Centro")
                .telefono("+5491187654321")
                .direccion("Av. Principal 123")
                .build();

        // Producto de prueba
        producto = Producto.builder()
                .id(1L)
                .nombre("Hamburguesa Clásica")
                .descripcion("Deliciosa hamburguesa con queso")
                .precio(BigDecimal.valueOf(5000))
                .estaAgotado(false)
                .local(local)
                .build();

        // Ingrediente de prueba
        ingrediente = Ingrediente.builder()
                .id(1L)
                .nombre("Pan")
                .unidadMedida(UnidadMedida.UNIDAD)
                .stockActual(BigDecimal.valueOf(100))
                .local(local)
                .build();
    }

    // ==================== TESTS DE PRODUCTOS ====================

    @Test
    @DisplayName("GET /api/admin/productos - Listar productos exitoso")
    @WithMockUser(roles = "ADMIN")
    void testListarProductosExitoso() throws Exception {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(productoRepository.findByLocal(any(Local.class))).thenReturn(productos);

        // Act & Assert
        mockMvc.perform(get("/api/admin/productos")
                        .param("telefonoLocal", "+5491187654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Hamburguesa Clásica"))
                .andExpect(jsonPath("$[0].precio").value(5000));

        verify(localService, times(1)).buscarPorTelefono("+5491187654321");
        verify(productoRepository, times(1)).findByLocal(any(Local.class));
    }

    @Test
    @DisplayName("GET /api/admin/productos - Sin autenticación retorna 403")
    void testListarProductosSinAuth() throws Exception {
        mockMvc.perform(get("/api/admin/productos")
                        .param("telefonoLocal", "+5491187654321"))
                .andExpect(status().isForbidden());

        verify(localService, never()).buscarPorTelefono(anyString());
        verify(productoRepository, never()).findByLocal(any(Local.class));
    }

    @Test
    @DisplayName("POST /api/admin/productos - Crear producto exitoso")
    @WithMockUser(roles = "ADMIN")
    void testCrearProductoExitoso() throws Exception {
        // Arrange
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act & Assert
        mockMvc.perform(post("/api/admin/productos")
                        .param("telefonoLocal", "+5491187654321")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Hamburguesa Clásica"));

        verify(localService, times(1)).buscarPorTelefono("+5491187654321");
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("PUT /api/admin/productos/{id} - Actualizar producto exitoso")
    @WithMockUser(roles = "ADMIN")
    void testActualizarProductoExitoso() throws Exception {
        // Arrange
        Producto productoActualizado = Producto.builder()
                .id(1L)
                .nombre("Hamburguesa Premium")
                .descripcion("Con ingredientes premium")
                .precio(BigDecimal.valueOf(7000))
                .build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActualizado);
        doNothing().when(stockService).actualizarDisponibilidadProducto(any(Producto.class));

        // Act & Assert
        mockMvc.perform(put("/api/admin/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Hamburguesa Premium"))
                .andExpect(jsonPath("$.precio").value(7000));

        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
        verify(stockService, times(1)).actualizarDisponibilidadProducto(any(Producto.class));
    }

    @Test
    @DisplayName("PUT /api/admin/productos/{id} - Producto no encontrado")
    @WithMockUser(roles = "ADMIN")
    void testActualizarProductoNoEncontrado() throws Exception {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/admin/productos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().is5xxServerError());

        verify(productoRepository, times(1)).findById(999L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("DELETE /api/admin/productos/{id} - Eliminar producto exitoso")
    @WithMockUser(roles = "ADMIN")
    void testEliminarProductoExitoso() throws Exception {
        // Arrange
        when(productoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/productos/1"))
                .andExpect(status().isNoContent());

        verify(productoRepository, times(1)).existsById(1L);
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("DELETE /api/admin/productos/{id} - Producto no encontrado")
    @WithMockUser(roles = "ADMIN")
    void testEliminarProductoNoEncontrado() throws Exception {
        // Arrange
        when(productoRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/productos/999"))
                .andExpect(status().is5xxServerError());

        verify(productoRepository, times(1)).existsById(999L);
        verify(productoRepository, never()).deleteById(anyLong());
    }

    // ==================== TESTS DE INGREDIENTES ====================

    @Test
    @DisplayName("GET /api/admin/ingredientes - Listar ingredientes exitoso")
    @WithMockUser(roles = "ADMIN")
    void testListarIngredientesExitoso() throws Exception {
        // Arrange
        List<Ingrediente> ingredientes = Arrays.asList(ingrediente);
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(ingredienteRepository.findByLocal(any(Local.class))).thenReturn(ingredientes);

        // Act & Assert
        mockMvc.perform(get("/api/admin/ingredientes")
                        .param("telefonoLocal", "+5491187654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Pan"))
                .andExpect(jsonPath("$[0].stockActual").value(100));

        verify(localService, times(1)).buscarPorTelefono("+5491187654321");
        verify(ingredienteRepository, times(1)).findByLocal(any(Local.class));
    }

    @Test
    @DisplayName("PUT /api/admin/ingredientes/{id}/stock - Actualizar stock exitoso")
    @WithMockUser(roles = "ADMIN")
    void testActualizarStockExitoso() throws Exception {
        // Arrange
        BigDecimal nuevoStock = BigDecimal.valueOf(150);
        Ingrediente ingredienteActualizado = Ingrediente.builder()
                .id(1L)
                .nombre("Pan")
                .stockActual(nuevoStock)
                .build();

        when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ingrediente));
        when(ingredienteRepository.save(any(Ingrediente.class))).thenReturn(ingredienteActualizado);
        doNothing().when(stockService).actualizarDisponibilidadProductos(any(Local.class));

        // Act & Assert
        mockMvc.perform(put("/api/admin/ingredientes/1/stock")
                        .param("nuevoStock", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(150));

        verify(ingredienteRepository, times(1)).findById(1L);
        verify(ingredienteRepository, times(1)).save(any(Ingrediente.class));
    }

    @Test
    @DisplayName("PUT /api/admin/ingredientes/{id}/stock - Ingrediente no encontrado")
    @WithMockUser(roles = "ADMIN")
    void testActualizarStockIngredienteNoEncontrado() throws Exception {
        // Arrange
        when(ingredienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/admin/ingredientes/999/stock")
                        .param("nuevoStock", "150"))
                .andExpect(status().is5xxServerError());

        verify(ingredienteRepository, times(1)).findById(999L);
        verify(ingredienteRepository, never()).save(any(Ingrediente.class));
    }

    @Test
    @DisplayName("PATCH /api/admin/productos/{id}/disponibilidad - Alterna estado agotado")
    @WithMockUser(roles = "ADMIN")
    void testCambiarDisponibilidadProducto() throws Exception {
        // Arrange: el producto arranca disponible (estaAgotado = false)
        Producto agotado = Producto.builder()
                .id(1L)
                .nombre("Hamburguesa Clásica")
                .precio(BigDecimal.valueOf(5000))
                .estaAgotado(true)
                .local(local)
                .build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(agotado);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/productos/1/disponibilidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estaAgotado").value(true));

        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("PUT /api/admin/ingredientes/{id} - Actualizar ingrediente exitoso")
    @WithMockUser(roles = "ADMIN")
    void testActualizarIngredienteExitoso() throws Exception {
        // Arrange
        Ingrediente datos = Ingrediente.builder()
                .nombre("Pan de papa")
                .stockActual(BigDecimal.valueOf(80))
                .unidadMedida(UnidadMedida.UNIDAD)
                .build();
        Ingrediente actualizado = Ingrediente.builder()
                .id(1L)
                .nombre("Pan de papa")
                .stockActual(BigDecimal.valueOf(80))
                .unidadMedida(UnidadMedida.UNIDAD)
                .local(local)
                .build();

        when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ingrediente));
        when(ingredienteRepository.save(any(Ingrediente.class))).thenReturn(actualizado);
        doNothing().when(stockService).actualizarDisponibilidadProductos(any(Local.class));

        // Act & Assert
        mockMvc.perform(put("/api/admin/ingredientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pan de papa"))
                .andExpect(jsonPath("$.stockActual").value(80));

        verify(ingredienteRepository, times(1)).findById(1L);
        verify(ingredienteRepository, times(1)).save(any(Ingrediente.class));
    }

    @Test
    @DisplayName("DELETE /api/admin/ingredientes/{id} - Eliminar ingrediente exitoso")
    @WithMockUser(roles = "ADMIN")
    void testEliminarIngredienteExitoso() throws Exception {
        // Arrange
        when(ingredienteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(ingredienteRepository).deleteById(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/ingredientes/1"))
                .andExpect(status().isNoContent());

        verify(ingredienteRepository, times(1)).existsById(1L);
        verify(ingredienteRepository, times(1)).deleteById(1L);
    }

    // ==================== TESTS DE RECETA ====================

    @Test
    @DisplayName("POST /api/admin/productos - Crea el producto con su receta")
    @WithMockUser(roles = "ADMIN")
    void testCrearProductoConReceta() throws Exception {
        // Arrange: body con un item de receta (solo id de ingrediente + cantidad)
        Producto body = Producto.builder()
                .nombre("Doble cheddar")
                .precio(BigDecimal.valueOf(6000))
                .recetas(new ArrayList<>(List.of(
                        Receta.builder()
                                .ingrediente(Ingrediente.builder().id(1L).build())
                                .cantidadRequerida(BigDecimal.valueOf(2))
                                .build())))
                .build();

        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ingrediente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });
        doNothing().when(stockService).actualizarDisponibilidadProducto(any(Producto.class));

        // Act & Assert
        mockMvc.perform(post("/api/admin/productos")
                        .param("telefonoLocal", "+5491187654321")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recetas.length()").value(1))
                .andExpect(jsonPath("$.recetas[0].ingrediente.id").value(1))
                .andExpect(jsonPath("$.recetas[0].cantidadRequerida").value(2));

        verify(ingredienteRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("PUT /api/admin/productos/{id} - Reemplaza la receta existente")
    @WithMockUser(roles = "ADMIN")
    void testActualizarProductoReemplazaReceta() throws Exception {
        // Arrange: el producto ya tiene una receta con el ingrediente 2
        Producto existente = Producto.builder()
                .id(1L)
                .nombre("Doble")
                .precio(BigDecimal.valueOf(5000))
                .estaAgotado(false)
                .local(local)
                .recetas(new ArrayList<>(List.of(
                        Receta.builder()
                                .id(99L)
                                .ingrediente(Ingrediente.builder().id(2L).build())
                                .cantidadRequerida(BigDecimal.ONE)
                                .build())))
                .build();

        // El body trae otra receta (ingrediente 1)
        Producto body = Producto.builder()
                .nombre("Doble")
                .precio(BigDecimal.valueOf(5000))
                .recetas(new ArrayList<>(List.of(
                        Receta.builder()
                                .ingrediente(Ingrediente.builder().id(1L).build())
                                .cantidadRequerida(BigDecimal.valueOf(3))
                                .build())))
                .build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ingrediente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(stockService).actualizarDisponibilidadProducto(any(Producto.class));

        // Act & Assert
        mockMvc.perform(put("/api/admin/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recetas.length()").value(1))
                .andExpect(jsonPath("$.recetas[0].ingrediente.id").value(1))
                .andExpect(jsonPath("$.recetas[0].cantidadRequerida").value(3));

        verify(ingredienteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/admin/productos - Con rol SUPERADMIN")
    @WithMockUser(roles = "SUPERADMIN")
    void testListarProductosConSuperAdmin() throws Exception {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(productoRepository.findByLocal(any(Local.class))).thenReturn(productos);

        // Act & Assert
        mockMvc.perform(get("/api/admin/productos")
                        .param("telefonoLocal", "+5491187654321"))
                .andExpect(status().isOk());

        verify(localService, times(1)).buscarPorTelefono("+5491187654321");
    }

    @Test
    @DisplayName("GET /api/admin/productos - Con rol USER retorna 403")
    @WithMockUser(roles = "USER")
    void testListarProductosConRolUser() throws Exception {
        mockMvc.perform(get("/api/admin/productos")
                        .param("telefonoLocal", "+5491187654321"))
                .andExpect(status().isForbidden());

        verify(localService, never()).buscarPorTelefono(anyString());
    }
}
