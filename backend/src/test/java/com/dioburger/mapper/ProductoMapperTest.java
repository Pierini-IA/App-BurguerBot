package com.dioburger.mapper;

import com.dioburger.model.dto.ProductoDTO;
import com.dioburger.model.entity.*;
import com.dioburger.model.enums.TipoProducto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para ProductoMapper.
 * Valida la conversión correcta de entidades Producto a DTOs.
 * Se enfoca en los métodos custom del mapper: invertirAgotado, mapExtras, mapPromocion.
 */
@SpringBootTest
@DisplayName("Tests de ProductoMapper")
class ProductoMapperTest {

    @Autowired
    private ProductoMapper productoMapper;

    @Test
    @DisplayName("toDTO - Convierte producto básico correctamente")
    void testToDTO_ProductoBasico() {
        // Arrange
        Local local = Local.builder()
                .id(1L)
                .nombre("Sucursal Centro")
                .build();

        Categoria categoria = Categoria.builder()
                .id(1L)
                .nombre("Hamburguesas")
                .build();

        Producto producto = Producto.builder()
                .id(10L)
                .nombre("Clásica")
                .descripcion("Hamburguesa clásica con queso")
                .precio(new BigDecimal("1500.00"))
                .estaAgotado(false)
                .tipoProducto(TipoProducto.CON_RECETA)
                .local(local)
                .categoria(categoria)
                .build();

        // Act
        ProductoDTO dto = productoMapper.toDTO(producto);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getNombre()).isEqualTo("Clásica");
        assertThat(dto.getDescripcion()).isEqualTo("Hamburguesa clásica con queso");
        assertThat(dto.getPrecio()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(dto.getDisponible()).isTrue(); // estaAgotado=false → disponible=true
        assertThat(dto.getTipoProducto()).isEqualTo(TipoProducto.CON_RECETA);
        assertThat(dto.getLocalId()).isEqualTo(1L);
        assertThat(dto.getLocalNombre()).isEqualTo("Sucursal Centro");
        assertThat(dto.getCategoriaId()).isEqualTo(1L);
        assertThat(dto.getCategoriaNombre()).isEqualTo("Hamburguesas");
    }

    @Test
    @DisplayName("invertirAgotado - Producto disponible (estaAgotado=false)")
    void testInvertirAgotado_Disponible() {
        // Arrange
        Producto producto = Producto.builder()
                .estaAgotado(false)
                .build();

        // Act
        ProductoDTO dto = productoMapper.toDTO(producto);

        // Assert
        assertThat(dto.getDisponible()).isTrue();
    }

    @Test
    @DisplayName("invertirAgotado - Producto agotado (estaAgotado=true)")
    void testInvertirAgotado_Agotado() {
        // Arrange
        Producto producto = Producto.builder()
                .estaAgotado(true)
                .build();

        // Act
        ProductoDTO dto = productoMapper.toDTO(producto);

        // Assert
        assertThat(dto.getDisponible()).isFalse();
    }

    @Test
    @DisplayName("invertirAgotado - Estado null (default a disponible=true)")
    void testInvertirAgotado_Null() {
        // Arrange
        Producto producto = Producto.builder()
                .estaAgotado(null)
                .build();

        // Act
        ProductoDTO dto = productoMapper.toDTO(producto);

        // Assert
        assertThat(dto.getDisponible()).isTrue();
    }

    @Test
    @DisplayName("mapPromocion - Producto sin promoción")
    void testMapPromocion_SinPromocion() {
        // Arrange
        Producto producto = Producto.builder()
                .tienePromocion(false)
                .build();

        // Act
        ProductoDTO dto = productoMapper.toDTO(producto);

        // Assert
        assertThat(dto.getPromocion()).isNull();
    }

    @Test
    @DisplayName("mapPromocion - tienePromocion null")
    void testMapPromocion_TienePromocionNull() {
        // Arrange
        Producto producto = Producto.builder()
                .tienePromocion(null)
                .build();

        // Act
        ProductoDTO dto = productoMapper.toDTO(producto);

        // Assert
        assertThat(dto.getPromocion()).isNull();
    }

    @Test
    @DisplayName("mapPromocion - Producto con promoción activa")
    void testMapPromocion_ConPromocion() {
        // Arrange
        Producto producto = Producto.builder()
                .tienePromocion(true)
                .precioPromocion(new BigDecimal("1200"))
                .precioBase(new BigDecimal("1500"))
                .build();

        // Act
        ProductoDTO dto = productoMapper.toDTO(producto);

        // Assert
        assertThat(dto.getPromocion()).isNotNull();
    }

    @Test
    @DisplayName("toDTO - Producto sin local ni categoría")
    void testToDTO_SinLocalNiCategoria() {
        // Arrange
        Producto producto = Producto.builder()
                .id(1L)
                .nombre("Test")
                .precio(BigDecimal.ZERO)
                .local(null)
                .categoria(null)
                .build();

        // Act
        ProductoDTO dto = productoMapper.toDTO(producto);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getLocalId()).isNull();
        assertThat(dto.getCategoriaId()).isNull();
    }

    @Test
    @DisplayName("toDTO - Precio con decimales se preserva")
    void testToDTO_PrecioDecimal() {
        // Arrange
        Producto producto = Producto.builder()
                .precio(new BigDecimal("1234.56"))
                .build();

        // Act
        ProductoDTO dto = productoMapper.toDTO(producto);

        // Assert
        assertThat(dto.getPrecio()).isEqualByComparingTo(new BigDecimal("1234.56"));
    }

    @Test
    @DisplayName("toDTO - Mapea todos los tipos de producto")
    void testToDTO_TodosLosTiposProducto() {
        // Arrange & Act & Assert
        for (TipoProducto tipo : TipoProducto.values()) {
            Producto producto = Producto.builder()
                    .tipoProducto(tipo)
                    .build();

            ProductoDTO dto = productoMapper.toDTO(producto);

            assertThat(dto.getTipoProducto()).isEqualTo(tipo);
        }
    }
}
