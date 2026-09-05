package com.dioburger.service;

import com.dioburger.exception.NotFoundException;
import com.dioburger.mapper.ProductoMapper;
import com.dioburger.model.dto.MenuResponseDTO;
import com.dioburger.model.dto.ProductoDTO;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Producto;
import com.dioburger.model.enums.TipoProducto;
import com.dioburger.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para MenuService.
 * Valida la lógica de construcción del menú dinámico del local.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de MenuService")
class MenuServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private StockService stockService;

    @Mock
    private HorarioService horarioService;

    @Mock
    private ProductoMapper productoMapper;

    @Mock
    private PromocionService promocionService;

    @Mock
    private LocalService localService;

    @InjectMocks
    private MenuService menuService;

    private Local local;
    private ConfiguracionLocal config;
    private Producto productoHamburguesa;
    private Producto productoBebida;
    private ProductoDTO productoDTO1;
    private ProductoDTO productoDTO2;

    @BeforeEach
    void setUp() {
        // Configuración del local
        config = ConfiguracionLocal.builder()
                .id(1L)
                .permiteDelivery(true)
                .permiteTakeAway(true)
                .permiteReservas(true)
                .build();

        local = Local.builder()
                .id(1L)
                .nombre("Dio Burger Centro")
                .direccion("Av. Principal 123")
                .telefono("549349366512")
                .configuracion(config)
                .build();

        // Productos
        productoHamburguesa = Producto.builder()
                .id(1L)
                .nombre("Hamburguesa Clásica")
                .descripcion("Hamburguesa con carne y queso")
                .precio(new BigDecimal("1500.00"))
                .precioBase(new BigDecimal("1500.00"))
                .estaAgotado(false)
                .tipoProducto(TipoProducto.CON_RECETA)
                .local(local)
                .build();

        productoBebida = Producto.builder()
                .id(2L)
                .nombre("Coca Cola")
                .descripcion("Bebida 500ml")
                .precio(new BigDecimal("500.00"))
                .precioBase(new BigDecimal("500.00"))
                .estaAgotado(false)
                .tipoProducto(TipoProducto.SIMPLE)
                .local(local)
                .build();

        // DTOs
        productoDTO1 = ProductoDTO.builder()
                .id(1L)
                .nombre("Hamburguesa Clásica")
                .descripcion("Hamburguesa con carne y queso")
                .precio(new BigDecimal("1500.00"))
                .disponible(true)
                .tipoProducto(TipoProducto.CON_RECETA)
                .build();

        productoDTO2 = ProductoDTO.builder()
                .id(2L)
                .nombre("Coca Cola")
                .descripcion("Bebida 500ml")
                .precio(new BigDecimal("500.00"))
                .disponible(true)
                .tipoProducto(TipoProducto.SIMPLE)
                .build();
    }

    @Test
    @DisplayName("obtenerMenuCompleto - Local válido con productos retorna menú completo")
    void testObtenerMenuCompleto_LocalValido_RetornaMenuCompleto() {
        // Arrange
        List<Producto> productos = Arrays.asList(productoHamburguesa, productoBebida);
        List<String> horarios = Arrays.asList("12:00", "12:30", "13:00");

        when(localService.buscarPorTelefono("549349366512")).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(productoMapper.toDTO(productoHamburguesa)).thenReturn(productoDTO1);
        when(productoMapper.toDTO(productoBebida)).thenReturn(productoDTO2);
        when(horarioService.getHorariosSugeridosPedidos("549349366512")).thenReturn(horarios);

        // Act
        MenuResponseDTO result = menuService.obtenerMenuCompleto("549349366512");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getLocal()).isNotNull();
        assertThat(result.getLocal().getNombre()).isEqualTo("Dio Burger Centro");
        assertThat(result.getLocal().getDireccion()).isEqualTo("Av. Principal 123");
        assertThat(result.getLocal().getTelefono()).isEqualTo("549349366512");

        assertThat(result.getProductos()).hasSize(2);
        assertThat(result.getProductos()).contains(productoDTO1, productoDTO2);

        assertThat(result.getHorariosSugeridos()).hasSize(3);
        assertThat(result.getHorariosSugeridos()).containsExactly("12:00", "12:30", "13:00");

        assertThat(result.getModalidadesPermitidas()).hasSize(2);
        assertThat(result.getModalidadesPermitidas()).containsExactlyInAnyOrder("DELIVERY", "RETIRAR");

        assertThat(result.getPermiteReservas()).isTrue();

        // Verify interactions
        verify(localService).buscarPorTelefono("549349366512");
        verify(stockService).actualizarDisponibilidadProductos(local);
        verify(productoRepository).findByLocal(local);
        verify(productoMapper, times(2)).toDTO(any(Producto.class));
        verify(horarioService).getHorariosSugeridosPedidos("549349366512");
    }

    @Test
    @DisplayName("obtenerMenuCompleto - Local sin configuración lanza NotFoundException")
    void testObtenerMenuCompleto_LocalSinConfiguracion_LanzaNotFoundException() {
        // Arrange
        Local localSinConfig = Local.builder()
                .id(1L)
                .nombre("Dio Burger Centro")
                .telefono("549349366512")
                .configuracion(null)
                .build();

        when(localService.buscarPorTelefono("549349366512")).thenReturn(localSinConfig);

        // Act & Assert
        assertThatThrownBy(() -> menuService.obtenerMenuCompleto("549349366512"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no tiene configuración");

        verify(localService).buscarPorTelefono("549349366512");
        verify(stockService, never()).actualizarDisponibilidadProductos(any());
        verify(productoRepository, never()).findByLocal(any());
    }

    @Test
    @DisplayName("obtenerMenuCompleto - Solo permite DELIVERY cuando takeAway es false")
    void testObtenerMenuCompleto_SoloDelivery_RetornaUnaModalidad() {
        // Arrange
        config.setPermiteTakeAway(false);
        List<Producto> productos = Arrays.asList(productoHamburguesa);

        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(productoMapper.toDTO(any())).thenReturn(productoDTO1);
        when(horarioService.getHorariosSugeridosPedidos(anyString())).thenReturn(Arrays.asList("12:00"));

        // Act
        MenuResponseDTO result = menuService.obtenerMenuCompleto("549349366512");

        // Assert
        assertThat(result.getModalidadesPermitidas()).hasSize(1);
        assertThat(result.getModalidadesPermitidas()).containsExactly("DELIVERY");
    }

    @Test
    @DisplayName("obtenerMenuCompleto - Solo permite RETIRAR cuando delivery es false")
    void testObtenerMenuCompleto_SoloRetirar_RetornaUnaModalidad() {
        // Arrange
        config.setPermiteDelivery(false);
        List<Producto> productos = Arrays.asList(productoHamburguesa);

        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(productoMapper.toDTO(any())).thenReturn(productoDTO1);
        when(horarioService.getHorariosSugeridosPedidos(anyString())).thenReturn(Arrays.asList("12:00"));

        // Act
        MenuResponseDTO result = menuService.obtenerMenuCompleto("549349366512");

        // Assert
        assertThat(result.getModalidadesPermitidas()).hasSize(1);
        assertThat(result.getModalidadesPermitidas()).containsExactly("RETIRAR");
    }

    @Test
    @DisplayName("obtenerMenuCompleto - No permite ninguna modalidad cuando ambas son false")
    void testObtenerMenuCompleto_NingunaModalidad_RetornaListaVacia() {
        // Arrange
        config.setPermiteDelivery(false);
        config.setPermiteTakeAway(false);
        List<Producto> productos = Arrays.asList(productoHamburguesa);

        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(productoMapper.toDTO(any())).thenReturn(productoDTO1);
        when(horarioService.getHorariosSugeridosPedidos(anyString())).thenReturn(Arrays.asList("12:00"));

        // Act
        MenuResponseDTO result = menuService.obtenerMenuCompleto("549349366512");

        // Assert
        assertThat(result.getModalidadesPermitidas()).isEmpty();
    }

    @Test
    @DisplayName("obtenerMenuCompleto - permiteReservas false se refleja correctamente")
    void testObtenerMenuCompleto_NoPermiteReservas_RetornaFalse() {
        // Arrange
        config.setPermiteReservas(false);
        List<Producto> productos = Arrays.asList(productoHamburguesa);

        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(productoMapper.toDTO(any())).thenReturn(productoDTO1);
        when(horarioService.getHorariosSugeridosPedidos(anyString())).thenReturn(Arrays.asList("12:00"));

        // Act
        MenuResponseDTO result = menuService.obtenerMenuCompleto("549349366512");

        // Assert
        assertThat(result.getPermiteReservas()).isFalse();
    }

    @Test
    @DisplayName("obtenerMenuCompleto - Local sin productos retorna lista vacía")
    void testObtenerMenuCompleto_SinProductos_RetornaListaVacia() {
        // Arrange
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(Arrays.asList());
        when(horarioService.getHorariosSugeridosPedidos(anyString())).thenReturn(Arrays.asList("12:00"));

        // Act
        MenuResponseDTO result = menuService.obtenerMenuCompleto("549349366512");

        // Assert
        assertThat(result.getProductos()).isEmpty();
    }

    @Test
    @DisplayName("obtenerMenuCompleto - Sin horarios disponibles retorna lista vacía")
    void testObtenerMenuCompleto_SinHorarios_RetornaListaVacia() {
        // Arrange
        List<Producto> productos = Arrays.asList(productoHamburguesa);

        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(productoMapper.toDTO(any())).thenReturn(productoDTO1);
        when(horarioService.getHorariosSugeridosPedidos(anyString())).thenReturn(Arrays.asList());

        // Act
        MenuResponseDTO result = menuService.obtenerMenuCompleto("549349366512");

        // Assert
        assertThat(result.getHorariosSugeridos()).isEmpty();
    }

    @Test
    @DisplayName("obtenerHamburguesasConStock - Local con stock retorna hamburguesas disponibles")
    void testObtenerHamburguesasConStock_ConStock_RetornaDisponibles() {
        // Arrange
        List<Producto> productos = Arrays.asList(productoHamburguesa, productoBebida);

        when(localService.buscarPorTelefono("549349366512")).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(stockService.calcularCantidadMaximaDisponible(productoHamburguesa)).thenReturn(10);
        when(stockService.calcularCantidadMaximaDisponible(productoBebida)).thenReturn(20);
        when(promocionService.calcularPrecioActual(any())).thenReturn(new BigDecimal("1500.00"));
        when(promocionService.esPromocionActiva(any())).thenReturn(false);

        // Act
        Map<String, Object> result = menuService.obtenerHamburguesasConStock("549349366512");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).containsKey("hamburguesas");
        assertThat(result).containsKey("totalHamburguesas");
        assertThat(result).containsKey("hamburguesasConStock");
        assertThat(result).containsKey("hamburguesasAgotadas");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> hamburguesas = (List<Map<String, Object>>) result.get("hamburguesas");
        assertThat(hamburguesas).hasSize(2);

        assertThat(result.get("totalHamburguesas")).isEqualTo(2L);
        assertThat(result.get("hamburguesasConStock")).isEqualTo(2L);
        assertThat(result.get("hamburguesasAgotadas")).isEqualTo(0L);

        verify(localService).buscarPorTelefono("549349366512");
        verify(productoRepository).findByLocal(local);
        verify(stockService, times(2)).calcularCantidadMaximaDisponible(any());
        verify(promocionService, times(2)).calcularPrecioActual(any());
        verify(promocionService, times(2)).esPromocionActiva(any());
    }

    @Test
    @DisplayName("obtenerHamburguesasConStock - Productos con promoción activa incluyen descuento")
    void testObtenerHamburguesasConStock_ConPromocion_IncluyeDescuento() {
        // Arrange
        List<Producto> productos = Arrays.asList(productoHamburguesa);

        when(localService.buscarPorTelefono("549349366512")).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(stockService.calcularCantidadMaximaDisponible(productoHamburguesa)).thenReturn(5);
        when(promocionService.calcularPrecioActual(productoHamburguesa)).thenReturn(new BigDecimal("1200.00"));
        when(promocionService.esPromocionActiva(productoHamburguesa)).thenReturn(true);
        when(promocionService.calcularPorcentajeDescuento(productoHamburguesa)).thenReturn(new BigDecimal("20"));
        when(promocionService.obtenerInfoPromocion(productoHamburguesa)).thenReturn("Happy Hour");

        // Act
        Map<String, Object> result = menuService.obtenerHamburguesasConStock("549349366512");

        // Assert
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> hamburguesas = (List<Map<String, Object>>) result.get("hamburguesas");
        Map<String, Object> hamburguesa = hamburguesas.get(0);

        assertThat(hamburguesa).containsEntry("enPromocion", true);
        assertThat(hamburguesa).containsEntry("precioActual", new BigDecimal("1200.00"));
        assertThat(hamburguesa).containsEntry("descuentoPorcentaje", new BigDecimal("20"));
        assertThat(hamburguesa).containsEntry("infoPromocion", "Happy Hour");

        verify(promocionService).calcularPorcentajeDescuento(productoHamburguesa);
        verify(promocionService).obtenerInfoPromocion(productoHamburguesa);
    }

    @Test
    @DisplayName("obtenerHamburguesasConStock - Productos sin stock cuentan como agotados")
    void testObtenerHamburguesasConStock_SinStock_CuentaComoAgotados() {
        // Arrange
        List<Producto> productos = Arrays.asList(productoHamburguesa, productoBebida);

        when(localService.buscarPorTelefono("549349366512")).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(stockService.calcularCantidadMaximaDisponible(productoHamburguesa)).thenReturn(0);
        when(stockService.calcularCantidadMaximaDisponible(productoBebida)).thenReturn(0);
        when(promocionService.calcularPrecioActual(any())).thenReturn(new BigDecimal("1500.00"));
        when(promocionService.esPromocionActiva(any())).thenReturn(false);

        // Act
        Map<String, Object> result = menuService.obtenerHamburguesasConStock("549349366512");

        // Assert
        assertThat(result.get("totalHamburguesas")).isEqualTo(2L);
        assertThat(result.get("hamburguesasConStock")).isEqualTo(0L);
        assertThat(result.get("hamburguesasAgotadas")).isEqualTo(2L);
    }

    @Test
    @DisplayName("obtenerHamburguesasConStock - Descripción null se convierte en string vacío")
    void testObtenerHamburguesasConStock_DescripcionNull_RetornaStringVacio() {
        // Arrange
        productoHamburguesa.setDescripcion(null);
        List<Producto> productos = Arrays.asList(productoHamburguesa);

        when(localService.buscarPorTelefono("549349366512")).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(stockService.calcularCantidadMaximaDisponible(any())).thenReturn(5);
        when(promocionService.calcularPrecioActual(any())).thenReturn(new BigDecimal("1500.00"));
        when(promocionService.esPromocionActiva(any())).thenReturn(false);

        // Act
        Map<String, Object> result = menuService.obtenerHamburguesasConStock("549349366512");

        // Assert
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> hamburguesas = (List<Map<String, Object>>) result.get("hamburguesas");
        Map<String, Object> hamburguesa = hamburguesas.get(0);

        assertThat(hamburguesa).containsEntry("descripcion", "");
    }

    @Test
    @DisplayName("obtenerHamburguesasConStock - Incluye información del local en la respuesta")
    void testObtenerHamburguesasConStock_IncluyeInfoLocal() {
        // Arrange
        List<Producto> productos = Arrays.asList(productoHamburguesa);

        when(localService.buscarPorTelefono("549349366512")).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(stockService.calcularCantidadMaximaDisponible(any())).thenReturn(10);
        when(promocionService.calcularPrecioActual(any())).thenReturn(new BigDecimal("1500.00"));
        when(promocionService.esPromocionActiva(any())).thenReturn(false);

        // Act
        Map<String, Object> result = menuService.obtenerHamburguesasConStock("549349366512");

        // Assert
        assertThat(result).containsKey("local");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> localInfo = (Map<String, Object>) result.get("local");
        assertThat(localInfo).containsEntry("nombre", "Dio Burger Centro");
        assertThat(localInfo).containsEntry("telefono", "549349366512");
    }

    @Test
    @DisplayName("obtenerHamburguesasConStock - Producto sin precioBase usa precio")
    void testObtenerHamburguesasConStock_SinPrecioBase_UsaPrecio() {
        // Arrange
        productoHamburguesa.setPrecioBase(null);
        List<Producto> productos = Arrays.asList(productoHamburguesa);

        when(localService.buscarPorTelefono("549349366512")).thenReturn(local);
        when(productoRepository.findByLocal(local)).thenReturn(productos);
        when(stockService.calcularCantidadMaximaDisponible(any())).thenReturn(5);
        when(promocionService.calcularPrecioActual(any())).thenReturn(new BigDecimal("1500.00"));
        when(promocionService.esPromocionActiva(any())).thenReturn(false);

        // Act
        Map<String, Object> result = menuService.obtenerHamburguesasConStock("549349366512");

        // Assert
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> hamburguesas = (List<Map<String, Object>>) result.get("hamburguesas");
        Map<String, Object> hamburguesa = hamburguesas.get(0);

        assertThat(hamburguesa).containsEntry("precioBase", new BigDecimal("1500.00"));
    }
}
