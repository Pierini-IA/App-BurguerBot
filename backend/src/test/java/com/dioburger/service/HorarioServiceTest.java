package com.dioburger.service;

import com.dioburger.exception.NotFoundException;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests para HorarioService.
 * Valida la generación de horarios disponibles para pedidos y reservas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de HorarioService")
class HorarioServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private LocalService localService;

    @InjectMocks
    private HorarioService horarioService;

    private Local local;
    private ConfiguracionLocal configuracion;

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
            .horaApertura(LocalTime.of(12, 0))
            .horaCierre(LocalTime.of(14, 0))
            .intervaloMinutosPedidos(30)
            .maxPedidosPorIntervalo(5)
            .horaAperturaReservas(LocalTime.of(19, 0))
            .horaCierreReservas(LocalTime.of(23, 0))
            .intervaloMinutosReservas(60)
            .permiteReservas(true)
            .build();

        local.setConfiguracion(configuracion);
    }

    @Test
    @DisplayName("getHorariosSugeridosPedidos - Todos los slots disponibles - Retorna todos los horarios")
    void testGetHorariosSugeridosPedidos_TodosDisponibles_RetornaTodos() {
        // Arrange
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);
        when(pedidoRepository.countPedidosEnIntervalo(any(Local.class), any(), any())).thenReturn(0L);

        // Act
        List<String> horarios = horarioService.getHorariosSugeridosPedidos("5491112345678");

        // Assert
        assertThat(horarios).isNotEmpty();
        assertThat(horarios).contains("12:00", "12:30", "13:00", "13:30");
        assertThat(horarios).hasSize(4); // 12:00, 12:30, 13:00, 13:30 (14:00 es cierre)

        verify(localService).buscarPorTelefono("5491112345678");
        verify(pedidoRepository, times(4)).countPedidosEnIntervalo(any(Local.class), any(), any());
    }

    @Test
    @DisplayName("getHorariosSugeridosPedidos - Algunos slots llenos - Filtra slots llenos")
    void testGetHorariosSugeridosPedidos_AlgunosLlenos_FiltraLlenos() {
        // Arrange
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);
        // Primer slot lleno (5 pedidos), resto vacío
        when(pedidoRepository.countPedidosEnIntervalo(any(Local.class), any(), any()))
            .thenReturn(5L) // 12:00 lleno
            .thenReturn(0L) // 12:30 disponible
            .thenReturn(0L) // 13:00 disponible
            .thenReturn(0L); // 13:30 disponible

        // Act
        List<String> horarios = horarioService.getHorariosSugeridosPedidos("5491112345678");

        // Assert
        assertThat(horarios).hasSize(3);
        assertThat(horarios).doesNotContain("12:00");
        assertThat(horarios).contains("12:30", "13:00", "13:30");
    }

    @Test
    @DisplayName("getHorariosSugeridosPedidos - Todos los slots llenos - Retorna lista vacía")
    void testGetHorariosSugeridosPedidos_TodosLlenos_RetornaVacia() {
        // Arrange
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);
        when(pedidoRepository.countPedidosEnIntervalo(any(Local.class), any(), any())).thenReturn(5L);

        // Act
        List<String> horarios = horarioService.getHorariosSugeridosPedidos("5491112345678");

        // Assert
        assertThat(horarios).isEmpty();
    }

    @Test
    @DisplayName("getHorariosSugeridosPedidos - Local sin configuración - Lanza NotFoundException")
    void testGetHorariosSugeridosPedidos_SinConfiguracion_LanzaException() {
        // Arrange
        local.setConfiguracion(null);
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);

        // Act & Assert
        assertThatThrownBy(() -> horarioService.getHorariosSugeridosPedidos("5491112345678"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("no tiene configuración de horarios");

        verify(localService).buscarPorTelefono("5491112345678");
        verifyNoInteractions(pedidoRepository);
    }

    @Test
    @DisplayName("getHorariosSugeridosPedidos - Local no existe - Lanza NotFoundException")
    void testGetHorariosSugeridosPedidos_LocalNoExiste_LanzaException() {
        // Arrange
        when(localService.buscarPorTelefono("5491199999999"))
            .thenThrow(new NotFoundException("Local no encontrado"));

        // Act & Assert
        assertThatThrownBy(() -> horarioService.getHorariosSugeridosPedidos("5491199999999"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Local no encontrado");

        verify(localService).buscarPorTelefono("5491199999999");
        verifyNoInteractions(pedidoRepository);
    }

    @Test
    @DisplayName("getHorariosSugeridosPedidos - Intervalo de 15 minutos - Genera más slots")
    void testGetHorariosSugeridosPedidos_Intervalo15Min_GeneraMasSlots() {
        // Arrange
        configuracion.setIntervaloMinutosPedidos(15);
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);
        when(pedidoRepository.countPedidosEnIntervalo(any(Local.class), any(), any())).thenReturn(0L);

        // Act
        List<String> horarios = horarioService.getHorariosSugeridosPedidos("5491112345678");

        // Assert
        assertThat(horarios).hasSize(8); // 12:00 a 13:45 cada 15 min
        assertThat(horarios).contains("12:00", "12:15", "12:30", "12:45", "13:00", "13:15", "13:30", "13:45");
    }

    @Test
    @DisplayName("getHorariosSugeridosPedidos - Slot casi lleno - Incluye slot")
    void testGetHorariosSugeridosPedidos_SlotCasiLleno_IncluyeSlot() {
        // Arrange
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);
        when(pedidoRepository.countPedidosEnIntervalo(any(Local.class), any(), any()))
            .thenReturn(4L); // 4 de 5 (casi lleno pero disponible)

        // Act
        List<String> horarios = horarioService.getHorariosSugeridosPedidos("5491112345678");

        // Assert
        assertThat(horarios).hasSize(4); // Todos disponibles
        assertThat(horarios).contains("12:00", "12:30", "13:00", "13:30");
    }

    @Test
    @DisplayName("getHorariosSugeridosReservas - Permite reservas - Retorna horarios de reservas")
    void testGetHorariosSugeridosReservas_PermiteReservas_RetornaHorarios() {
        // Arrange
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);

        // Act
        List<String> horarios = horarioService.getHorariosSugeridosReservas("5491112345678");

        // Assert
        assertThat(horarios).isNotEmpty();
        assertThat(horarios).hasSize(4); // 19:00, 20:00, 21:00, 22:00 (23:00 es cierre)
        assertThat(horarios).contains("19:00", "20:00", "21:00", "22:00");

        verify(localService).buscarPorTelefono("5491112345678");
    }

    @Test
    @DisplayName("getHorariosSugeridosReservas - No permite reservas - Retorna lista vacía")
    void testGetHorariosSugeridosReservas_NoPermiteReservas_RetornaVacia() {
        // Arrange
        configuracion.setPermiteReservas(false);
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);

        // Act
        List<String> horarios = horarioService.getHorariosSugeridosReservas("5491112345678");

        // Assert
        assertThat(horarios).isEmpty();

        verify(localService).buscarPorTelefono("5491112345678");
    }

    @Test
    @DisplayName("getHorariosSugeridosReservas - Local sin configuración - Retorna lista vacía")
    void testGetHorariosSugeridosReservas_SinConfiguracion_RetornaVacia() {
        // Arrange
        local.setConfiguracion(null);
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);

        // Act
        List<String> horarios = horarioService.getHorariosSugeridosReservas("5491112345678");

        // Assert
        assertThat(horarios).isEmpty();

        verify(localService).buscarPorTelefono("5491112345678");
    }

    @Test
    @DisplayName("getHorariosSugeridosReservas - Intervalo de 30 minutos - Genera más slots")
    void testGetHorariosSugeridosReservas_Intervalo30Min_GeneraMasSlots() {
        // Arrange
        configuracion.setHoraAperturaReservas(LocalTime.of(18, 0));
        configuracion.setHoraCierreReservas(LocalTime.of(20, 0));
        configuracion.setIntervaloMinutosReservas(30);
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);

        // Act
        List<String> horarios = horarioService.getHorariosSugeridosReservas("5491112345678");

        // Assert
        assertThat(horarios).hasSize(4); // 18:00, 18:30, 19:00, 19:30
        assertThat(horarios).contains("18:00", "18:30", "19:00", "19:30");
    }

    @Test
    @DisplayName("getHorariosSugeridosPedidos - Horario amplio 10hs - Genera muchos slots")
    void testGetHorariosSugeridosPedidos_HorarioAmplio_GeneraMuchosSlots() {
        // Arrange
        configuracion.setHoraApertura(LocalTime.of(11, 0));
        configuracion.setHoraCierre(LocalTime.of(23, 0));
        configuracion.setIntervaloMinutosPedidos(60);
        when(localService.buscarPorTelefono("5491112345678")).thenReturn(local);
        when(pedidoRepository.countPedidosEnIntervalo(any(Local.class), any(), any())).thenReturn(0L);

        // Act
        List<String> horarios = horarioService.getHorariosSugeridosPedidos("5491112345678");

        // Assert
        assertThat(horarios).hasSize(12); // 11:00 a 22:00 cada 1 hora
        assertThat(horarios).contains("11:00", "12:00", "13:00", "20:00", "21:00", "22:00");
    }
}
