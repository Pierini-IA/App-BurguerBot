package com.dioburger.mapper;

import com.dioburger.model.dto.MesaDTO;
import com.dioburger.model.dto.ReservaResponseDTO;
import com.dioburger.model.entity.*;
import com.dioburger.model.enums.EstadoReserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para ReservaMapper.
 * Valida la conversión correcta de entidades Reserva a DTOs.
 */
@SpringBootTest
@DisplayName("Tests de ReservaMapper")
class ReservaMapperTest {

    @Autowired
    private ReservaMapper reservaMapper;

    @Test
    @DisplayName("toResponseDTO - Convierte reserva completa correctamente")
    void testToResponseDTO_ReservaCompleta() {
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

        Mesa mesa1 = Mesa.builder()
                .id(1L)
                .numero(5)
                .capacidad(4)
                .disponible(true)
                .local(local)
                .build();

        Mesa mesa2 = Mesa.builder()
                .id(2L)
                .numero(6)
                .capacidad(2)
                .disponible(true)
                .local(local)
                .build();

        Set<Mesa> mesas = new HashSet<>();
        mesas.add(mesa1);
        mesas.add(mesa2);

        Reserva reserva = Reserva.builder()
                .id(100L)
                .cliente(cliente)
                .horaReserva(LocalDateTime.of(2025, 10, 25, 20, 30))
                .numeroPersonas(6)
                .estado(EstadoReserva.CONFIRMADA)
                .mesas(mesas)
                .observaciones("Celebración cumpleaños")
                .build();

        // Act
        ReservaResponseDTO dto = reservaMapper.toResponseDTO(reserva);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getHoraReserva()).isEqualTo(LocalDateTime.of(2025, 10, 25, 20, 30));
        assertThat(dto.getNumeroPersonas()).isEqualTo(6);
        assertThat(dto.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(dto.getObservaciones()).isEqualTo("Celebración cumpleaños");
        assertThat(dto.getMesas()).hasSize(2);
    }

    @Test
    @DisplayName("mesasToDTO - Convierte Set de mesas correctamente")
    void testMesasToDTO_SetDeMesas() {
        // Arrange
        Local local = Local.builder()
                .id(1L)
                .nombre("Test")
                .build();

        Mesa mesa1 = Mesa.builder()
                .id(1L)
                .numero(1)
                .capacidad(2)
                .disponible(true)
                .local(local)
                .build();

        Mesa mesa2 = Mesa.builder()
                .id(2L)
                .numero(2)
                .capacidad(4)
                .disponible(false)
                .local(local)
                .build();

        Set<Mesa> mesas = Set.of(mesa1, mesa2);

        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .telefono("123456789")
                .build();

        Reserva reserva = Reserva.builder()
                .id(1L)
                .cliente(cliente)
                .horaReserva(LocalDateTime.now())
                .numeroPersonas(4)
                .estado(EstadoReserva.CONFIRMADA)
                .mesas(mesas)
                .build();

        // Act
        ReservaResponseDTO dto = reservaMapper.toResponseDTO(reserva);

        // Assert
        assertThat(dto.getMesas()).hasSize(2);
        assertThat(dto.getMesas()).extracting(MesaDTO::getId).containsExactlyInAnyOrder(1L, 2L);
        assertThat(dto.getMesas()).extracting(MesaDTO::getNumero).containsExactlyInAnyOrder(1, 2);
        assertThat(dto.getMesas()).extracting(MesaDTO::getCapacidad).containsExactlyInAnyOrder(2, 4);
    }

    @Test
    @DisplayName("mesasToDTO - Set vacío retorna Set vacío")
    void testMesasToDTO_SetVacio() {
        // Arrange
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .telefono("123456789")
                .build();

        Reserva reserva = Reserva.builder()
                .id(1L)
                .cliente(cliente)
                .horaReserva(LocalDateTime.now())
                .numeroPersonas(2)
                .estado(EstadoReserva.CONFIRMADA)
                .mesas(new HashSet<>())
                .build();

        // Act
        ReservaResponseDTO dto = reservaMapper.toResponseDTO(reserva);

        // Assert
        assertThat(dto.getMesas()).isNotNull();
        assertThat(dto.getMesas()).isEmpty();
    }

    @Test
    @DisplayName("mesaToDTO - Convierte mesa individual correctamente")
    void testMesaToDTO_MesaIndividual() {
        // Arrange
        Local local = Local.builder()
                .id(1L)
                .nombre("Test Local")
                .build();

        Mesa mesa = Mesa.builder()
                .id(10L)
                .numero(15)
                .capacidad(8)
                .disponible(true)
                .local(local)
                .build();

        // Act
        MesaDTO dto = reservaMapper.mesaToDTO(mesa);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getNumero()).isEqualTo(15);
        assertThat(dto.getCapacidad()).isEqualTo(8);
        assertThat(dto.getDisponible()).isTrue();
    }

    @Test
    @DisplayName("mesaToDTO - Mesa no disponible")
    void testMesaToDTO_MesaNoDisponible() {
        // Arrange
        Local local = Local.builder()
                .id(1L)
                .nombre("Test")
                .build();

        Mesa mesa = Mesa.builder()
                .id(1L)
                .numero(1)
                .capacidad(4)
                .disponible(false)
                .local(local)
                .build();

        // Act
        MesaDTO dto = reservaMapper.mesaToDTO(mesa);

        // Assert
        assertThat(dto.getDisponible()).isFalse();
    }

    @Test
    @DisplayName("toResponseDTO - Reserva sin observaciones")
    void testToResponseDTO_SinObservaciones() {
        // Arrange
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .telefono("123456789")
                .build();

        Reserva reserva = Reserva.builder()
                .id(1L)
                .cliente(cliente)
                .horaReserva(LocalDateTime.of(2025, 10, 26, 19, 0))
                .numeroPersonas(2)
                .estado(EstadoReserva.CONFIRMADA)
                .mesas(new HashSet<>())
                .observaciones(null)
                .build();

        // Act
        ReservaResponseDTO dto = reservaMapper.toResponseDTO(reserva);

        // Assert
        assertThat(dto.getObservaciones()).isNull();
    }

    @Test
    @DisplayName("toResponseDTO - Reserva CANCELADA")
    void testToResponseDTO_ReservaCancelada() {
        // Arrange
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .telefono("123456789")
                .build();

        Reserva reserva = Reserva.builder()
                .id(1L)
                .cliente(cliente)
                .horaReserva(LocalDateTime.now())
                .numeroPersonas(4)
                .estado(EstadoReserva.CANCELADA)
                .mesas(new HashSet<>())
                .build();

        // Act
        ReservaResponseDTO dto = reservaMapper.toResponseDTO(reserva);

        // Assert
        assertThat(dto.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    @DisplayName("toResponseDTO - Todos los estados de reserva")
    void testToResponseDTO_TodosLosEstados() {
        // Arrange & Act & Assert
        for (EstadoReserva estado : EstadoReserva.values()) {
            Cliente cliente = Cliente.builder()
                    .id(1L)
                    .nombre("Test")
                    .telefono("123456789")
                    .build();

            Reserva reserva = Reserva.builder()
                    .id(1L)
                    .cliente(cliente)
                    .horaReserva(LocalDateTime.now())
                    .numeroPersonas(2)
                    .estado(estado)
                    .mesas(new HashSet<>())
                    .build();

            ReservaResponseDTO dto = reservaMapper.toResponseDTO(reserva);

            assertThat(dto.getEstado()).isEqualTo(estado);
        }
    }

    @Test
    @DisplayName("toResponseDTO - Reserva con múltiples mesas")
    void testToResponseDTO_MultipleMesas() {
        // Arrange
        Local local = Local.builder()
                .id(1L)
                .nombre("Test")
                .build();

        Mesa mesa1 = Mesa.builder()
                .id(1L)
                .numero(1)
                .capacidad(2)
                .disponible(true)
                .local(local)
                .build();

        Mesa mesa2 = Mesa.builder()
                .id(2L)
                .numero(2)
                .capacidad(4)
                .disponible(true)
                .local(local)
                .build();

        Mesa mesa3 = Mesa.builder()
                .id(3L)
                .numero(3)
                .capacidad(6)
                .disponible(true)
                .local(local)
                .build();

        Set<Mesa> mesas = Set.of(mesa1, mesa2, mesa3);

        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .telefono("123456789")
                .build();

        Reserva reserva = Reserva.builder()
                .id(1L)
                .cliente(cliente)
                .horaReserva(LocalDateTime.now())
                .numeroPersonas(12)
                .estado(EstadoReserva.CONFIRMADA)
                .mesas(mesas)
                .build();

        // Act
        ReservaResponseDTO dto = reservaMapper.toResponseDTO(reserva);

        // Assert
        assertThat(dto.getMesas()).hasSize(3);
        assertThat(dto.getMesas()).extracting(MesaDTO::getCapacidad).containsExactlyInAnyOrder(2, 4, 6);
        assertThat(dto.getNumeroPersonas()).isEqualTo(12);
    }

    @Test
    @DisplayName("toResponseDTO - Gasto total se preserva")
    void testToResponseDTO_GastoTotal() {
        // Arrange
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Test")
                .telefono("123456789")
                .build();

        Reserva reserva = Reserva.builder()
                .id(1L)
                .cliente(cliente)
                .horaReserva(LocalDateTime.now())
                .numeroPersonas(2)
                .estado(EstadoReserva.FINALIZADA)
                .mesas(new HashSet<>())
                .gastoTotal(new BigDecimal("5432.10"))
                .build();

        // Act
        ReservaResponseDTO dto = reservaMapper.toResponseDTO(reserva);

        // Assert
        assertThat(dto.getGastoTotal()).isEqualByComparingTo(new BigDecimal("5432.10"));
    }
}
