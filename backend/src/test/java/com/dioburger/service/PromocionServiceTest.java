package com.dioburger.service;

import com.dioburger.model.entity.Producto;
import com.dioburger.model.enums.TipoProducto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests para PromocionService.
 * Valida el cálculo de precios dinámicos, validación de horarios y días de promoción.
 * 
 * Nota: Estos tests asumen que el horario y día actuales están en el rango de promoción.
 * Para producción, se recomienda usar Clock mockeable para controlar el tiempo.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PromocionService")
class PromocionServiceTest {

    @InjectMocks
    private PromocionService promocionService;

    private Producto productoSinPromocion;
    private Producto productoConPromocionActiva;
    private Producto productoConPromocionInactiva;

    @BeforeEach
    void setUp() {
        // Producto sin promoción
        productoSinPromocion = Producto.builder()
            .id(1L)
            .nombre("Hamburguesa Clásica")
            .precio(BigDecimal.valueOf(1500))
            .precioBase(BigDecimal.valueOf(1500))
            .tienePromocion(false)
            .tipoProducto(TipoProducto.CON_RECETA)
            .build();

        // Producto con promoción activa (horario amplio para que siempre esté activa en tests)
        productoConPromocionActiva = Producto.builder()
            .id(2L)
            .nombre("Hamburguesa Bacon")
            .precio(BigDecimal.valueOf(1800))
            .precioBase(BigDecimal.valueOf(2000))
            .precioPromocion(BigDecimal.valueOf(1600))
            .tienePromocion(true)
            .horaInicioPromo(LocalTime.of(0, 0)) // Siempre activa
            .horaFinPromo(LocalTime.of(23, 59))
            .diasPromocion(null) // Todos los días
            .tipoProducto(TipoProducto.CON_RECETA)
            .build();

        // Producto con promoción inactiva (horario nocturno para que esté inactiva en tests)
        productoConPromocionInactiva = Producto.builder()
            .id(3L)
            .nombre("Hamburguesa Especial")
            .precio(BigDecimal.valueOf(2200))
            .precioBase(BigDecimal.valueOf(2200))
            .precioPromocion(BigDecimal.valueOf(1800))
            .tienePromocion(true)
            .horaInicioPromo(LocalTime.of(2, 0)) // Horario nocturno
            .horaFinPromo(LocalTime.of(4, 0))
            .diasPromocion("[\"MONDAY\"]") // Solo lunes
            .tipoProducto(TipoProducto.CON_RECETA)
            .build();
    }

    @Test
    @DisplayName("calcularPrecioActual - Producto sin promoción - Retorna precio base")
    void testCalcularPrecioActual_SinPromocion_RetornaPrecioBase() {
        // Act
        BigDecimal precioActual = promocionService.calcularPrecioActual(productoSinPromocion);

        // Assert
        assertThat(precioActual).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    @DisplayName("calcularPrecioActual - Producto con promoción activa - Retorna precio promoción")
    void testCalcularPrecioActual_PromocionActiva_RetornaPrecioPromocion() {
        // Act
        BigDecimal precioActual = promocionService.calcularPrecioActual(productoConPromocionActiva);

        // Assert
        assertThat(precioActual).isEqualByComparingTo(BigDecimal.valueOf(1600));
    }

    @Test
    @DisplayName("calcularPrecioActual - tienePromocion null - Retorna precio base")
    void testCalcularPrecioActual_TienePromocionNull_RetornaPrecioBase() {
        // Arrange
        Producto producto = Producto.builder()
            .id(4L)
            .nombre("Producto Test")
            .precio(BigDecimal.valueOf(1000))
            .precioBase(BigDecimal.valueOf(1000))
            .tienePromocion(null) // Null
            .build();

        // Act
        BigDecimal precioActual = promocionService.calcularPrecioActual(producto);

        // Assert
        assertThat(precioActual).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("calcularPrecioActual - Producto sin precioBase - Retorna precio normal")
    void testCalcularPrecioActual_SinPrecioBase_RetornaPrecio() {
        // Arrange
        Producto producto = Producto.builder()
            .id(5L)
            .nombre("Producto Test")
            .precio(BigDecimal.valueOf(1200))
            .precioBase(null) // Sin precio base
            .tienePromocion(false)
            .build();

        // Act
        BigDecimal precioActual = promocionService.calcularPrecioActual(producto);

        // Assert
        assertThat(precioActual).isEqualByComparingTo(BigDecimal.valueOf(1200));
    }

    @Test
    @DisplayName("esPromocionActiva - Producto sin promoción - Retorna false")
    void testEsPromocionActiva_SinPromocion_RetornaFalse() {
        // Act
        boolean activa = promocionService.esPromocionActiva(productoSinPromocion);

        // Assert
        assertThat(activa).isFalse();
    }

    @Test
    @DisplayName("esPromocionActiva - Producto con promoción en horario válido - Retorna true")
    void testEsPromocionActiva_HorarioValido_RetornaTrue() {
        // Act
        boolean activa = promocionService.esPromocionActiva(productoConPromocionActiva);

        // Assert
        assertThat(activa).isTrue();
    }

    @Test
    @DisplayName("esPromocionActiva - tienePromocion null - Retorna false")
    void testEsPromocionActiva_TienePromocionNull_RetornaFalse() {
        // Arrange
        Producto producto = Producto.builder()
            .id(6L)
            .nombre("Producto Test")
            .tienePromocion(null)
            .build();

        // Act
        boolean activa = promocionService.esPromocionActiva(producto);

        // Assert
        assertThat(activa).isFalse();
    }

    @Test
    @DisplayName("esPromocionActiva - Promoción sin horarios configurados - Retorna true")
    void testEsPromocionActiva_SinHorarios_RetornaTrue() {
        // Arrange
        Producto producto = Producto.builder()
            .id(7L)
            .nombre("Producto 24/7")
            .tienePromocion(true)
            .horaInicioPromo(null) // Sin horarios = todo el día
            .horaFinPromo(null)
            .diasPromocion(null) // Todos los días
            .build();

        // Act
        boolean activa = promocionService.esPromocionActiva(producto);

        // Assert
        assertThat(activa).isTrue();
    }

    @Test
    @DisplayName("esPromocionActiva - Promoción sin días configurados - Retorna true")
    void testEsPromocionActiva_SinDias_RetornaTrue() {
        // Arrange
        Producto producto = Producto.builder()
            .id(8L)
            .nombre("Producto Test")
            .tienePromocion(true)
            .horaInicioPromo(LocalTime.of(0, 0))
            .horaFinPromo(LocalTime.of(23, 59))
            .diasPromocion(null) // Todos los días
            .build();

        // Act
        boolean activa = promocionService.esPromocionActiva(producto);

        // Assert
        assertThat(activa).isTrue();
    }

    @Test
    @DisplayName("esPromocionActiva - Promoción con días vacíos - Retorna true")
    void testEsPromocionActiva_DiasVacios_RetornaTrue() {
        // Arrange
        Producto producto = Producto.builder()
            .id(9L)
            .nombre("Producto Test")
            .tienePromocion(true)
            .horaInicioPromo(LocalTime.of(0, 0))
            .horaFinPromo(LocalTime.of(23, 59))
            .diasPromocion("") // String vacío
            .build();

        // Act
        boolean activa = promocionService.esPromocionActiva(producto);

        // Assert
        assertThat(activa).isTrue();
    }

    @Test
    @DisplayName("calcularPorcentajeDescuento - Con precios válidos - Calcula porcentaje correcto")
    void testCalcularPorcentajeDescuento_PreciosValidos_CalculaCorrectamente() {
        // Arrange
        Producto producto = Producto.builder()
            .precioBase(BigDecimal.valueOf(2000))
            .precioPromocion(BigDecimal.valueOf(1600))
            .build();

        // Act
        BigDecimal descuento = promocionService.calcularPorcentajeDescuento(producto);

        // Assert
        assertThat(descuento).isEqualByComparingTo(BigDecimal.valueOf(20.00)); // 20% descuento
    }

    @Test
    @DisplayName("calcularPorcentajeDescuento - Sin precioBase - Retorna cero")
    void testCalcularPorcentajeDescuento_SinPrecioBase_RetornaCero() {
        // Arrange
        Producto producto = Producto.builder()
            .precioBase(null)
            .precioPromocion(BigDecimal.valueOf(1600))
            .build();

        // Act
        BigDecimal descuento = promocionService.calcularPorcentajeDescuento(producto);

        // Assert
        assertThat(descuento).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calcularPorcentajeDescuento - Sin precioPromocion - Retorna cero")
    void testCalcularPorcentajeDescuento_SinPrecioPromocion_RetornaCero() {
        // Arrange
        Producto producto = Producto.builder()
            .precioBase(BigDecimal.valueOf(2000))
            .precioPromocion(null)
            .build();

        // Act
        BigDecimal descuento = promocionService.calcularPorcentajeDescuento(producto);

        // Assert
        assertThat(descuento).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calcularPorcentajeDescuento - Descuento 50% - Calcula correctamente")
    void testCalcularPorcentajeDescuento_Descuento50_CalculaCorrectamente() {
        // Arrange
        Producto producto = Producto.builder()
            .precioBase(BigDecimal.valueOf(1000))
            .precioPromocion(BigDecimal.valueOf(500))
            .build();

        // Act
        BigDecimal descuento = promocionService.calcularPorcentajeDescuento(producto);

        // Assert
        assertThat(descuento).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    @Test
    @DisplayName("obtenerInfoPromocion - Producto sin promoción - Retorna null")
    void testObtenerInfoPromocion_SinPromocion_RetornaNull() {
        // Arrange
        productoSinPromocion.setTienePromocion(false);

        // Act
        String info = promocionService.obtenerInfoPromocion(productoSinPromocion);

        // Assert
        assertThat(info).isNull();
    }

    @Test
    @DisplayName("obtenerInfoPromocion - Con promoción activa - Retorna información detallada")
    void testObtenerInfoPromocion_PromocionActiva_RetornaInfo() {
        // Act
        String info = promocionService.obtenerInfoPromocion(productoConPromocionActiva);

        // Assert
        assertThat(info).isNotNull();
        assertThat(info).contains("20% OFF"); // (2000-1600)/2000 = 20%
        assertThat(info).contains("00:00");
        assertThat(info).contains("23:59");
    }

    @Test
    @DisplayName("obtenerInfoPromocion - Sin horarios configurados - No incluye horarios")
    void testObtenerInfoPromocion_SinHorarios_NoIncluyeHorarios() {
        // Arrange
        Producto producto = Producto.builder()
            .precioBase(BigDecimal.valueOf(1000))
            .precioPromocion(BigDecimal.valueOf(750))
            .tienePromocion(true)
            .horaInicioPromo(null)
            .horaFinPromo(null)
            .build();

        // Act
        String info = promocionService.obtenerInfoPromocion(producto);

        // Assert
        assertThat(info).isNotNull();
        assertThat(info).contains("25% OFF");
        assertThat(info).doesNotContain("de");
        assertThat(info).doesNotContain("a");
    }

    @Test
    @DisplayName("calcularPorcentajeDescuento - Decimal con precisión - Redondea correctamente")
    void testCalcularPorcentajeDescuento_DecimalConPrecision_RedondeaCorrectamente() {
        // Arrange
        Producto producto = Producto.builder()
            .precioBase(BigDecimal.valueOf(1234.56))
            .precioPromocion(BigDecimal.valueOf(987.65))
            .build();

        // Act
        BigDecimal descuento = promocionService.calcularPorcentajeDescuento(producto);

        // Assert
        assertThat(descuento).isNotNull();
        assertThat(descuento.scale()).isEqualTo(2); // Dos decimales
        assertThat(descuento).isGreaterThan(BigDecimal.ZERO);
        assertThat(descuento).isLessThan(BigDecimal.valueOf(100));
    }
}
