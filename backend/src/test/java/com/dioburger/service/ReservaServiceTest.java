package com.dioburger.service;

import com.dioburger.exception.HorarioNoDisponibleException;
import com.dioburger.exception.NotFoundException;
import com.dioburger.exception.ReservaDuplicadaException;
import com.dioburger.mapper.ReservaMapper;
import com.dioburger.model.dto.ClienteDTO;
import com.dioburger.model.dto.ReservaDTO;
import com.dioburger.model.dto.ReservaResponseDTO;
import com.dioburger.model.entity.*;
import com.dioburger.model.enums.EstadoReserva;
import com.dioburger.repository.ClienteRepository;
import com.dioburger.repository.MesaRepository;
import com.dioburger.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ReservaService.
 * 
 * Cobertura:
 * - Crear reserva con idempotencia
 * - Asignación inteligente de mesas
 * - Registro de gastos y liberación de mesas
 * - Consulta de reservas
 * - Cancelación de reservas
 * - Validaciones de horarios y disponibilidad
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaService - Tests Unitarios")
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ReservaMapper reservaMapper;

    @Mock
    private HorarioService horarioService;

    @Mock
    private LocalService localService;

    @InjectMocks
    private ReservaService reservaService;

    private Local local;
    private ConfiguracionLocal configuracion;
    private Cliente cliente;
    private Mesa mesa1;
    private Mesa mesa2;
    private Mesa mesa3;
    private ReservaDTO reservaDTO;
    private ClienteDTO clienteDTO;

    @BeforeEach
    void setUp() {
        // Configurar local
        local = new Local();
        local.setId(1L);
        local.setNombre("Dio Burger Central");
        local.setTelefono("549349366512");

        configuracion = new ConfiguracionLocal();
        configuracion.setPermiteReservas(true);
        local.setConfiguracion(configuracion);

        // Configurar cliente
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan Pérez");
        cliente.setTelefono("549123456789");

        // Configurar mesas
        mesa1 = new Mesa();
        mesa1.setId(1L);
        mesa1.setNumero(1);
        mesa1.setCapacidad(2);
        mesa1.setDisponible(true);
        mesa1.setLocal(local);

        mesa2 = new Mesa();
        mesa2.setId(2L);
        mesa2.setNumero(2);
        mesa2.setCapacidad(4);
        mesa2.setDisponible(true);
        mesa2.setLocal(local);

        mesa3 = new Mesa();
        mesa3.setId(3L);
        mesa3.setNumero(3);
        mesa3.setCapacidad(6);
        mesa3.setDisponible(true);
        mesa3.setLocal(local);

        // Configurar DTOs
        clienteDTO = new ClienteDTO();
        clienteDTO.setNombre("Juan Pérez");
        clienteDTO.setTelefono("549123456789");

        reservaDTO = new ReservaDTO();
        reservaDTO.setRequestId("REQ-12345");
        reservaDTO.setCliente(clienteDTO);
        reservaDTO.setNumeroPersonas(4);
        reservaDTO.setHoraReserva(LocalDateTime.now().plusHours(2));
        reservaDTO.setObservaciones("Mesa cerca de la ventana");
    }

    // ==================== CREAR RESERVA ====================

    @Test
    @DisplayName("crearReserva - Reserva nueva exitosa con una mesa")
    void crearReserva_reservaNueva_creaExitosamente() {
        // Arrange
        when(reservaRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(horarioService.getHorariosSugeridosReservas(anyString()))
                .thenReturn(Arrays.asList("18:00", "19:00", "20:00"));
        when(mesaRepository.findByLocalAndDisponibleTrue(any(Local.class)))
                .thenReturn(Arrays.asList(mesa2)); // Mesa de 4 personas
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));

        Reserva reservaGuardada = new Reserva();
        reservaGuardada.setId(1L);
        reservaGuardada.setCliente(cliente);
        reservaGuardada.setMesas(Set.of(mesa2));
        reservaGuardada.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaGuardada);

        ReservaResponseDTO responseDTO = new ReservaResponseDTO();
        responseDTO.setId(1L);
        when(reservaMapper.toResponseDTO(any(Reserva.class))).thenReturn(responseDTO);

        // Act
        ReservaResponseDTO resultado = reservaService.crearReserva("549349366512", reservaDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(reservaRepository).save(any(Reserva.class));
        verify(reservaMapper).toResponseDTO(any(Reserva.class));
    }

    @Test
    @DisplayName("crearReserva - Reserva duplicada devuelve reserva existente (idempotencia)")
    void crearReserva_requestIdDuplicado_devuelveReservaExistente() {
        // Arrange
        Reserva reservaExistente = new Reserva();
        reservaExistente.setId(1L);
        reservaExistente.setRequestId("REQ-12345");

        when(reservaRepository.findByRequestId("REQ-12345")).thenReturn(Optional.of(reservaExistente));

        ReservaResponseDTO responseDTO = new ReservaResponseDTO();
        responseDTO.setId(1L);
        when(reservaMapper.toResponseDTO(reservaExistente)).thenReturn(responseDTO);

        // Act
        ReservaResponseDTO resultado = reservaService.crearReserva("549349366512", reservaDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(reservaRepository, never()).save(any(Reserva.class));
        verify(reservaMapper).toResponseDTO(reservaExistente);
    }

    @Test
    @DisplayName("crearReserva - Local no permite reservas lanza excepción")
    void crearReserva_localNoPermiteReservas_lanzaException() {
        // Arrange
        configuracion.setPermiteReservas(false);
        when(reservaRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                reservaService.crearReserva("549349366512", reservaDTO)
        );

        assertTrue(exception.getMessage().contains("no acepta reservas"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crearReserva - Sin horarios disponibles lanza excepción")
    void crearReserva_sinHorariosDisponibles_lanzaException() {
        // Arrange
        when(reservaRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(horarioService.getHorariosSugeridosReservas(anyString()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        HorarioNoDisponibleException exception = assertThrows(HorarioNoDisponibleException.class, () ->
                reservaService.crearReserva("549349366512", reservaDTO)
        );

        assertTrue(exception.getMessage().contains("No hay horarios disponibles"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crearReserva - Sin mesas disponibles lanza excepción")
    void crearReserva_sinMesasDisponibles_lanzaException() {
        // Arrange
        when(reservaRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(horarioService.getHorariosSugeridosReservas(anyString()))
                .thenReturn(Arrays.asList("18:00", "19:00"));
        when(mesaRepository.findByLocalAndDisponibleTrue(any(Local.class)))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        HorarioNoDisponibleException exception = assertThrows(HorarioNoDisponibleException.class, () ->
                reservaService.crearReserva("549349366512", reservaDTO)
        );

        assertTrue(exception.getMessage().contains("No hay mesas disponibles"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crearReserva - Capacidad insuficiente lanza excepción")
    void crearReserva_capacidadInsuficiente_lanzaException() {
        // Arrange
        reservaDTO.setNumeroPersonas(10); // Requiere 10 personas

        when(reservaRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(horarioService.getHorariosSugeridosReservas(anyString()))
                .thenReturn(Arrays.asList("18:00", "19:00"));
        when(mesaRepository.findByLocalAndDisponibleTrue(any(Local.class)))
                .thenReturn(Arrays.asList(mesa1, mesa2)); // Total: 2+4 = 6 personas

        // Act & Assert
        HorarioNoDisponibleException exception = assertThrows(HorarioNoDisponibleException.class, () ->
                reservaService.crearReserva("549349366512", reservaDTO)
        );

        assertTrue(exception.getMessage().contains("No hay mesas disponibles para 10 personas"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("crearReserva - Múltiples mesas asignadas correctamente")
    void crearReserva_multiplesMesas_asignaCorrectamente() {
        // Arrange
        reservaDTO.setNumeroPersonas(8); // Requiere 2+6 o 4+4 mesas

        when(reservaRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(horarioService.getHorariosSugeridosReservas(anyString()))
                .thenReturn(Arrays.asList("18:00", "19:00"));
        when(mesaRepository.findByLocalAndDisponibleTrue(any(Local.class)))
                .thenReturn(Arrays.asList(mesa1, mesa2, mesa3)); // 2+4+6 = 12 personas
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));

        Reserva reservaGuardada = new Reserva();
        reservaGuardada.setId(1L);
        reservaGuardada.setMesas(new LinkedHashSet<>(Arrays.asList(mesa1, mesa2, mesa3)));

        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaGuardada);

        ReservaResponseDTO responseDTO = new ReservaResponseDTO();
        responseDTO.setId(1L);
        when(reservaMapper.toResponseDTO(any(Reserva.class))).thenReturn(responseDTO);

        // Act
        ReservaResponseDTO resultado = reservaService.crearReserva("549349366512", reservaDTO);

        // Assert
        assertNotNull(resultado);
        verify(reservaRepository).save(argThat(reserva -> {
            Set<Mesa> mesasAsignadas = reserva.getMesas();
            // Verifica que se asignaron al menos 2 mesas (greedy: 2+4=6, 2+6=8, 4+6=10)
            return mesasAsignadas != null && mesasAsignadas.size() >= 2;
        }));
    }

    @Test
    @DisplayName("crearReserva - Cliente nuevo se crea automáticamente")
    void crearReserva_clienteNuevo_creaAutomaticamente() {
        // Arrange
        when(reservaRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(horarioService.getHorariosSugeridosReservas(anyString()))
                .thenReturn(Arrays.asList("18:00", "19:00"));
        when(mesaRepository.findByLocalAndDisponibleTrue(any(Local.class)))
                .thenReturn(Arrays.asList(mesa2));
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.empty()); // Cliente NO existe

        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setId(2L);
        nuevoCliente.setNombre("Juan Pérez");
        nuevoCliente.setTelefono("549123456789");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(nuevoCliente);

        Reserva reservaGuardada = new Reserva();
        reservaGuardada.setId(1L);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaGuardada);

        ReservaResponseDTO responseDTO = new ReservaResponseDTO();
        when(reservaMapper.toResponseDTO(any(Reserva.class))).thenReturn(responseDTO);

        // Act
        reservaService.crearReserva("549349366512", reservaDTO);

        // Assert
        verify(clienteRepository).save(argThat(c ->
                "Juan Pérez".equals(c.getNombre()) &&
                        "549123456789".equals(c.getTelefono())
        ));
    }

    @Test
    @DisplayName("crearReserva - Mesas marcadas como no disponibles")
    void crearReserva_mesasMarcadasNoDisponibles() {
        // Arrange
        when(reservaRepository.findByRequestId(anyString())).thenReturn(Optional.empty());
        when(localService.buscarPorTelefono(anyString())).thenReturn(local);
        when(horarioService.getHorariosSugeridosReservas(anyString()))
                .thenReturn(Arrays.asList("18:00", "19:00"));
        when(mesaRepository.findByLocalAndDisponibleTrue(any(Local.class)))
                .thenReturn(Arrays.asList(mesa2));
        when(clienteRepository.findByTelefono(anyString())).thenReturn(Optional.of(cliente));

        Reserva reservaGuardada = new Reserva();
        reservaGuardada.setId(1L);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaGuardada);

        ReservaResponseDTO responseDTO = new ReservaResponseDTO();
        when(reservaMapper.toResponseDTO(any(Reserva.class))).thenReturn(responseDTO);

        // Act
        reservaService.crearReserva("549349366512", reservaDTO);

        // Assert
        assertFalse(mesa2.getDisponible(), "La mesa debe estar marcada como NO disponible");
        verify(reservaRepository).save(any(Reserva.class));
    }

    // ==================== REGISTRAR GASTO ====================

    @Test
    @DisplayName("registrarGasto - Registra gasto y libera mesas correctamente")
    void registrarGasto_reservaValida_registraYLiberaMesas() {
        // Arrange
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado(EstadoReserva.OCUPADA);
        reserva.setMesas(new HashSet<>(Arrays.asList(mesa1, mesa2)));

        mesa1.setDisponible(false);
        mesa2.setDisponible(false);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        BigDecimal gasto = new BigDecimal("15000.00");

        // Act
        reservaService.registrarGasto(1L, gasto);

        // Assert
        assertEquals(gasto, reserva.getGastoTotal());
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertTrue(mesa1.getDisponible(), "Mesa 1 debe estar liberada");
        assertTrue(mesa2.getDisponible(), "Mesa 2 debe estar liberada");
        verify(reservaRepository).save(reserva);
    }

    @Test
    @DisplayName("registrarGasto - Reserva no existe lanza excepción")
    void registrarGasto_reservaNoExiste_lanzaException() {
        // Arrange
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                reservaService.registrarGasto(999L, BigDecimal.valueOf(10000))
        );

        assertTrue(exception.getMessage().contains("Reserva no encontrada"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    // ==================== CANCELAR RESERVA ====================

    @Test
    @DisplayName("cancelarReserva - Cancela y libera mesas correctamente")
    void cancelarReserva_reservaValida_cancelaYLiberaMesas() {
        // Arrange
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setMesas(new HashSet<>(Arrays.asList(mesa1, mesa2)));

        mesa1.setDisponible(false);
        mesa2.setDisponible(false);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        // Act
        reservaService.cancelarReserva(1L);

        // Assert
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertTrue(mesa1.getDisponible(), "Mesa 1 debe estar liberada");
        assertTrue(mesa2.getDisponible(), "Mesa 2 debe estar liberada");
        verify(reservaRepository).save(reserva);
    }

    @Test
    @DisplayName("cancelarReserva - Reserva no existe lanza excepción")
    void cancelarReserva_reservaNoExiste_lanzaException() {
        // Arrange
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                reservaService.cancelarReserva(999L)
        );

        assertTrue(exception.getMessage().contains("Reserva no encontrada"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    // ==================== OBTENER RESERVA ====================

    @Test
    @DisplayName("obtenerReservaPorId - Reserva existe devuelve DTO")
    void obtenerReservaPorId_reservaExiste_devuelveDTO() {
        // Arrange
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setMesas(new HashSet<>(Arrays.asList(mesa1)));

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        ReservaResponseDTO responseDTO = new ReservaResponseDTO();
        responseDTO.setId(1L);
        when(reservaMapper.toResponseDTO(reserva)).thenReturn(responseDTO);

        // Act
        ReservaResponseDTO resultado = reservaService.obtenerReservaPorId("549349366512", 1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(reservaMapper).toResponseDTO(reserva);
    }

    @Test
    @DisplayName("obtenerReservaPorId - Reserva no existe lanza excepción")
    void obtenerReservaPorId_reservaNoExiste_lanzaException() {
        // Arrange
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                reservaService.obtenerReservaPorId("549349366512", 999L)
        );

        assertTrue(exception.getMessage().contains("Reserva no encontrada"));
    }

    @Test
    @DisplayName("obtenerReservaPorId - Reserva de otro local lanza excepción")
    void obtenerReservaPorId_reservaDeOtroLocal_lanzaException() {
        // Arrange
        Local otroLocal = new Local();
        otroLocal.setTelefono("549999999999");

        Mesa mesaOtroLocal = new Mesa();
        mesaOtroLocal.setLocal(otroLocal);

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setMesas(new HashSet<>(Arrays.asList(mesaOtroLocal)));

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                reservaService.obtenerReservaPorId("549349366512", 1L)
        );

        assertTrue(exception.getMessage().contains("no pertenece a este local"));
    }

    // ==================== OBTENER RESERVAS POR LOCAL ====================

    @Test
    @DisplayName("obtenerReservasPorLocal - Sin rango de fechas devuelve todas")
    void obtenerReservasPorLocal_sinRangoFechas_devuelveTodas() {
        // Arrange
        Reserva reserva1 = new Reserva();
        reserva1.setId(1L);

        Reserva reserva2 = new Reserva();
        reserva2.setId(2L);

        List<Reserva> reservas = Arrays.asList(reserva1, reserva2);

        when(localService.buscarPorTelefono("549349366512")).thenReturn(local);
        when(reservaRepository.findByMesas_Local(local)).thenReturn(reservas);

        ReservaResponseDTO dto1 = new ReservaResponseDTO();
        dto1.setId(1L);
        ReservaResponseDTO dto2 = new ReservaResponseDTO();
        dto2.setId(2L);

        when(reservaMapper.toResponseDTO(reserva1)).thenReturn(dto1);
        when(reservaMapper.toResponseDTO(reserva2)).thenReturn(dto2);

        // Act
        List<ReservaResponseDTO> resultado = reservaService.obtenerReservasPorLocal("549349366512", null, null);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(reservaRepository).findByMesas_Local(local);
    }

    @Test
    @DisplayName("obtenerReservasPorLocal - Con rango de fechas filtra correctamente")
    void obtenerReservasPorLocal_conRangoFechas_filtraCorrectamente() {
        // Arrange
        LocalDateTime desde = LocalDateTime.now();
        LocalDateTime hasta = LocalDateTime.now().plusDays(7);

        Reserva reserva1 = new Reserva();
        reserva1.setId(1L);

        List<Reserva> reservas = Arrays.asList(reserva1);

        when(localService.buscarPorTelefono("549349366512")).thenReturn(local);
        when(reservaRepository.findByMesas_LocalAndHoraReservaBetween(local, desde, hasta))
                .thenReturn(reservas);

        ReservaResponseDTO dto1 = new ReservaResponseDTO();
        dto1.setId(1L);
        when(reservaMapper.toResponseDTO(reserva1)).thenReturn(dto1);

        // Act
        List<ReservaResponseDTO> resultado = reservaService.obtenerReservasPorLocal("549349366512", desde, hasta);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(reservaRepository).findByMesas_LocalAndHoraReservaBetween(local, desde, hasta);
    }
}
