package com.dioburger.service;

import com.dioburger.exception.NotFoundException;
import com.dioburger.mapper.ProductoMapper;
import com.dioburger.model.dto.MenuCompletoDTO;
import com.dioburger.model.dto.ProductoDTO;
import com.dioburger.model.entity.Categoria;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Producto;
import com.dioburger.model.enums.TipoProducto;
import com.dioburger.repository.CategoriaRepository;
import com.dioburger.repository.LocalRepository;
import com.dioburger.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests para CatalogoService.
 * Valida la obtención del catálogo completo con categorías, productos, horarios y modalidades.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de CatalogoService")
class CatalogoServiceTest {

    @Mock
    private LocalRepository localRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private CatalogoService catalogoService;

    private Local local;
    private ConfiguracionLocal configuracion;
    private Categoria categoriaHamburguesas;
    private Categoria categoriaBebidas;
    private Producto hamburguesa1;
    private Producto hamburguesa2;
    private Producto bebida1;
    private ProductoDTO hamburguesaDTO1;
    private ProductoDTO hamburguesaDTO2;
    private ProductoDTO bebidaDTO1;

    @BeforeEach
    void setUp() {
        // Local
        local = Local.builder()
            .id(1L)
            .nombre("Dio Burger Palermo")
            .direccion("Av. Santa Fe 1234")
            .telefono("5491112345678")
            .build();

        // Configuración del local
        configuracion = ConfiguracionLocal.builder()
            .id(1L)
            .local(local)
            .horaApertura(LocalTime.of(11, 0))
            .horaCierre(LocalTime.of(23, 0))
            .intervaloMinutosPedidos(30)
            .permiteDelivery(true)
            .permiteTakeAway(true)
            .permiteReservas(true)
            .build();

        local.setConfiguracion(configuracion);

        // Categorías
        categoriaHamburguesas = Categoria.builder()
            .id(1L)
            .nombre("Hamburguesas")
            .descripcion("Nuestras hamburguesas gourmet")
            .orden(1)
            .activo(true)
            .local(local)
            .build();

        categoriaBebidas = Categoria.builder()
            .id(2L)
            .nombre("Bebidas")
            .descripcion("Bebidas frías y calientes")
            .orden(2)
            .activo(true)
            .local(local)
            .build();

        // Productos
        hamburguesa1 = Producto.builder()
            .id(1L)
            .nombre("Clásica")
            .descripcion("Hamburguesa clásica con cheddar")
            .precio(BigDecimal.valueOf(1500))
            .estaAgotado(false)
            .tipoProducto(TipoProducto.CON_RECETA)
            .categoria(categoriaHamburguesas)
            .local(local)
            .build();

        hamburguesa2 = Producto.builder()
            .id(2L)
            .nombre("Bacon Cheese")
            .descripcion("Con bacon y doble queso")
            .precio(BigDecimal.valueOf(1800))
            .estaAgotado(false)
            .tipoProducto(TipoProducto.CON_RECETA)
            .categoria(categoriaHamburguesas)
            .local(local)
            .build();

        bebida1 = Producto.builder()
            .id(3L)
            .nombre("Coca Cola 500ml")
            .descripcion("Gaseosa")
            .precio(BigDecimal.valueOf(500))
            .estaAgotado(false)
            .tipoProducto(TipoProducto.SIMPLE)
            .categoria(categoriaBebidas)
            .local(local)
            .build();

        // ProductoDTOs
        hamburguesaDTO1 = ProductoDTO.builder()
            .id(1L)
            .nombre("Clásica")
            .descripcion("Hamburguesa clásica con cheddar")
            .precio(BigDecimal.valueOf(1500))
            .disponible(true)
            .tipoProducto(TipoProducto.CON_RECETA)
            .build();

        hamburguesaDTO2 = ProductoDTO.builder()
            .id(2L)
            .nombre("Bacon Cheese")
            .descripcion("Con bacon y doble queso")
            .precio(BigDecimal.valueOf(1800))
            .disponible(true)
            .tipoProducto(TipoProducto.CON_RECETA)
            .build();

        bebidaDTO1 = ProductoDTO.builder()
            .id(3L)
            .nombre("Coca Cola 500ml")
            .descripcion("Gaseosa")
            .precio(BigDecimal.valueOf(500))
            .disponible(true)
            .tipoProducto(TipoProducto.SIMPLE)
            .build();
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - Local válido con productos - Retorna catálogo completo")
    void testObtenerCatalogoCompleto_LocalValido_RetornaCatalogoCompleto() {
        // Arrange
        when(localRepository.findByTelefono("5491112345678")).thenReturn(Optional.of(local));
        when(categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true))
            .thenReturn(Arrays.asList(categoriaHamburguesas, categoriaBebidas));
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaHamburguesas))
            .thenReturn(Arrays.asList(hamburguesa1, hamburguesa2));
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaBebidas))
            .thenReturn(Collections.singletonList(bebida1));
        when(productoMapper.toDTO(hamburguesa1)).thenReturn(hamburguesaDTO1);
        when(productoMapper.toDTO(hamburguesa2)).thenReturn(hamburguesaDTO2);
        when(productoMapper.toDTO(bebida1)).thenReturn(bebidaDTO1);

        // Act
        MenuCompletoDTO resultado = catalogoService.obtenerCatalogoCompleto("5491112345678");

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getLocal()).isNotNull();
        assertThat(resultado.getLocal().getNombre()).isEqualTo("Dio Burger Palermo");
        assertThat(resultado.getLocal().getTelefono()).isEqualTo("5491112345678");
        
        assertThat(resultado.getCategorias()).hasSize(2);
        assertThat(resultado.getCategorias().get(0).getNombre()).isEqualTo("Hamburguesas");
        assertThat(resultado.getCategorias().get(0).getProductos()).hasSize(2);
        assertThat(resultado.getCategorias().get(1).getNombre()).isEqualTo("Bebidas");
        assertThat(resultado.getCategorias().get(1).getProductos()).hasSize(1);

        assertThat(resultado.getModalidadesPermitidas()).containsExactlyInAnyOrder("DELIVERY", "RETIRAR");
        assertThat(resultado.getPermiteReservas()).isTrue();
        assertThat(resultado.getHorariosSugeridos()).isNotEmpty();
        assertThat(resultado.getConfiguracion()).isNotNull();
        assertThat(resultado.getConfiguracion().getHoraApertura()).isEqualTo("11:00");
        assertThat(resultado.getConfiguracion().getHoraCierre()).isEqualTo("23:00");

        verify(localRepository).findByTelefono("5491112345678");
        verify(categoriaRepository).findByLocalAndActivoOrderByOrdenAsc(local, true);
        verify(productoRepository, times(2)).findByCategoriaAndEstaAgotadoFalse(any(Categoria.class));
        verify(productoMapper, times(3)).toDTO(any(Producto.class));
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - Local no existe - Lanza NotFoundException")
    void testObtenerCatalogoCompleto_LocalNoExiste_LanzaNotFoundException() {
        // Arrange
        when(localRepository.findByTelefono("5491199999999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> catalogoService.obtenerCatalogoCompleto("5491199999999"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Local no encontrado con teléfono: 5491199999999");

        verify(localRepository).findByTelefono("5491199999999");
        verifyNoInteractions(categoriaRepository, productoRepository, productoMapper);
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - Local sin configuración - Lanza NotFoundException")
    void testObtenerCatalogoCompleto_LocalSinConfiguracion_LanzaNotFoundException() {
        // Arrange
        local.setConfiguracion(null);
        when(localRepository.findByTelefono("5491112345678")).thenReturn(Optional.of(local));

        // Act & Assert
        assertThatThrownBy(() -> catalogoService.obtenerCatalogoCompleto("5491112345678"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Configuración no encontrada para el local");

        verify(localRepository).findByTelefono("5491112345678");
        verifyNoInteractions(categoriaRepository, productoRepository, productoMapper);
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - Solo delivery permitido - Retorna solo DELIVERY")
    void testObtenerCatalogoCompleto_SoloDelivery_RetornaSoloDelivery() {
        // Arrange
        configuracion.setPermiteTakeAway(false); // Solo delivery
        when(localRepository.findByTelefono("5491112345678")).thenReturn(Optional.of(local));
        when(categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true))
            .thenReturn(Collections.singletonList(categoriaHamburguesas));
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaHamburguesas))
            .thenReturn(Collections.singletonList(hamburguesa1));
        when(productoMapper.toDTO(hamburguesa1)).thenReturn(hamburguesaDTO1);

        // Act
        MenuCompletoDTO resultado = catalogoService.obtenerCatalogoCompleto("5491112345678");

        // Assert
        assertThat(resultado.getModalidadesPermitidas()).containsExactly("DELIVERY");
        assertThat(resultado.getModalidadesPermitidas()).doesNotContain("RETIRAR");
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - Solo retirar permitido - Retorna solo RETIRAR")
    void testObtenerCatalogoCompleto_SoloRetirar_RetornaSoloRetirar() {
        // Arrange
        configuracion.setPermiteDelivery(false); // Solo retirar
        when(localRepository.findByTelefono("5491112345678")).thenReturn(Optional.of(local));
        when(categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true))
            .thenReturn(Collections.singletonList(categoriaHamburguesas));
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaHamburguesas))
            .thenReturn(Collections.singletonList(hamburguesa1));
        when(productoMapper.toDTO(hamburguesa1)).thenReturn(hamburguesaDTO1);

        // Act
        MenuCompletoDTO resultado = catalogoService.obtenerCatalogoCompleto("5491112345678");

        // Assert
        assertThat(resultado.getModalidadesPermitidas()).containsExactly("RETIRAR");
        assertThat(resultado.getModalidadesPermitidas()).doesNotContain("DELIVERY");
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - Ninguna modalidad permitida - Retorna lista vacía")
    void testObtenerCatalogoCompleto_NingunaModalidad_RetornaListaVacia() {
        // Arrange
        configuracion.setPermiteDelivery(false);
        configuracion.setPermiteTakeAway(false);
        when(localRepository.findByTelefono("5491112345678")).thenReturn(Optional.of(local));
        when(categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true))
            .thenReturn(Collections.singletonList(categoriaHamburguesas));
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaHamburguesas))
            .thenReturn(Collections.singletonList(hamburguesa1));
        when(productoMapper.toDTO(hamburguesa1)).thenReturn(hamburguesaDTO1);

        // Act
        MenuCompletoDTO resultado = catalogoService.obtenerCatalogoCompleto("5491112345678");

        // Assert
        assertThat(resultado.getModalidadesPermitidas()).isEmpty();
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - No permite reservas - Retorna false")
    void testObtenerCatalogoCompleto_NoPermiteReservas_RetornaFalse() {
        // Arrange
        configuracion.setPermiteReservas(false);
        when(localRepository.findByTelefono("5491112345678")).thenReturn(Optional.of(local));
        when(categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true))
            .thenReturn(Collections.singletonList(categoriaHamburguesas));
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaHamburguesas))
            .thenReturn(Collections.singletonList(hamburguesa1));
        when(productoMapper.toDTO(hamburguesa1)).thenReturn(hamburguesaDTO1);

        // Act
        MenuCompletoDTO resultado = catalogoService.obtenerCatalogoCompleto("5491112345678");

        // Assert
        assertThat(resultado.getPermiteReservas()).isFalse();
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - Sin categorías - Retorna catálogo vacío")
    void testObtenerCatalogoCompleto_SinCategorias_RetornaCatalogoVacio() {
        // Arrange
        when(localRepository.findByTelefono("5491112345678")).thenReturn(Optional.of(local));
        when(categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true))
            .thenReturn(Collections.emptyList());

        // Act
        MenuCompletoDTO resultado = catalogoService.obtenerCatalogoCompleto("5491112345678");

        // Assert
        assertThat(resultado.getCategorias()).isEmpty();
        verify(productoRepository, never()).findByCategoriaAndEstaAgotadoFalse(any(Categoria.class));
        verify(productoMapper, never()).toDTO(any(Producto.class));
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - Categoría sin productos - No incluye categoría vacía")
    void testObtenerCatalogoCompleto_CategoriaSinProductos_NoIncluyeCategoria() {
        // Arrange
        when(localRepository.findByTelefono("5491112345678")).thenReturn(Optional.of(local));
        when(categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true))
            .thenReturn(Arrays.asList(categoriaHamburguesas, categoriaBebidas));
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaHamburguesas))
            .thenReturn(Collections.singletonList(hamburguesa1));
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaBebidas))
            .thenReturn(Collections.emptyList()); // Sin bebidas
        when(productoMapper.toDTO(hamburguesa1)).thenReturn(hamburguesaDTO1);

        // Act
        MenuCompletoDTO resultado = catalogoService.obtenerCatalogoCompleto("5491112345678");

        // Assert
        assertThat(resultado.getCategorias()).hasSize(1); // Solo hamburguesas
        assertThat(resultado.getCategorias().get(0).getNombre()).isEqualTo("Hamburguesas");
        verify(productoMapper, times(1)).toDTO(any(Producto.class));
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - Horarios sugeridos calculados correctamente - Retorna horarios cada 30 min")
    void testObtenerCatalogoCompleto_HorariosSugeridos_CalculaCorrectamente() {
        // Arrange
        configuracion.setHoraApertura(LocalTime.of(12, 0));
        configuracion.setHoraCierre(LocalTime.of(14, 0));
        configuracion.setIntervaloMinutosPedidos(30);
        when(localRepository.findByTelefono("5491112345678")).thenReturn(Optional.of(local));
        when(categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true))
            .thenReturn(Collections.singletonList(categoriaHamburguesas));
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaHamburguesas))
            .thenReturn(Collections.singletonList(hamburguesa1));
        when(productoMapper.toDTO(hamburguesa1)).thenReturn(hamburguesaDTO1);

        // Act
        MenuCompletoDTO resultado = catalogoService.obtenerCatalogoCompleto("5491112345678");

        // Assert
        assertThat(resultado.getHorariosSugeridos())
            .containsExactly("12:00", "12:30", "13:00", "13:30", "14:00");
    }

    @Test
    @DisplayName("obtenerCatalogoCompleto - Categorías ordenadas por campo orden - Retorna en orden correcto")
    void testObtenerCatalogoCompleto_CategoriasOrdenadas_RetornaEnOrden() {
        // Arrange
        categoriaHamburguesas.setOrden(2); // Cambiar orden
        categoriaBebidas.setOrden(1);
        
        when(localRepository.findByTelefono("5491112345678")).thenReturn(Optional.of(local));
        when(categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true))
            .thenReturn(Arrays.asList(categoriaBebidas, categoriaHamburguesas)); // Orden invertido
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaBebidas))
            .thenReturn(Collections.singletonList(bebida1));
        when(productoRepository.findByCategoriaAndEstaAgotadoFalse(categoriaHamburguesas))
            .thenReturn(Collections.singletonList(hamburguesa1));
        when(productoMapper.toDTO(bebida1)).thenReturn(bebidaDTO1);
        when(productoMapper.toDTO(hamburguesa1)).thenReturn(hamburguesaDTO1);

        // Act
        MenuCompletoDTO resultado = catalogoService.obtenerCatalogoCompleto("5491112345678");

        // Assert
        assertThat(resultado.getCategorias()).hasSize(2);
        assertThat(resultado.getCategorias().get(0).getNombre()).isEqualTo("Bebidas");
        assertThat(resultado.getCategorias().get(0).getOrden()).isEqualTo(1);
        assertThat(resultado.getCategorias().get(1).getNombre()).isEqualTo("Hamburguesas");
        assertThat(resultado.getCategorias().get(1).getOrden()).isEqualTo(2);
    }
}
