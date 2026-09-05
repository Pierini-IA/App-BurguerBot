package com.dioburger.mapper;

import com.dioburger.model.dto.PedidoResponseDTO;
import com.dioburger.model.entity.*;
import com.dioburger.model.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para PedidoMapper.
 * Valida la conversión correcta de entidades Pedido a DTOs.
 */
@SpringBootTest
@DisplayName("Tests de PedidoMapper")
class PedidoMapperTest {

    @Autowired
    private PedidoMapper pedidoMapper;

    @Test
    @DisplayName("toResponseDTO - Convierte pedido DELIVERY completo correctamente")
    void testToResponseDTO_PedidoDeliveryCompleto() {
        // Arrange
        Local local = Local.builder()
                .id(1L)
                .nombre("Sucursal Centro")
                .build();

        Cliente cliente = Cliente.builder()
                .id(10L)
                .nombre("Juan Pérez")
                .telefono("5491187654321")
                .build();

        Pedido pedido = Pedido.builder()
                .id(100L)
                .local(local)
                .cliente(cliente)
                .modalidad(Modalidad.DELIVERY)
                .estado(EstadoPedido.CONFIRMADO)
                .medioPago(MedioPago.EFECTIVO)
                .estadoPago(EstadoPago.PENDIENTE)
                .origenPedido(OrigenPedido.BOT)
                .total(new BigDecimal("2500.50"))
                .direccionEnvio("Av. Rivadavia 1234, CABA")
                .horaPedido(LocalDateTime.of(2025, 10, 22, 19, 30))
                .requestId("REQ-12345")
                .build();

        // Act
        PedidoResponseDTO dto = pedidoMapper.toResponseDTO(pedido);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getRequestId()).isEqualTo("REQ-12345");
        assertThat(dto.getEstado()).isEqualTo("CONFIRMADO");
        assertThat(dto.getModalidad()).isEqualTo("DELIVERY");
        assertThat(dto.getMedioPago()).isEqualTo("EFECTIVO");
        assertThat(dto.getEstadoPago()).isEqualTo("PENDIENTE");
        assertThat(dto.getTotal()).isEqualByComparingTo(new BigDecimal("2500.50"));
        assertThat(dto.getHoraPedido()).isEqualTo(LocalDateTime.of(2025, 10, 22, 19, 30));
    }

    @Test
    @DisplayName("toResponseDTO - Convierte enums correctamente a Strings")
    void testToResponseDTO_ConversionEnums() {
        // Arrange
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .telefono("123456789")
                .build();

        Pedido pedido = Pedido.builder()
                .id(1L)
                .cliente(cliente)
                .modalidad(Modalidad.RETIRAR)
                .estado(EstadoPedido.EN_PREPARACION)
                .medioPago(MedioPago.TARJETA_CREDITO)
                .estadoPago(EstadoPago.PAGADO)
                .origenPedido(OrigenPedido.LOCAL)
                .total(BigDecimal.valueOf(1000))
                .horaPedido(LocalDateTime.now())
                .requestId("TEST")
                .build();

        // Act
        PedidoResponseDTO dto = pedidoMapper.toResponseDTO(pedido);

        // Assert
        assertThat(dto.getEstado()).isEqualTo("EN_PREPARACION");
        assertThat(dto.getModalidad()).isEqualTo("RETIRAR");
        assertThat(dto.getMedioPago()).isEqualTo("TARJETA_CREDITO");
        assertThat(dto.getEstadoPago()).isEqualTo("PAGADO");
    }

    @Test
    @DisplayName("toResponseDTO - Prueba todos los estados de pedido")
    void testToResponseDTO_TodosLosEstados() {
        // Arrange & Act & Assert
        for (EstadoPedido estado : EstadoPedido.values()) {
            Cliente cliente = Cliente.builder()
                    .id(1L)
                    .nombre("Test")
                    .telefono("123456789")
                    .build();

            Pedido pedido = Pedido.builder()
                    .id(1L)
                    .cliente(cliente)
                    .modalidad(Modalidad.DELIVERY)
                    .estado(estado)
                    .medioPago(MedioPago.EFECTIVO)
                    .estadoPago(EstadoPago.PENDIENTE)
                    .origenPedido(OrigenPedido.BOT)
                    .total(BigDecimal.ZERO)
                    .horaPedido(LocalDateTime.now())
                    .requestId("TEST")
                    .build();

            PedidoResponseDTO dto = pedidoMapper.toResponseDTO(pedido);

            assertThat(dto.getEstado()).isEqualTo(estado.name());
        }
    }

    @Test
    @DisplayName("toResponseDTO - Prueba todas las modalidades")
    void testToResponseDTO_TodasLasModalidades() {
        // Arrange & Act & Assert
        for (Modalidad modalidad : Modalidad.values()) {
            Cliente cliente = Cliente.builder()
                    .id(1L)
                    .nombre("Test")
                    .telefono("123456789")
                    .build();

            Pedido pedido = Pedido.builder()
                    .id(1L)
                    .cliente(cliente)
                    .modalidad(modalidad)
                    .estado(EstadoPedido.PENDIENTE)
                    .medioPago(MedioPago.EFECTIVO)
                    .estadoPago(EstadoPago.PENDIENTE)
                    .origenPedido(OrigenPedido.BOT)
                    .total(BigDecimal.ZERO)
                    .horaPedido(LocalDateTime.now())
                    .requestId("TEST")
                    .build();

            PedidoResponseDTO dto = pedidoMapper.toResponseDTO(pedido);

            assertThat(dto.getModalidad()).isEqualTo(modalidad.name());
        }
    }

    @Test
    @DisplayName("toResponseDTO - Prueba todos los medios de pago")
    void testToResponseDTO_TodosLosMediosPago() {
        // Arrange & Act & Assert
        for (MedioPago medioPago : MedioPago.values()) {
            Cliente cliente = Cliente.builder()
                    .id(1L)
                    .nombre("Test")
                    .telefono("123456789")
                    .build();

            Pedido pedido = Pedido.builder()
                    .id(1L)
                    .cliente(cliente)
                    .modalidad(Modalidad.DELIVERY)
                    .estado(EstadoPedido.PENDIENTE)
                    .medioPago(medioPago)
                    .estadoPago(EstadoPago.PENDIENTE)
                    .origenPedido(OrigenPedido.BOT)
                    .total(BigDecimal.ZERO)
                    .horaPedido(LocalDateTime.now())
                    .requestId("TEST")
                    .build();

            PedidoResponseDTO dto = pedidoMapper.toResponseDTO(pedido);

            assertThat(dto.getMedioPago()).isEqualTo(medioPago.name());
        }
    }

    @Test
    @DisplayName("toResponseDTO - Prueba todos los estados de pago")
    void testToResponseDTO_TodosLosEstadosPago() {
        // Arrange & Act & Assert
        for (EstadoPago estadoPago : EstadoPago.values()) {
            Cliente cliente = Cliente.builder()
                    .id(1L)
                    .nombre("Test")
                    .telefono("123456789")
                    .build();

            Pedido pedido = Pedido.builder()
                    .id(1L)
                    .cliente(cliente)
                    .modalidad(Modalidad.DELIVERY)
                    .estado(EstadoPedido.PENDIENTE)
                    .medioPago(MedioPago.EFECTIVO)
                    .estadoPago(estadoPago)
                    .origenPedido(OrigenPedido.BOT)
                    .total(BigDecimal.ZERO)
                    .horaPedido(LocalDateTime.now())
                    .requestId("TEST")
                    .build();

            PedidoResponseDTO dto = pedidoMapper.toResponseDTO(pedido);

            assertThat(dto.getEstadoPago()).isEqualTo(estadoPago.name());
        }
    }

    @Test
    @DisplayName("toResponseDTO - Total con decimales se preserva")
    void testToResponseDTO_TotalConDecimales() {
        // Arrange
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .telefono("123456789")
                .build();

        Pedido pedido = Pedido.builder()
                .id(1L)
                .cliente(cliente)
                .modalidad(Modalidad.DELIVERY)
                .estado(EstadoPedido.PENDIENTE)
                .medioPago(MedioPago.EFECTIVO)
                .estadoPago(EstadoPago.PENDIENTE)
                .origenPedido(OrigenPedido.BOT)
                .total(new BigDecimal("1234.56"))
                .horaPedido(LocalDateTime.now())
                .requestId("TEST")
                .build();

        // Act
        PedidoResponseDTO dto = pedidoMapper.toResponseDTO(pedido);

        // Assert
        assertThat(dto.getTotal()).isEqualByComparingTo(new BigDecimal("1234.56"));
    }
}
