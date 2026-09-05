package com.dioburger.controller;

import com.dioburger.model.dto.ConfiguracionLocalDTO;
import com.dioburger.model.dto.MiLocalDTO;
import com.dioburger.model.dto.PedidoDTO;
import com.dioburger.model.dto.ReservaDTO;
import com.dioburger.model.dto.ReservaResponseDTO;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Pedido;
import com.dioburger.model.entity.Reserva;
import com.dioburger.model.entity.Usuario;
import com.dioburger.model.enums.Feature;
import com.dioburger.model.enums.PlanFeatureMatrix;
import com.dioburger.model.enums.PlanSuscripcion;
import com.dioburger.repository.LocalRepository;
import com.dioburger.repository.PedidoRepository;
import com.dioburger.repository.ReservaRepository;
import com.dioburger.repository.UsuarioRepository;
import com.dioburger.service.PedidoService;
import com.dioburger.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.dioburger.exception.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Controlador REST para operaciones del panel web del local.
 * Accesible para usuarios autenticados (ROLE_ADMIN o ROLE_COCINA).
 * 
 * Endpoints principales:
 * - Gestión de pedidos desde el local
 * - Gestión de reservas desde el local
 * - Consulta de información del día
 * 
 * @author Dio Burger Team
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/local")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class LocalController extends BaseController {

    private final PedidoService pedidoService;
    private final ReservaService reservaService;
    private final PedidoRepository pedidoRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalRepository localRepository;

    // ========== PEDIDOS ==========

    /**
     * Crea un nuevo pedido desde el panel web del local.
     * Similar al endpoint del bot, pero con validación de autenticación.
     * 
     * @param telefonoLocal Teléfono del local
     * @param pedidoDTO Datos del pedido
     * @return Pedido creado con total calculado
     */
    @PostMapping("/pedido")
    public ResponseEntity<Pedido> crearPedido(
            @RequestParam String telefonoLocal,
            @Valid @RequestBody PedidoDTO pedidoDTO) {
        
        logOperacionInicio("Panel web crea pedido", telefonoLocal);
        
        // Verificar que el local existe
        validarLocal(telefonoLocal);

        Pedido pedido = pedidoService.crearPedido(
                pedidoDTO, telefonoLocal, com.dioburger.model.enums.OrigenPedido.LOCAL);

        logOperacionExito("Pedido creado desde panel web",
                String.format("ID: %d, Total: $%s", pedido.getId(), pedido.getTotal()));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pedido);
    }

    /**
     * Lista todos los pedidos del día actual.
     * 
     * @param telefonoLocal Teléfono del local
     * @return Lista de pedidos del día
     */
    @GetMapping("/pedidos")
    public ResponseEntity<List<Pedido>> listarPedidosDelDia(
            @RequestParam String telefonoLocal) {
        
        log.info("🏪 Panel web lista pedidos del día - Local: {}", telefonoLocal);

        var local = localService.buscarPorTelefono(telefonoLocal);

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        List<Pedido> pedidosDelDia = pedidoRepository
                .findByLocalAndHoraPedidoBetween(local, inicioDia, finDia);

        log.info("✅ {} pedidos encontrados para hoy", pedidosDelDia.size());

        return ResponseEntity.ok(pedidosDelDia);
    }

    /**
     * Obtiene el detalle de un pedido específico.
     * 
     * @param id ID del pedido
     * @param telefonoLocal Teléfono del local (validación Multi-Tenancy)
     * @return Pedido con todos sus items
     */
    @GetMapping("/pedidos/{id}")
    public ResponseEntity<Pedido> obtenerPedido(
            @PathVariable Long id,
            @RequestParam String telefonoLocal) {
        
        log.info("🏪 Panel web obtiene pedido {} - Local: {}", id, telefonoLocal);

        var local = localService.buscarPorTelefono(telefonoLocal);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Validar Multi-Tenancy
        if (!pedido.getLocal().getId().equals(local.getId())) {
            log.warn("⚠️ Intento de acceso a pedido de otro local");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(pedido);
    }

    /**
     * Obtiene pedidos filtrados por rango de fechas.
     * 
     * @param telefonoLocal Teléfono del local
     * @param fechaInicio Fecha de inicio (formato: yyyy-MM-dd)
     * @param fechaFin Fecha de fin (formato: yyyy-MM-dd)
     * @return Lista de pedidos en el rango
     */
    @GetMapping("/pedidos/rango")
    public ResponseEntity<List<Pedido>> obtenerPedidosPorRango(
            @RequestParam String telefonoLocal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        log.info("🏪 Panel web consulta pedidos {} a {} - Local: {}", 
                fechaInicio, fechaFin, telefonoLocal);

        var local = localService.buscarPorTelefono(telefonoLocal);

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);

        List<Pedido> pedidos = pedidoRepository.findByLocalAndHoraPedidoBetween(
                local, inicio, fin);

        log.info("✅ {} pedidos encontrados en el rango", pedidos.size());

        return ResponseEntity.ok(pedidos);
    }

    // ========== RESERVAS ==========

    /**
     * Crea una nueva reserva desde el panel web del local.
     * 
     * @param telefonoLocal Teléfono del local
     * @param reservaDTO Datos de la reserva
     * @return Reserva creada con mesas asignadas
     */
    @PostMapping("/reserva")
    public ResponseEntity<ReservaResponseDTO> crearReserva(
            @RequestParam String telefonoLocal,
            @Valid @RequestBody ReservaDTO reservaDTO) {
        
        log.info("🏪 Panel web crea reserva - Local: {}, Personas: {}", 
                telefonoLocal, reservaDTO.getNumeroPersonas());

        // Verificar que el local existe
        localService.buscarPorTelefono(telefonoLocal);

        ReservaResponseDTO response = reservaService.crearReserva(telefonoLocal, reservaDTO);

        log.info("✅ Reserva creada desde panel web - ID: {}, Mesas: {}", 
                response.getId(), response.getMesas().size());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Lista todas las reservas del día actual.
     * 
     * @param telefonoLocal Teléfono del local
     * @return Lista de reservas del día
     */
    @GetMapping("/reservas")
    public ResponseEntity<List<Reserva>> listarReservasDelDia(
            @RequestParam String telefonoLocal) {
        
        log.info("🏪 Panel web lista reservas del día - Local: {}", telefonoLocal);

        var local = localService.buscarPorTelefono(telefonoLocal);

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        List<Reserva> reservasDelDia = reservaRepository
                .findByMesas_LocalAndHoraReservaBetween(local, inicioDia, finDia);

        log.info("✅ {} reservas encontradas para hoy", reservasDelDia.size());

        return ResponseEntity.ok(reservasDelDia);
    }

    /**
     * Obtiene el detalle de una reserva específica.
     * 
     * @param id ID de la reserva
     * @param telefonoLocal Teléfono del local (validación Multi-Tenancy)
     * @return Reserva con mesas asignadas
     */
    @GetMapping("/reservas/{id}")
    public ResponseEntity<ReservaResponseDTO> obtenerReserva(
            @PathVariable Long id,
            @RequestParam String telefonoLocal) {
        
        log.info("🏪 Panel web obtiene reserva {} - Local: {}", id, telefonoLocal);

        ReservaResponseDTO reserva = reservaService.obtenerReservaPorId(telefonoLocal, id);

        return ResponseEntity.ok(reserva);
    }

    /**
     * Registra el gasto final de una reserva.
     * Libera las mesas ocupadas.
     * 
     * @param id ID de la reserva
     * @param telefonoLocal Teléfono del local
     * @param gastoTotal Monto total gastado
     * @return Reserva actualizada con estado FINALIZADA
     */
    @PatchMapping("/reservas/{id}/gasto")
    public ResponseEntity<Reserva> registrarGasto(
            @PathVariable Long id,
            @RequestParam String telefonoLocal,
            @RequestParam BigDecimal gastoTotal) {
        
        log.info("🏪 Panel web registra gasto - Reserva: {}, Monto: ${}", 
                id, gastoTotal);

        reservaService.registrarGasto(id, gastoTotal);
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        log.info("✅ Gasto registrado - Reserva {} finalizada", id);

        return ResponseEntity.ok(reserva);
    }

    /**
     * Cancela una reserva existente.
     * Libera las mesas asignadas.
     * 
     * @param id ID de la reserva
     * @param telefonoLocal Teléfono del local
     * @return Reserva cancelada
     */
    @DeleteMapping("/reservas/{id}")
    public ResponseEntity<Reserva> cancelarReserva(
            @PathVariable Long id,
            @RequestParam String telefonoLocal) {
        
        log.info("🏪 Panel web cancela reserva {} - Local: {}", id, telefonoLocal);

        reservaService.cancelarReserva(id);
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        log.info("✅ Reserva {} cancelada", id);

        return ResponseEntity.ok(reserva);
    }

    /**
     * Obtiene reservas filtradas por rango de fechas.
     * 
     * @param telefonoLocal Teléfono del local
     * @param fechaInicio Fecha de inicio (formato: yyyy-MM-dd)
     * @param fechaFin Fecha de fin (formato: yyyy-MM-dd)
     * @return Lista de reservas en el rango
     */
    @GetMapping("/reservas/rango")
    public ResponseEntity<List<Reserva>> obtenerReservasPorRango(
            @RequestParam String telefonoLocal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        log.info("🏪 Panel web consulta reservas {} a {} - Local: {}", 
                fechaInicio, fechaFin, telefonoLocal);

        var local = localService.buscarPorTelefono(telefonoLocal);

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);

        List<Reserva> reservas = reservaRepository
                .findByMesas_LocalAndHoraReservaBetween(local, inicio, fin);

        log.info("✅ {} reservas encontradas en el rango", reservas.size());

        return ResponseEntity.ok(reservas);
    }

    // ========== ESTADÍSTICAS ==========

    /**
     * Obtiene un resumen estadístico del día actual.
     * Incluye: total de pedidos, total de reservas, ingresos estimados.
     * 
     * @param telefonoLocal Teléfono del local
     * @return Objeto con estadísticas del día
     */
    @GetMapping("/estadisticas/dia")
    public ResponseEntity<EstadisticasDelDia> obtenerEstadisticasDelDia(
            @RequestParam String telefonoLocal) {
        
        log.info("🏪 Panel web consulta estadísticas del día - Local: {}", telefonoLocal);

        var local = localService.buscarPorTelefono(telefonoLocal);

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        // Pedidos del día
        List<Pedido> pedidosDelDia = pedidoRepository
                .findByLocalAndHoraPedidoBetween(local, inicioDia, finDia);

        // Reservas del día
        List<Reserva> reservasDelDia = reservaRepository
                .findByMesas_LocalAndHoraReservaBetween(local, inicioDia, finDia);

        // Calcular ingresos
        BigDecimal ingresosPedidos = pedidosDelDia.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresosReservas = reservasDelDia.stream()
                .map(Reserva::getGastoTotal)
                .filter(gasto -> gasto != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresosTotal = ingresosPedidos.add(ingresosReservas);

        EstadisticasDelDia estadisticas = new EstadisticasDelDia(
                pedidosDelDia.size(),
                reservasDelDia.size(),
                ingresosPedidos,
                ingresosReservas,
                ingresosTotal
        );

        log.info("✅ Estadísticas generadas - Pedidos: {}, Reservas: {}, Total: ${}", 
                estadisticas.totalPedidos, 
                estadisticas.totalReservas, 
                estadisticas.ingresosTotal);

        return ResponseEntity.ok(estadisticas);
    }

    // ========== MI LOCAL ==========

    /**
     * Devuelve la información del local del usuario autenticado.
     *
     * <p>Pensado para los paneles de ADMIN y COCINA, que necesitan conocer
     * su propio local, el plan vigente y las funcionalidades habilitadas
     * sin acceder al listado global de locales (exclusivo de SUPERADMIN).</p>
     *
     * @param authentication contexto de seguridad con el usuario autenticado
     * @return datos del local, plan y features habilitadas
     */
    @GetMapping("/mi-local")
    public ResponseEntity<MiLocalDTO> obtenerMiLocal(Authentication authentication) {

        String username = authentication.getName();
        log.info("🏪 {} consulta los datos de su local", username);

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + username));

        Local local = usuario.getLocal();
        if (local == null) {
            throw new NotFoundException("El usuario no está asociado a ningún local");
        }

        PlanSuscripcion plan = local.getPlanSuscripcion() != null
                ? local.getPlanSuscripcion()
                : PlanSuscripcion.BASICO;
        boolean planActivo = Boolean.TRUE.equals(local.getPlanActivo());

        // Si el plan está inactivo no se habilita ninguna feature (igual criterio que el frontend).
        List<String> features = planActivo
                ? PlanFeatureMatrix.getFeaturesForPlan(plan).stream()
                        .map(Feature::name)
                        .sorted(Comparator.naturalOrder())
                        .toList()
                : List.of();

        ConfiguracionLocal config = local.getConfiguracion();

        MiLocalDTO dto = MiLocalDTO.builder()
                .localId(local.getId())
                .nombre(local.getNombre())
                .direccion(local.getDireccion())
                .telefono(local.getTelefono())
                .planSuscripcion(plan)
                .planNombre(plan.getNombre())
                .planActivo(planActivo)
                .fechaFinPlan(local.getFechaFinPlan())
                .features(features)
                .horaApertura(config != null ? config.getHoraApertura() : null)
                .horaCierre(config != null ? config.getHoraCierre() : null)
                .permiteTakeAway(config != null ? config.getPermiteTakeAway() : null)
                .permiteDelivery(config != null ? config.getPermiteDelivery() : null)
                .permiteReservas(config != null ? config.getPermiteReservas() : null)
                .impresionActiva(config != null ? config.getImpresionActiva() : null)
                .whatsappConfigurado(config != null
                        && config.getWaPhoneId() != null && !config.getWaPhoneId().isBlank()
                        && config.getWaAccessToken() != null && !config.getWaAccessToken().isBlank())
                .build();

        log.info("✅ Local '{}' (plan {}, activo={}) con {} features",
                local.getNombre(), plan, planActivo, features.size());

        return ResponseEntity.ok(dto);
    }

    // ========== CONFIGURACIÓN DEL LOCAL ==========

    /**
     * Devuelve la configuración operativa del local del usuario autenticado.
     * Los tokens de Meta no se exponen: en su lugar viajan los flags {@code *Configurado}.
     *
     * @param authentication contexto de seguridad
     * @return configuración del local
     */
    @GetMapping("/mi-local/configuracion")
    public ResponseEntity<ConfiguracionLocalDTO> obtenerMiConfiguracion(Authentication authentication) {
        ConfiguracionLocal config = configuracionDelUsuario(authentication.getName());
        return ResponseEntity.ok(aDTO(config));
    }

    /**
     * Actualiza (parcialmente) la configuración del local del usuario autenticado.
     * Un campo {@code null} se deja como está. Un token de Meta vacío o nulo no se toca;
     * solo se reemplaza si llega un valor no vacío.
     *
     * @param authentication contexto de seguridad
     * @param dto            campos a actualizar
     * @return configuración ya actualizada
     */
    @PutMapping("/mi-local/configuracion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<ConfiguracionLocalDTO> actualizarMiConfiguracion(
            Authentication authentication,
            @RequestBody ConfiguracionLocalDTO dto) {

        String username = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        Local local = usuario.getLocal();
        if (local == null || local.getConfiguracion() == null) {
            throw new NotFoundException("El local no tiene configuración");
        }

        aplicar(local.getConfiguracion(), dto);
        localRepository.save(local); // cascade ALL -> persiste la configuración

        log.info("⚙️ {} actualizó la configuración de '{}'", username, local.getNombre());

        return ResponseEntity.ok(aDTO(local.getConfiguracion()));
    }

    private ConfiguracionLocal configuracionDelUsuario(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        Local local = usuario.getLocal();
        if (local == null || local.getConfiguracion() == null) {
            throw new NotFoundException("El local no tiene configuración");
        }
        return local.getConfiguracion();
    }

    private static boolean tiene(String s) {
        return s != null && !s.isBlank();
    }

    private static ConfiguracionLocalDTO aDTO(ConfiguracionLocal c) {
        return ConfiguracionLocalDTO.builder()
                .horaApertura(c.getHoraApertura())
                .horaCierre(c.getHoraCierre())
                .intervaloMinutosPedidos(c.getIntervaloMinutosPedidos())
                .maxPedidosPorIntervalo(c.getMaxPedidosPorIntervalo())
                .horaAperturaReservas(c.getHoraAperturaReservas())
                .horaCierreReservas(c.getHoraCierreReservas())
                .intervaloMinutosReservas(c.getIntervaloMinutosReservas())
                .maxReservasPorIntervalo(c.getMaxReservasPorIntervalo())
                .minutosAnticipacionCancelacion(c.getMinutosAnticipacionCancelacion())
                .permiteDelivery(c.getPermiteDelivery())
                .permiteTakeAway(c.getPermiteTakeAway())
                .permiteReservas(c.getPermiteReservas())
                .impresionActiva(c.getImpresionActiva())
                .urlWebhookImpresora(c.getUrlWebhookImpresora())
                .urlWebhookNotificaciones(c.getUrlWebhookNotificaciones())
                .urlWebhookAsignacionDelivery(c.getUrlWebhookAsignacionDelivery())
                .waPhoneId(c.getWaPhoneId())
                .fbPageId(c.getFbPageId())
                // tokens: nunca se devuelven, solo el flag
                .waConfigurado(tiene(c.getWaAccessToken()))
                .igConfigurado(tiene(c.getIgToken()))
                .fbConfigurado(tiene(c.getFbPageAccessToken()))
                .build();
    }

    private static void aplicar(ConfiguracionLocal c, ConfiguracionLocalDTO d) {
        if (d.getHoraApertura() != null) c.setHoraApertura(d.getHoraApertura());
        if (d.getHoraCierre() != null) c.setHoraCierre(d.getHoraCierre());
        if (d.getIntervaloMinutosPedidos() != null) c.setIntervaloMinutosPedidos(d.getIntervaloMinutosPedidos());
        if (d.getMaxPedidosPorIntervalo() != null) c.setMaxPedidosPorIntervalo(d.getMaxPedidosPorIntervalo());
        if (d.getHoraAperturaReservas() != null) c.setHoraAperturaReservas(d.getHoraAperturaReservas());
        if (d.getHoraCierreReservas() != null) c.setHoraCierreReservas(d.getHoraCierreReservas());
        if (d.getIntervaloMinutosReservas() != null) c.setIntervaloMinutosReservas(d.getIntervaloMinutosReservas());
        if (d.getMaxReservasPorIntervalo() != null) c.setMaxReservasPorIntervalo(d.getMaxReservasPorIntervalo());
        if (d.getMinutosAnticipacionCancelacion() != null) c.setMinutosAnticipacionCancelacion(d.getMinutosAnticipacionCancelacion());
        if (d.getPermiteDelivery() != null) c.setPermiteDelivery(d.getPermiteDelivery());
        if (d.getPermiteTakeAway() != null) c.setPermiteTakeAway(d.getPermiteTakeAway());
        if (d.getPermiteReservas() != null) c.setPermiteReservas(d.getPermiteReservas());
        if (d.getImpresionActiva() != null) c.setImpresionActiva(d.getImpresionActiva());
        if (d.getUrlWebhookImpresora() != null) c.setUrlWebhookImpresora(emptyToNull(d.getUrlWebhookImpresora()));
        if (d.getUrlWebhookNotificaciones() != null) c.setUrlWebhookNotificaciones(emptyToNull(d.getUrlWebhookNotificaciones()));
        if (d.getUrlWebhookAsignacionDelivery() != null) c.setUrlWebhookAsignacionDelivery(emptyToNull(d.getUrlWebhookAsignacionDelivery()));
        if (d.getWaPhoneId() != null) c.setWaPhoneId(emptyToNull(d.getWaPhoneId()));
        if (d.getFbPageId() != null) c.setFbPageId(emptyToNull(d.getFbPageId()));
        // Secretos: solo se reemplazan si llega un valor no vacío.
        if (tiene(d.getWaAccessToken())) c.setWaAccessToken(d.getWaAccessToken().trim());
        if (tiene(d.getIgToken())) c.setIgToken(d.getIgToken().trim());
        if (tiene(d.getFbPageAccessToken())) c.setFbPageAccessToken(d.getFbPageAccessToken().trim());
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /**
     * Record para encapsular estadísticas del día.
     */
    public record EstadisticasDelDia(
            int totalPedidos,
            int totalReservas,
            BigDecimal ingresosPedidos,
            BigDecimal ingresosReservas,
            BigDecimal ingresosTotal
    ) {}
}
