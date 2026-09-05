package com.dioburger.controller;

import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Usuario;
import com.dioburger.model.enums.PlanSuscripcion;
import com.dioburger.model.enums.Rol;
import com.dioburger.repository.LocalRepository;
import com.dioburger.repository.PedidoRepository;
import com.dioburger.repository.ReservaRepository;
import com.dioburger.repository.UsuarioRepository;
import com.dioburger.service.PedidoService;
import com.dioburger.service.ReservaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para {@code GET /api/local/mi-local}.
 * Valida que un usuario ADMIN/COCINA obtiene su propio local, plan y features.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests de LocalController - mi-local")
class LocalControllerMiLocalTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioRepository usuarioRepository;
    @MockBean
    private LocalRepository localRepository;

    // Este test mockea LocalRepository, lo que rompería el sembrado de datos
    // (DataInitializer.count() devolvería 0 y save() sería no-op). Se anula el runner.
    @MockBean(name = "initData")
    private ApplicationRunner initData;

    // Dependencias del constructor de LocalController que no intervienen en estos endpoints
    @MockBean
    private PedidoService pedidoService;
    @MockBean
    private ReservaService reservaService;
    @MockBean
    private PedidoRepository pedidoRepository;
    @MockBean
    private ReservaRepository reservaRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ConfiguracionLocal config = ConfiguracionLocal.builder()
                .horaApertura(LocalTime.of(20, 0))
                .horaCierre(LocalTime.of(23, 30))
                .permiteTakeAway(true)
                .permiteDelivery(false)
                .permiteReservas(true)
                .impresionActiva(false)
                .waPhoneId("123456789")
                .waAccessToken("EAAG-token-demo")
                .build();

        Local local = Local.builder()
                .id(7L)
                .nombre("Dio Burger Centro")
                .direccion("Av. Principal 123")
                .telefono("+5491187654321")
                .planSuscripcion(PlanSuscripcion.ESTANDAR)
                .planActivo(true)
                .configuracion(config)
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .username("admin_centro")
                .password("hash")
                .rol(Rol.ROLE_ADMIN)
                .local(local)
                .build();
    }

    @Test
    @DisplayName("GET /api/local/mi-local - Devuelve local, plan y features del usuario autenticado")
    @WithMockUser(username = "admin_centro", roles = "ADMIN")
    void devuelveMiLocal() throws Exception {
        when(usuarioRepository.findByUsername("admin_centro")).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/local/mi-local"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localId").value(7))
                .andExpect(jsonPath("$.nombre").value("Dio Burger Centro"))
                .andExpect(jsonPath("$.planSuscripcion").value("ESTANDAR"))
                .andExpect(jsonPath("$.planNombre").value("Estándar"))
                .andExpect(jsonPath("$.planActivo").value(true))
                .andExpect(jsonPath("$.whatsappConfigurado").value(true))
                .andExpect(jsonPath("$.features").isArray())
                // ESTANDAR incluye BOT_WHATSAPP pero no REPORTES_AVANZADOS (solo PREMIUM)
                .andExpect(jsonPath("$.features", hasItem("BOT_WHATSAPP")))
                .andExpect(jsonPath("$.features", not(hasItem("REPORTES_AVANZADOS"))));
    }

    @Test
    @DisplayName("GET /api/local/mi-local - Plan inactivo devuelve features vacías")
    @WithMockUser(username = "admin_centro", roles = "ADMIN")
    void planInactivoSinFeatures() throws Exception {
        usuario.getLocal().setPlanActivo(false);
        when(usuarioRepository.findByUsername("admin_centro")).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/local/mi-local"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planActivo").value(false))
                .andExpect(jsonPath("$.features").isEmpty());
    }

    @Test
    @DisplayName("GET /api/local/mi-local - Usuario sin local asociado devuelve 404")
    @WithMockUser(username = "huerfano", roles = "ADMIN")
    void usuarioSinLocal() throws Exception {
        Usuario sinLocal = Usuario.builder()
                .id(2L).username("huerfano").password("hash").rol(Rol.ROLE_ADMIN).build();
        when(usuarioRepository.findByUsername("huerfano")).thenReturn(Optional.of(sinLocal));

        mockMvc.perform(get("/api/local/mi-local"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/local/mi-local - Sin autenticación devuelve 401/403")
    void sinAuth() throws Exception {
        mockMvc.perform(get("/api/local/mi-local"))
                .andExpect(status().is4xxClientError());
    }

    // ===== Configuración =====

    @Test
    @DisplayName("GET /api/local/mi-local/configuracion - Devuelve config sin exponer tokens")
    @WithMockUser(username = "admin_centro", roles = "ADMIN")
    void devuelveConfiguracion() throws Exception {
        when(usuarioRepository.findByUsername("admin_centro")).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/local/mi-local/configuracion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaApertura").value("20:00:00"))
                .andExpect(jsonPath("$.permiteTakeAway").value(true))
                .andExpect(jsonPath("$.waPhoneId").value("123456789"))
                // el token nunca se devuelve, solo el flag
                .andExpect(jsonPath("$.waAccessToken").doesNotExist())
                .andExpect(jsonPath("$.waConfigurado").value(true))
                .andExpect(jsonPath("$.igConfigurado").value(false));
    }

    @Test
    @DisplayName("PUT /api/local/mi-local/configuracion - Actualización parcial; token vacío no pisa el existente")
    @WithMockUser(username = "admin_centro", roles = "ADMIN")
    void actualizaConfiguracionParcial() throws Exception {
        when(usuarioRepository.findByUsername("admin_centro")).thenReturn(Optional.of(usuario));

        String body = objectMapper.writeValueAsString(Map.of(
                "permiteDelivery", true,
                "waPhoneId", "999888777",
                "waAccessToken", "" // vacío => no debe cambiar el token existente
        ));

        mockMvc.perform(put("/api/local/mi-local/configuracion")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permiteDelivery").value(true))
                .andExpect(jsonPath("$.waPhoneId").value("999888777"))
                .andExpect(jsonPath("$.waConfigurado").value(true)); // seguía configurado

        // el token en memoria no se tocó
        org.junit.jupiter.api.Assertions.assertEquals(
                "EAAG-token-demo", usuario.getLocal().getConfiguracion().getWaAccessToken());
        verify(localRepository).save(any());
    }

    @Test
    @DisplayName("PUT /api/local/mi-local/configuracion - COCINA no puede (solo ADMIN)")
    @WithMockUser(username = "cocina_centro", roles = "COCINA")
    void cocinaNoPuedeEditarConfig() throws Exception {
        mockMvc.perform(put("/api/local/mi-local/configuracion")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
