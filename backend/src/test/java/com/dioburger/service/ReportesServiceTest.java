package com.dioburger.service;

import com.dioburger.model.dto.ReporteVentasDTO;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Pedido;
import com.dioburger.model.entity.PedidoItem;
import com.dioburger.model.entity.Producto;
import com.dioburger.model.enums.EstadoPedido;
import com.dioburger.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ReportesService")
class ReportesServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private LocalService localService;

    @InjectMocks
    private ReportesService reportesService;

    private Local local;

    @BeforeEach
    void setUp() {
        local = Local.builder()
            .id(1L)
            .nombre("Dio Burger Palermo")
            .telefono("5491112345678")
            .build();
    }

    @Test
    @DisplayName("obtenerVentasDiarias - Agrupa por fecha y calcula totales y promedios")
    void testObtenerVentasDiarias_AgrupaYCalcula() {
        // Arrange
        LocalDate dia1 = LocalDate.now().minusDays(1);
        LocalDate dia2 = LocalDate.now();

        Pedido p1 = Pedido.builder()
            .id(1L)
            .local(local)
            .horaPedido(dia1.atTime(12,0))
            .total(BigDecimal.valueOf(1000))
            .estado(EstadoPedido.ENTREGADO)
            .build();

        Pedido p2 = Pedido.builder()
            .id(2L)
            .local(local)
            .horaPedido(dia1.atTime(13,0))
            .total(BigDecimal.valueOf(500))
            .estado(EstadoPedido.ENTREGADO)
            .build();

        Pedido p3 = Pedido.builder()
            .id(3L)
            .local(local)
            .horaPedido(dia2.atTime(14,0))
            .total(BigDecimal.valueOf(2000))
            .estado(EstadoPedido.ENTREGADO)
            .build();

        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);
        when(pedidoRepository.findByLocalAndHoraPedidoBetween(eq(local), any(), any()))
            .thenReturn(List.of(p1, p2, p3));

        // Act
        List<ReporteVentasDTO> reportes = reportesService.obtenerVentasDiarias("5491112345678", dia1, dia2);

        // Assert
        assertThat(reportes).hasSize(2);

        ReporteVentasDTO r1 = reportes.get(0);
        assertThat(r1.getFecha()).isEqualTo(dia1);
        assertThat(r1.getCantidadPedidos()).isEqualTo(2);
        assertThat(r1.getTotalVentas()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(r1.getPromedioTicket()).isEqualByComparingTo(BigDecimal.valueOf(750.00));

        ReporteVentasDTO r2 = reportes.get(1);
        assertThat(r2.getFecha()).isEqualTo(dia2);
        assertThat(r2.getCantidadPedidos()).isEqualTo(1);
        assertThat(r2.getTotalVentas()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(r2.getPromedioTicket()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));

        verify(localService).buscarPorTelefono("5491112345678");
        verify(pedidoRepository).findByLocalAndHoraPedidoBetween(eq(local), any(), any());
    }

    @Test
    @DisplayName("obtenerProductosTopVentas - Devuelve top N productos con cantidades y totales")
    void testObtenerProductosTopVentas_TopNCorrecto() {
        // Arrange
        LocalDate inicio = LocalDate.now().minusDays(2);
        LocalDate fin = LocalDate.now();

        Producto prodA = Producto.builder().id(10L).nombre("A").precio(BigDecimal.valueOf(100)).build();
        Producto prodB = Producto.builder().id(20L).nombre("B").precio(BigDecimal.valueOf(200)).build();

        PedidoItem item1 = PedidoItem.builder().producto(prodA).cantidad(3).build();
        PedidoItem item2 = PedidoItem.builder().producto(prodB).cantidad(1).build();
        PedidoItem item3 = PedidoItem.builder().producto(prodA).cantidad(2).build();

        Pedido p = Pedido.builder()
            .id(1L)
            .local(local)
            .horaPedido(LocalDateTime.now())
            .total(BigDecimal.valueOf(500))
            .estado(EstadoPedido.ENTREGADO)
            .items(List.of(item1, item2, item3))
            .build();

        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);
        when(pedidoRepository.findByLocalAndHoraPedidoBetween(eq(local), any(), any()))
            .thenReturn(List.of(p));

    // Act
    List<Map<String, Object>> top = reportesService.obtenerProductosTopVentas("5491112345678", inicio, fin, 5);

    // Assert
        assertThat(top.get(0).get("producto")).isEqualTo("A");
        assertThat(top.get(0).get("cantidadVendida")).isEqualTo(5L);
        assertThat(top).hasSize(2);
        assertThat(top.get(0).get("producto")).isEqualTo("A");
        assertThat(top.get(0).get("cantidadVendida")).isEqualTo(5L);
        assertThat((java.math.BigDecimal) top.get(0).get("totalVentas")).isEqualByComparingTo(java.math.BigDecimal.valueOf(500));
        assertThat(top.get(1).get("producto")).isEqualTo("B");
        assertThat(top.get(1).get("cantidadVendida")).isEqualTo(1L);
        assertThat((java.math.BigDecimal) top.get(1).get("totalVentas")).isEqualByComparingTo(java.math.BigDecimal.valueOf(200));

        verify(localService).buscarPorTelefono("5491112345678");
        verify(pedidoRepository).findByLocalAndHoraPedidoBetween(eq(local), any(), any());

    }

    @Test
    @DisplayName("obtenerDashboard - Calcula KPIs correctamente")
    void testObtenerDashboard_CalculaKPIs() {
        // Arrange
        LocalDate inicio = LocalDate.now().minusDays(1);
        LocalDate fin = LocalDate.now();

        Pedido p1 = Pedido.builder()
            .id(1L)
            .local(local)
            .horaPedido(LocalDateTime.now())
            .total(java.math.BigDecimal.valueOf(1000))
            .estado(EstadoPedido.ENTREGADO)
            .build();

        Pedido p2 = Pedido.builder()
            .id(2L)
            .local(local)
            .horaPedido(LocalDateTime.now())
            .total(java.math.BigDecimal.valueOf(500))
            .estado(EstadoPedido.CANCELADO)
            .build();

        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);
        when(pedidoRepository.findByLocalAndHoraPedidoBetween(eq(local), any(), any()))
            .thenReturn(List.of(p1, p2));

        // Act
        Map<String, Object> dashboard = reportesService.obtenerDashboard("5491112345678", inicio, fin);

        // Assert
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.get("totalPedidos")).isEqualTo(1L);
        assertThat((java.math.BigDecimal) dashboard.get("totalVentas")).isEqualByComparingTo(java.math.BigDecimal.valueOf(1000));
        assertThat((java.math.BigDecimal) dashboard.get("promedioTicket")).isEqualByComparingTo(java.math.BigDecimal.valueOf(1000));
        assertThat(dashboard.get("pedidosCancelados")).isEqualTo(1L);

        verify(localService).buscarPorTelefono("5491112345678");
    }

    @Test
    @DisplayName("compararPeriodos - Calcula crecimiento y diferencia")
    void testCompararPeriodos_CalculaCrecimiento() {
        // Arrange: periodo1 tiene 2000 ventas, periodo2 tiene 1000
        LocalDate p1Inicio = LocalDate.now().minusDays(10);
        LocalDate p1Fin = LocalDate.now().minusDays(9);
        LocalDate p2Inicio = LocalDate.now().minusDays(5);
        LocalDate p2Fin = LocalDate.now().minusDays(4);

        Pedido pA = Pedido.builder()
            .id(1L)
            .local(local)
            .horaPedido(LocalDateTime.now())
            .total(java.math.BigDecimal.valueOf(2000))
            .estado(EstadoPedido.ENTREGADO)
            .build();

        Pedido pB = Pedido.builder()
            .id(2L)
            .local(local)
            .horaPedido(LocalDateTime.now())
            .total(java.math.BigDecimal.valueOf(1000))
            .estado(EstadoPedido.ENTREGADO)
            .build();

        // Mock obtenerDashboard calls via repository responses for each period
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);
        when(pedidoRepository.findByLocalAndHoraPedidoBetween(eq(local), any(), any()))
            .thenReturn(List.of(pA))
            .thenReturn(List.of(pB));

        // Act
        Map<String, Object> resultado = reportesService.compararPeriodos("5491112345678", p1Inicio, p1Fin, p2Inicio, p2Fin);

        // Assert
        assertThat(resultado).containsKeys("periodo1", "periodo2", "crecimientoVentas", "diferenciaAbsoluta");
        assertThat(resultado.get("crecimientoVentas")).isNotNull();
        assertThat(resultado.get("diferenciaAbsoluta")).isNotNull();
    }

}