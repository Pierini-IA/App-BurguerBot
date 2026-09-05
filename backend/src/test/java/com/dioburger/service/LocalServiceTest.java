package com.dioburger.service;

import com.dioburger.exception.NotFoundException;
import com.dioburger.model.entity.Local;
import com.dioburger.model.enums.PlanSuscripcion;
import com.dioburger.repository.LocalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LocalService.
 * Valida especialmente el método refactorizado buscarPorTelefono().
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LocalService - Tests Unitarios")
class LocalServiceTest {

    @Mock
    private LocalRepository localRepository;

    @InjectMocks
    private LocalService localService;

    private Local localEjemplo;

    @BeforeEach
    void setUp() {
        localEjemplo = new Local();
        localEjemplo.setId(1L);
        localEjemplo.setNombre("Dio Burger Palermo");
        localEjemplo.setTelefono("+5491187654321");
        localEjemplo.setDireccion("Av. Córdoba 1234");
        localEjemplo.setPlanSuscripcion(PlanSuscripcion.ESTANDAR);
    }

    // ==================== buscarPorTelefono() ====================

    @Test
    @DisplayName("buscarPorTelefono - Local existe - Debe retornarlo")
    void buscarPorTelefono_localExiste_retornaLocal() {
        // Given
        String telefono = localEjemplo.getTelefono();
        when(localRepository.findByTelefono(telefono))
                .thenReturn(Optional.of(localEjemplo));

        // When
        Local resultado = localService.buscarPorTelefono(telefono);

        // Then
        assertNotNull(resultado, "El local no debe ser null");
        assertEquals(localEjemplo.getId(), resultado.getId());
        assertEquals(localEjemplo.getNombre(), resultado.getNombre());
        assertEquals(localEjemplo.getTelefono(), resultado.getTelefono());
        assertEquals(localEjemplo.getDireccion(), resultado.getDireccion());
        assertEquals(localEjemplo.getPlanSuscripcion(), resultado.getPlanSuscripcion());
        
        verify(localRepository, times(1)).findByTelefono(telefono);
    }

    @Test
    @DisplayName("buscarPorTelefono - Local no existe - Debe lanzar NotFoundException")
    void buscarPorTelefono_localNoExiste_lanzaNotFoundException() {
        // Given
        String telefonoInexistente = "+5499999999999";
        when(localRepository.findByTelefono(telefonoInexistente))
                .thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> localService.buscarPorTelefono(telefonoInexistente),
                "Debe lanzar NotFoundException cuando el local no existe"
        );

        // Verificar mensaje de error
        assertTrue(
                exception.getMessage().contains("Local no encontrado"),
                "El mensaje debe indicar que el local no fue encontrado"
        );
        assertTrue(
                exception.getMessage().contains(telefonoInexistente),
                "El mensaje debe incluir el teléfono buscado"
        );

        verify(localRepository, times(1)).findByTelefono(telefonoInexistente);
    }

    @Test
    @DisplayName("buscarPorTelefono - Mensaje de error consistente")
    void buscarPorTelefono_mensajeErrorConsistente() {
        // Given
        String telefono = "+5491234567890";
        when(localRepository.findByTelefono(telefono))
                .thenReturn(Optional.empty());

        // When
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> localService.buscarPorTelefono(telefono)
        );

        // Then - Validar formato exacto del mensaje
        String mensajeEsperado = "Local no encontrado con teléfono: " + telefono;
        assertEquals(
                mensajeEsperado, 
                exception.getMessage(),
                "El mensaje de error debe tener el formato estándar"
        );
    }

    @Test
    @DisplayName("buscarPorTelefono - Diferentes formatos de teléfono")
    void buscarPorTelefono_diferentesFormatos() {
        // Given - Probar diferentes formatos de teléfono
        String[] telefonos = {
                "+5491187654321",
                "5491187654321",
                "+549349366512",
                "549349366512"
        };

        for (String telefono : telefonos) {
            Local local = new Local();
            local.setId(1L);
            local.setTelefono(telefono);
            
            when(localRepository.findByTelefono(telefono))
                    .thenReturn(Optional.of(local));

            // When
            Local resultado = localService.buscarPorTelefono(telefono);

            // Then
            assertNotNull(resultado);
            assertEquals(telefono, resultado.getTelefono());
            
            verify(localRepository, times(1)).findByTelefono(telefono);
            reset(localRepository); // Limpiar para próxima iteración
        }
    }

    @Test
    @DisplayName("buscarPorTelefono - Múltiples llamadas al mismo teléfono")
    void buscarPorTelefono_multiplesCalls_usaRepository() {
        // Given
        String telefono = localEjemplo.getTelefono();
        when(localRepository.findByTelefono(telefono))
                .thenReturn(Optional.of(localEjemplo));

        // When - Llamar 3 veces
        Local resultado1 = localService.buscarPorTelefono(telefono);
        Local resultado2 = localService.buscarPorTelefono(telefono);
        Local resultado3 = localService.buscarPorTelefono(telefono);

        // Then - Cada llamada debe consultar el repository
        assertNotNull(resultado1);
        assertNotNull(resultado2);
        assertNotNull(resultado3);
        
        verify(localRepository, times(3)).findByTelefono(telefono);
    }

    @Test
    @DisplayName("buscarPorTelefono - Local sin plan asignado")
    void buscarPorTelefono_localSinPlan() {
        // Given
        Local localSinPlan = new Local();
        localSinPlan.setId(2L);
        localSinPlan.setNombre("Local Sin Plan");
        localSinPlan.setTelefono("+5491199999999");
        localSinPlan.setPlanSuscripcion(null); // Sin plan

        when(localRepository.findByTelefono(localSinPlan.getTelefono()))
                .thenReturn(Optional.of(localSinPlan));

        // When
        Local resultado = localService.buscarPorTelefono(localSinPlan.getTelefono());

        // Then - Debe retornar el local incluso sin plan
        assertNotNull(resultado);
        assertEquals(localSinPlan.getId(), resultado.getId());
        assertNull(resultado.getPlanSuscripcion(), "El plan debe ser null");
    }

    @Test
    @DisplayName("buscarPorTelefono - Performance con diferentes planes")
    void buscarPorTelefono_diferentesPlanes() {
        // Given - Locales con diferentes planes
        PlanSuscripcion[] planes = PlanSuscripcion.values();

        for (PlanSuscripcion plan : planes) {
            Local local = new Local();
            local.setId(1L);
            local.setTelefono("+549" + plan.ordinal());
            local.setPlanSuscripcion(plan);

            when(localRepository.findByTelefono(local.getTelefono()))
                    .thenReturn(Optional.of(local));

            // When
            Local resultado = localService.buscarPorTelefono(local.getTelefono());

            // Then
            assertNotNull(resultado);
            assertEquals(plan, resultado.getPlanSuscripcion());
            
            reset(localRepository);
        }
    }

    // ==================== CASOS EDGE ====================

    @Test
    @DisplayName("buscarPorTelefono - Teléfono vacío")
    void buscarPorTelefono_telefonoVacio() {
        // Given
        when(localRepository.findByTelefono(""))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                NotFoundException.class,
                () -> localService.buscarPorTelefono(""),
                "Teléfono vacío debe lanzar NotFoundException"
        );
    }

    @Test
    @DisplayName("buscarPorTelefono - Teléfono con espacios")
    void buscarPorTelefono_telefonoConEspacios() {
        // Given
        String telefonoConEspacios = " +5491187654321 ";
        when(localRepository.findByTelefono(telefonoConEspacios))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                NotFoundException.class,
                () -> localService.buscarPorTelefono(telefonoConEspacios)
        );
    }

    @Test
    @DisplayName("buscarPorTelefono - Repository lanza excepción")
    void buscarPorTelefono_repositoryLanzaExcepcion() {
        // Given
        String telefono = "+5491187654321";
        when(localRepository.findByTelefono(telefono))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // When & Then
        assertThrows(
                RuntimeException.class,
                () -> localService.buscarPorTelefono(telefono),
                "Debe propagar la excepción del repository"
        );
    }
}
