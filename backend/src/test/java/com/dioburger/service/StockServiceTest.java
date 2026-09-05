package com.dioburger.service;

import com.dioburger.exception.StockInsuficienteException;
import com.dioburger.model.entity.*;
import com.dioburger.model.enums.UnidadMedida;
import com.dioburger.repository.IngredienteRepository;
import com.dioburger.repository.ProductoRepository;
import com.dioburger.repository.RecetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para StockService.
 * Valida la lógica de verificación y descuento de stock.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockService - Tests Unitarios")
class StockServiceTest {

    @Mock
    private RecetaRepository recetaRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private StockService stockService;

    private Producto producto;
    private Ingrediente ingredientePan;
    private Ingrediente ingredienteCarne;
    private Ingrediente ingredienteQueso;
    private Receta recetaPan;
    private Receta recetaCarne;
    private Receta recetaQueso;

    @BeforeEach
    void setUp() {
        // Producto: Hamburguesa Clásica
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Hamburguesa Clásica");

        // Ingrediente: Pan
        ingredientePan = new Ingrediente();
        ingredientePan.setId(1L);
        ingredientePan.setNombre("Pan");
        ingredientePan.setUnidadMedida(UnidadMedida.UNIDAD);
        ingredientePan.setStockActual(BigDecimal.valueOf(100));

        // Ingrediente: Carne (porciones de 150g cada una)
        ingredienteCarne = new Ingrediente();
        ingredienteCarne.setId(2L);
        ingredienteCarne.setNombre("Carne");
        ingredienteCarne.setUnidadMedida(UnidadMedida.PORCION);
        ingredienteCarne.setStockActual(BigDecimal.valueOf(50)); // 50 porciones

        // Ingrediente: Queso
        ingredienteQueso = new Ingrediente();
        ingredienteQueso.setId(3L);
        ingredienteQueso.setNombre("Queso");
        ingredienteQueso.setUnidadMedida(UnidadMedida.FETA);
        ingredienteQueso.setStockActual(BigDecimal.valueOf(100)); // 100 fetas

        // Receta: 2 panes por hamburguesa
        recetaPan = new Receta();
        recetaPan.setId(1L);
        recetaPan.setProducto(producto);
        recetaPan.setIngrediente(ingredientePan);
        recetaPan.setCantidadRequerida(BigDecimal.valueOf(2));

        // Receta: 1 porción de carne por hamburguesa
        recetaCarne = new Receta();
        recetaCarne.setId(2L);
        recetaCarne.setProducto(producto);
        recetaCarne.setIngrediente(ingredienteCarne);
        recetaCarne.setCantidadRequerida(BigDecimal.ONE);

        // Receta: 2 fetas de queso por hamburguesa
        recetaQueso = new Receta();
        recetaQueso.setId(3L);
        recetaQueso.setProducto(producto);
        recetaQueso.setIngrediente(ingredienteQueso);
        recetaQueso.setCantidadRequerida(BigDecimal.valueOf(2));
    }

    // ==================== TESTS - verificarDisponibilidad ====================

    @Test
    @DisplayName("verificarDisponibilidad - Producto sin recetas retorna false")
    void verificarDisponibilidad_productoSinRecetas_retornaFalse() {
        when(recetaRepository.findByProducto(producto)).thenReturn(List.of());

        boolean disponible = stockService.verificarDisponibilidad(producto);

        assertFalse(disponible);
        verify(recetaRepository, times(1)).findByProducto(producto);
    }

    @Test
    @DisplayName("verificarDisponibilidad - Stock suficiente retorna true")
    void verificarDisponibilidad_stockSuficiente_retornaTrue() {
        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        boolean disponible = stockService.verificarDisponibilidad(producto);

        assertTrue(disponible);
        verify(recetaRepository, times(1)).findByProducto(producto);
    }

    @Test
    @DisplayName("verificarDisponibilidad - Stock insuficiente de un ingrediente retorna false")
    void verificarDisponibilidad_stockInsuficiente_retornaFalse() {
        // Pan con stock insuficiente (solo 1 unidad, requiere 2)
        ingredientePan.setStockActual(BigDecimal.ONE);

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        boolean disponible = stockService.verificarDisponibilidad(producto);

        assertFalse(disponible);
        verify(recetaRepository, times(1)).findByProducto(producto);
    }

    @Test
    @DisplayName("verificarDisponibilidad - Stock exacto retorna true")
    void verificarDisponibilidad_stockExacto_retornaTrue() {
        // Stock exacto: 2 panes, 1 porción carne, 2 fetas queso
        ingredientePan.setStockActual(BigDecimal.valueOf(2));
        ingredienteCarne.setStockActual(BigDecimal.ONE);
        ingredienteQueso.setStockActual(BigDecimal.valueOf(2));

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        boolean disponible = stockService.verificarDisponibilidad(producto);

        assertTrue(disponible);
    }

    @Test
    @DisplayName("verificarDisponibilidad - Stock cero retorna false")
    void verificarDisponibilidad_stockCero_retornaFalse() {
        ingredienteQueso.setStockActual(BigDecimal.ZERO);

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        boolean disponible = stockService.verificarDisponibilidad(producto);

        assertFalse(disponible);
    }

    @Test
    @DisplayName("verificarDisponibilidad - Múltiples ingredientes insuficientes retorna false")
    void verificarDisponibilidad_multipleIngredientesInsuficientes_retornaFalse() {
        ingredientePan.setStockActual(BigDecimal.ONE);
        ingredienteQueso.setStockActual(BigDecimal.valueOf(10));

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        boolean disponible = stockService.verificarDisponibilidad(producto);

        assertFalse(disponible);
    }

    // ==================== TESTS - descontarStock ====================

    @Test
    @DisplayName("descontarStock - Descuenta correctamente el stock de un item")
    void descontarStock_unItem_descontaCorrectamente() {
        PedidoItem item = new PedidoItem();
        item.setProducto(producto);
        item.setCantidad(1);

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        stockService.descontarStock(List.of(item));

        // Verificar que se descontó el stock: 1 hamburguesa = 2 panes + 1 porción + 2 fetas
        assertEquals(BigDecimal.valueOf(98), ingredientePan.getStockActual()); // 100 - 2
        assertEquals(BigDecimal.valueOf(49), ingredienteCarne.getStockActual()); // 50 - 1
        assertEquals(BigDecimal.valueOf(98), ingredienteQueso.getStockActual()); // 100 - 2

        // Verificar que se guardaron los ingredientes
        verify(ingredienteRepository, times(3)).save(any(Ingrediente.class));
    }

    @Test
    @DisplayName("descontarStock - Descuenta correctamente con cantidad mayor a 1")
    void descontarStock_cantidadMayor_descontaCorrectamente() {
        PedidoItem item = new PedidoItem();
        item.setProducto(producto);
        item.setCantidad(5); // 5 hamburguesas

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        stockService.descontarStock(List.of(item));

        // Verificar descuento: 5 hamburguesas = 10 panes + 5 porciones + 10 fetas
        assertEquals(BigDecimal.valueOf(90), ingredientePan.getStockActual()); // 100 - (2*5)
        assertEquals(BigDecimal.valueOf(45), ingredienteCarne.getStockActual()); // 50 - (1*5)
        assertEquals(BigDecimal.valueOf(90), ingredienteQueso.getStockActual()); // 100 - (2*5)

        verify(ingredienteRepository, times(3)).save(any(Ingrediente.class));
    }

    @Test
    @DisplayName("descontarStock - Múltiples items se descuentan correctamente")
    void descontarStock_multipleItems_descontaCorrectamente() {
        PedidoItem item1 = new PedidoItem();
        item1.setProducto(producto);
        item1.setCantidad(2);

        PedidoItem item2 = new PedidoItem();
        item2.setProducto(producto);
        item2.setCantidad(3);

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        stockService.descontarStock(List.of(item1, item2));

        // Total: 5 hamburguesas (2+3) = 10 panes + 5 porciones + 10 fetas
        assertEquals(BigDecimal.valueOf(90), ingredientePan.getStockActual()); // 100 - (2*5)
        assertEquals(BigDecimal.valueOf(45), ingredienteCarne.getStockActual()); // 50 - (1*5)
        assertEquals(BigDecimal.valueOf(90), ingredienteQueso.getStockActual()); // 100 - (2*5)

        verify(ingredienteRepository, times(6)).save(any(Ingrediente.class)); // 3 ingredientes x 2 items
    }

    @Test
    @DisplayName("descontarStock - Stock insuficiente lanza StockInsuficienteException")
    void descontarStock_stockInsuficiente_lanzaException() {
        PedidoItem item = new PedidoItem();
        item.setProducto(producto);
        item.setCantidad(60); // Requiere 60 porciones de carne, solo hay 50

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        assertThrows(StockInsuficienteException.class, () -> {
            stockService.descontarStock(List.of(item));
        });

        // Verificar que NO se guardó ningún ingrediente (rollback)
        verify(ingredienteRepository, never()).save(any(Ingrediente.class));
    }

    @Test
    @DisplayName("descontarStock - Stock insuficiente en segundo ingrediente lanza excepción")
    void descontarStock_stockInsuficienteSegundoIngrediente_lanzaException() {
        // Pan suficiente, carne suficiente, pero queso insuficiente
        ingredienteQueso.setStockActual(BigDecimal.ONE); // Solo 1 feta, requiere 2

        PedidoItem item = new PedidoItem();
        item.setProducto(producto);
        item.setCantidad(1);

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        assertThrows(StockInsuficienteException.class, () -> {
            stockService.descontarStock(List.of(item));
        });

        // Nota: El servicio actual SÍ guarda los ingredientes anteriores antes de fallar
        // Esto es parte del comportamiento transaccional, pero la transacción hace rollback
        verify(ingredienteRepository, atLeastOnce()).save(any(Ingrediente.class));
    }

    @Test
    @DisplayName("descontarStock - Stock exacto descuenta hasta cero")
    void descontarStock_stockExacto_descontaHastaCero() {
        // Stock justo para 1 hamburguesa: 2 panes + 1 porción + 2 fetas
        ingredientePan.setStockActual(BigDecimal.valueOf(2));
        ingredienteCarne.setStockActual(BigDecimal.ONE);
        ingredienteQueso.setStockActual(BigDecimal.valueOf(2));

        PedidoItem item = new PedidoItem();
        item.setProducto(producto);
        item.setCantidad(1);

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        stockService.descontarStock(List.of(item));

        // Verificar que el stock quedó en 0
        assertEquals(BigDecimal.ZERO, ingredientePan.getStockActual());
        assertEquals(BigDecimal.ZERO, ingredienteCarne.getStockActual());
        assertEquals(BigDecimal.ZERO, ingredienteQueso.getStockActual());

        verify(ingredienteRepository, times(3)).save(any(Ingrediente.class));
    }

    @Test
    @DisplayName("descontarStock - Producto sin recetas no descuenta nada")
    void descontarStock_productoSinRecetas_noDescontaNada() {
        PedidoItem item = new PedidoItem();
        item.setProducto(producto);
        item.setCantidad(5);

        when(recetaRepository.findByProducto(producto)).thenReturn(List.of());

        stockService.descontarStock(List.of(item));

        // No debe haber guardado ningún ingrediente
        verify(ingredienteRepository, never()).save(any(Ingrediente.class));
    }

    @Test
    @DisplayName("descontarStock - Cantidad cero no descuenta stock")
    void descontarStock_cantidadCero_noDescontaStock() {
        PedidoItem item = new PedidoItem();
        item.setProducto(producto);
        item.setCantidad(0);

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        stockService.descontarStock(List.of(item));

        // Stock no debería cambiar
        assertEquals(BigDecimal.valueOf(100), ingredientePan.getStockActual());
        assertEquals(BigDecimal.valueOf(50), ingredienteCarne.getStockActual());
        assertEquals(BigDecimal.valueOf(100), ingredienteQueso.getStockActual());

        verify(ingredienteRepository, times(3)).save(any(Ingrediente.class));
    }

    // ==================== TESTS - Edge Cases ====================

    @Test
    @DisplayName("verificarDisponibilidad - Producto con un solo ingrediente")
    void verificarDisponibilidad_unSoloIngrediente_funciona() {
        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan));

        boolean disponible = stockService.verificarDisponibilidad(producto);

        assertTrue(disponible);
    }

    @Test
    @DisplayName("descontarStock - Descuento con decimales funciona correctamente")
    void descontarStock_conDecimales_funciona() {
        // Ingrediente con stock decimal
        ingredienteCarne.setStockActual(BigDecimal.valueOf(50.50));
        recetaCarne.setCantidadRequerida(BigDecimal.valueOf(1.25));

        PedidoItem item = new PedidoItem();
        item.setProducto(producto);
        item.setCantidad(2); // 2 hamburguesas = 2.50 porciones

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaCarne));

        stockService.descontarStock(List.of(item));

        // 50.50 - (1.25 * 2) = 50.50 - 2.50 = 48.00
        assertEquals(0, BigDecimal.valueOf(48.00).compareTo(ingredienteCarne.getStockActual()));

        verify(ingredienteRepository, times(1)).save(ingredienteCarne);
    }

    @Test
    @DisplayName("descontarStock - Exception contiene información detallada")
    void descontarStock_exception_contieneDetalles() {
        ingredienteQueso.setStockActual(BigDecimal.ONE); // Solo 1 feta

        PedidoItem item = new PedidoItem();
        item.setProducto(producto);
        item.setCantidad(1);

        when(recetaRepository.findByProducto(producto))
                .thenReturn(List.of(recetaPan, recetaCarne, recetaQueso));

        StockInsuficienteException exception = assertThrows(
                StockInsuficienteException.class,
                () -> stockService.descontarStock(List.of(item))
        );

        // Verificar que el mensaje contiene información útil
        assertTrue(exception.getMessage().contains("Queso"));
        assertTrue(exception.getMessage().contains("2")); // Cantidad requerida
        assertTrue(exception.getMessage().contains("1")); // Stock disponible
        assertTrue(exception.getMessage().contains("FETA")); // Unidad
    }
}
