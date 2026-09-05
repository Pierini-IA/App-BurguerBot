package com.dioburger.controller;

import com.dioburger.model.dto.CambiarPlanDTO;
import com.dioburger.model.dto.LocalDTO;
import com.dioburger.model.dto.PlanInfoDTO;
import com.dioburger.model.dto.UsuarioCreateDTO;
import com.dioburger.model.dto.UsuarioDTO;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Usuario;
import com.dioburger.model.enums.Feature;
import com.dioburger.model.enums.PlanSuscripcion;
import com.dioburger.model.enums.Rol;
import com.dioburger.repository.LocalRepository;
import com.dioburger.repository.UsuarioRepository;
import com.dioburger.service.LocalService;
import com.dioburger.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controlador REST para operaciones del SuperAdmin.
 * Solo accesible para usuarios con rol ROLE_SUPERADMIN.
 * 
 * Endpoints principales:
 * - CRUD completo de Locales
 * - CRUD completo de Usuarios (TODO)
 * - Gestión de Configuraciones (TODO)
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperAdminController extends BaseController {

    private final LocalRepository localRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalService localService;
    private final PasswordEncoder passwordEncoder;
    private final PlanService planService;

    // ========================================
    // GESTIÓN DE LOCALES
    // ========================================

    /**
     * Obtener todos los locales.
     * 
     * @return lista de todos los locales
     */
    @GetMapping("/locales")
    public ResponseEntity<List<LocalDTO>> listarLocales() {
        logOperacionInicio("SuperAdmin lista todos los locales", null);
        
        try {
            List<Local> locales = localRepository.findAll();
            List<LocalDTO> localesDTO = locales.stream()
                .map(this::convertirLocalADTO)
                .toList();
            
            logOperacionExito("Locales encontrados", String.valueOf(localesDTO.size()));
            return ResponseEntity.ok(localesDTO);
            
        } catch (Exception e) {
            logOperacionError("Error al listar locales", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtener un local por ID.
     * 
     * @param id ID del local
     * @return local encontrado
     */
    @GetMapping("/locales/{id}")
    public ResponseEntity<LocalDTO> obtenerLocal(@PathVariable Long id) {
        logOperacionInicio("SuperAdmin obtiene local", String.valueOf(id));
        
        Local local = localRepository.findById(id)
            .orElseThrow(() -> {
                logOperacionError("Local no encontrado", String.valueOf(id));
                return new RuntimeException("Local no encontrado con ID: " + id);
            });
        
        LocalDTO localDTO = convertirLocalADTO(local);
        logOperacionExito("Local encontrado", local.getNombre());
        
        return ResponseEntity.ok(localDTO);
    }

    /**
     * Crear un nuevo local.
     * 
     * @param localDTO datos del local a crear
     * @return local creado
     */
    @PostMapping("/locales")
    public ResponseEntity<LocalDTO> crearLocal(@Valid @RequestBody LocalDTO localDTO) {
        logOperacionInicio("SuperAdmin crea local", localDTO.getNombre());
        
        try {
            Local nuevoLocal = Local.builder()
                .nombre(localDTO.getNombre())
                .direccion(localDTO.getDireccion())
                .telefono(localDTO.getTelefono())
                .build();

            // Un local sin ConfiguracionLocal queda inutilizable: no hay ningun
            // endpoint que la cree despues (PUT /api/local/mi-local/configuracion
            // exige que ya exista), asi que sin esto no se pueden definir horarios,
            // modalidades, credenciales de Meta ni politica de cancelacion.
            ConfiguracionLocal config = new ConfiguracionLocal();
            config.setHoraApertura(LocalTime.of(20, 0));
            config.setHoraCierre(LocalTime.of(23, 0));
            config.setHoraAperturaReservas(LocalTime.of(20, 0));
            config.setHoraCierreReservas(LocalTime.of(22, 0));
            config.setIntervaloMinutosPedidos(15);
            config.setMaxPedidosPorIntervalo(5);
            config.setIntervaloMinutosReservas(30);
            config.setMaxReservasPorIntervalo(3);
            config.setPermiteDelivery(false);
            config.setPermiteTakeAway(true);
            config.setPermiteReservas(false);
            config.setImpresionActiva(false);
            nuevoLocal.setConfiguracion(config);
            config.setLocal(nuevoLocal);

            Local localGuardado = localRepository.save(nuevoLocal);
            LocalDTO responseDTO = convertirLocalADTO(localGuardado);
            
            logOperacionExito("Local creado", localGuardado.getNombre());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
            
        } catch (Exception e) {
            logOperacionError("Error al crear local", e.getMessage());
            throw e;
        }
    }

    /**
     * Actualizar un local existente.
     * 
     * @param id ID del local a actualizar
     * @param localDTO datos actualizados del local
     * @return local actualizado
     */
    @PutMapping("/locales/{id}")
    public ResponseEntity<LocalDTO> actualizarLocal(
            @PathVariable Long id,
            @Valid @RequestBody LocalDTO localDTO) {
        
        logOperacionInicio("SuperAdmin actualiza local", String.valueOf(id));
        
        Local local = localRepository.findById(id)
            .orElseThrow(() -> {
                logOperacionError("Local no encontrado", String.valueOf(id));
                return new RuntimeException("Local no encontrado con ID: " + id);
            });
        
        // Actualizar campos básicos
        local.setNombre(localDTO.getNombre());
        local.setDireccion(localDTO.getDireccion());
        local.setTelefono(localDTO.getTelefono());
        
        // Actualizar campos del plan de suscripción
        if (localDTO.getPlanSuscripcion() != null) {
            local.setPlanSuscripcion(localDTO.getPlanSuscripcion());
        }
        if (localDTO.getPlanActivo() != null) {
            local.setPlanActivo(localDTO.getPlanActivo());
        }
        if (localDTO.getFechaInicioPlan() != null) {
            local.setFechaInicioPlan(localDTO.getFechaInicioPlan());
        }
        // Para fechaFinPlan, permitir null explícitamente (plan indefinido)
        local.setFechaFinPlan(localDTO.getFechaFinPlan());
        
        Local localActualizado = localRepository.save(local);
        LocalDTO responseDTO = convertirLocalADTO(localActualizado);
        
        logOperacionExito("Local actualizado", localActualizado.getNombre());
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Eliminar un local.
     * 
     * @param id ID del local a eliminar
     * @return respuesta sin contenido
     */
    @DeleteMapping("/locales/{id}")
    public ResponseEntity<Void> eliminarLocal(@PathVariable Long id) {
        logOperacionInicio("SuperAdmin elimina local", String.valueOf(id));
        
        Local local = localRepository.findById(id)
            .orElseThrow(() -> {
                logOperacionError("Local no encontrado", String.valueOf(id));
                return new RuntimeException("Local no encontrado con ID: " + id);
            });
        
        String nombreLocal = local.getNombre();
        localRepository.delete(local);
        
        logOperacionExito("Local eliminado", nombreLocal);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // GESTIÓN DE USUARIOS
    // ========================================

    /**
     * Obtener todos los usuarios.
     * 
     * @return lista de todos los usuarios
     */
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        logOperacionInicio("SuperAdmin lista todos los usuarios", null);
        
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<UsuarioDTO> usuarioDTOs = usuarios.stream()
                .map(this::convertirUsuarioADTO)
                .toList();
            
            logOperacionExito("Usuarios encontrados", String.valueOf(usuarioDTOs.size()));
            return ResponseEntity.ok(usuarioDTOs);
            
        } catch (Exception e) {
            logOperacionError("Error al listar usuarios", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtener un usuario por ID.
     * 
     * @param id ID del usuario
     * @return usuario encontrado
     */
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable Long id) {
        logOperacionInicio("SuperAdmin obtiene usuario", String.valueOf(id));
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
                logOperacionError("Usuario no encontrado", String.valueOf(id));
                return new RuntimeException("Usuario no encontrado con ID: " + id);
            });
        
        logOperacionExito("Usuario encontrado", usuario.getUsername());
        return ResponseEntity.ok(convertirUsuarioADTO(usuario));
    }

    /**
     * Crear un nuevo usuario.
     * 
     * @param usuarioDTO datos del usuario a crear
     * @return usuario creado
     */
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDTO> crearUsuario(@Valid @RequestBody UsuarioCreateDTO usuarioDTO) {
        logOperacionInicio("SuperAdmin crea usuario", usuarioDTO.getUsername());
        
        try {
            // Validar que el username no exista
            if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
                logOperacionError("Username ya existe", usuarioDTO.getUsername());
                throw new IllegalArgumentException(
                    "Ya existe un usuario con el username: " + usuarioDTO.getUsername()
                );
            }

            // Si no es SUPERADMIN, debe tener telefonoLocal
            if (usuarioDTO.getRol() != Rol.ROLE_SUPERADMIN && 
                (usuarioDTO.getTelefonoLocal() == null || usuarioDTO.getTelefonoLocal().isBlank())) {
                throw new IllegalArgumentException(
                    "El campo telefonoLocal es obligatorio para roles diferentes a SUPERADMIN"
                );
            }

            // Si es SUPERADMIN, no debe tener telefonoLocal
            if (usuarioDTO.getRol() == Rol.ROLE_SUPERADMIN && 
                usuarioDTO.getTelefonoLocal() != null && !usuarioDTO.getTelefonoLocal().isBlank()) {
                throw new IllegalArgumentException(
                    "SUPERADMIN no debe estar asociado a un local específico"
                );
            }

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(usuarioDTO.getUsername());
            nuevoUsuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
            nuevoUsuario.setRol(usuarioDTO.getRol());

            // Asociar al local si no es SUPERADMIN
            if (usuarioDTO.getRol() != Rol.ROLE_SUPERADMIN) {
                Local local = localService.buscarPorTelefono(usuarioDTO.getTelefonoLocal());
                nuevoUsuario.setLocal(local);
            }

            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
            
            logOperacionExito("Usuario creado", usuarioGuardado.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertirUsuarioADTO(usuarioGuardado));
            
        } catch (Exception e) {
            logOperacionError("Error al crear usuario", e.getMessage());
            throw e;
        }
    }

    /**
     * Actualizar un usuario existente.
     * 
     * @param id ID del usuario a actualizar
     * @param usuarioDTO datos actualizados del usuario
     * @return usuario actualizado
     */
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioCreateDTO usuarioDTO) {
        
        logOperacionInicio("SuperAdmin actualiza usuario", String.valueOf(id));
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
                logOperacionError("Usuario no encontrado", String.valueOf(id));
                return new RuntimeException("Usuario no encontrado con ID: " + id);
            });
        
        // Validar username si cambió
        if (!usuario.getUsername().equals(usuarioDTO.getUsername()) && 
            usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
            logOperacionError("Username ya existe", usuarioDTO.getUsername());
            throw new IllegalArgumentException(
                "Ya existe un usuario con el username: " + usuarioDTO.getUsername()
            );
        }

        // Validar telefonoLocal según rol
        if (usuarioDTO.getRol() != Rol.ROLE_SUPERADMIN && 
            (usuarioDTO.getTelefonoLocal() == null || usuarioDTO.getTelefonoLocal().isBlank())) {
            throw new IllegalArgumentException(
                "El campo telefonoLocal es obligatorio para roles diferentes a SUPERADMIN"
            );
        }

        // Actualizar campos
        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setRol(usuarioDTO.getRol());
        
        // Actualizar password solo si se proporcionó uno nuevo
        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        }
        
        // Actualizar local
        if (usuarioDTO.getRol() != Rol.ROLE_SUPERADMIN) {
            Local local = localService.buscarPorTelefono(usuarioDTO.getTelefonoLocal());
            usuario.setLocal(local);
        } else {
            usuario.setLocal(null);
        }
        
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        
        logOperacionExito("Usuario actualizado", usuarioActualizado.getUsername());
        return ResponseEntity.ok(convertirUsuarioADTO(usuarioActualizado));
    }

    /**
     * Eliminar un usuario.
     * 
     * @param id ID del usuario a eliminar
     * @return respuesta sin contenido
     */
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        logOperacionInicio("SuperAdmin elimina usuario", String.valueOf(id));
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
                logOperacionError("Usuario no encontrado", String.valueOf(id));
                return new RuntimeException("Usuario no encontrado con ID: " + id);
            });
        
        String username = usuario.getUsername();
        usuarioRepository.delete(usuario);
        
        logOperacionExito("Usuario eliminado", username);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cambiar contraseña de un usuario.
     * 
     * @param id ID del usuario
     * @param newPassword nueva contraseña (en el body como plain text)
     * @return respuesta sin contenido
     */
    @PatchMapping("/usuarios/{id}/password")
    public ResponseEntity<Void> cambiarPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        logOperacionInicio("SuperAdmin cambia password de usuario", String.valueOf(id));
        
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");
        }
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
                logOperacionError("Usuario no encontrado", String.valueOf(id));
                return new RuntimeException("Usuario no encontrado con ID: " + id);
            });
        
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        
        logOperacionExito("Password cambiado", usuario.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    /**
     * Convierte una entidad Local a LocalDTO.
     * 
     * @param local entidad a convertir
     * @return DTO con los datos del local
     */
    private LocalDTO convertirLocalADTO(Local local) {
        return LocalDTO.builder()
            .id(local.getId())
            .nombre(local.getNombre())
            .direccion(local.getDireccion())
            .telefono(local.getTelefono())
            .planSuscripcion(local.getPlanSuscripcion())
            .planActivo(local.getPlanActivo())
            .fechaInicioPlan(local.getFechaInicioPlan())
            .fechaFinPlan(local.getFechaFinPlan())
            .build();
    }

    /**
     * Convierte una entidad Usuario a UsuarioDTO.
     * 
     * @param usuario entidad a convertir
     * @return DTO con los datos del usuario
     */
    private UsuarioDTO convertirUsuarioADTO(Usuario usuario) {
        UsuarioDTO.UsuarioDTOBuilder builder = UsuarioDTO.builder()
            .id(usuario.getId())
            .username(usuario.getUsername())
            .rol(usuario.getRol());
        
        // Agregar datos del local si existe
        if (usuario.getLocal() != null) {
            builder.localId(usuario.getLocal().getId())
                   .localNombre(usuario.getLocal().getNombre())
                   .telefonoLocal(usuario.getLocal().getTelefono());
        }
        
        return builder.build();
    }

    // ========================================
    // GESTIÓN DE PLANES DE SUSCRIPCIÓN (v2.2.0)
    // ========================================

    /**
     * Obtener información completa del plan de un local.
     * 
     * @param localId ID del local
     * @return información detallada del plan con features disponibles
     */
    @GetMapping("/locales/{localId}/plan")
    public ResponseEntity<PlanInfoDTO> obtenerPlanLocal(@PathVariable Long localId) {
        logOperacionInicio("SuperAdmin consulta plan de local", String.valueOf(localId));
        
        Local local = localRepository.findById(localId)
            .orElseThrow(() -> new RuntimeException("Local no encontrado con ID: " + localId));
        
        Set<Feature> features = planService.getFeaturesDisponibles(local);
        PlanInfoDTO planInfo = PlanInfoDTO.fromLocal(local, features);
        
        logOperacionExito("Plan obtenido", local.getNombre() + " - " + local.getPlanSuscripcion());
        return ResponseEntity.ok(planInfo);
    }

    /**
     * Cambiar el plan de suscripción de un local.
     * 
     * @param localId ID del local
     * @param cambiarPlanDTO datos del nuevo plan
     * @return información actualizada del plan
     */
    @PutMapping("/locales/{localId}/plan")
    public ResponseEntity<PlanInfoDTO> cambiarPlanLocal(
        @PathVariable Long localId,
        @Valid @RequestBody CambiarPlanDTO cambiarPlanDTO
    ) {
        logOperacionInicio("SuperAdmin cambia plan de local", 
            localId + " -> " + cambiarPlanDTO.getPlanSuscripcion());
        
        Local local = localRepository.findById(localId)
            .orElseThrow(() -> new RuntimeException("Local no encontrado con ID: " + localId));
        
        // Actualizar plan
        local.setPlanSuscripcion(cambiarPlanDTO.getPlanSuscripcion());
        local.setPlanActivo(cambiarPlanDTO.getPlanActivo());
        
        // Actualizar fechas
        if (cambiarPlanDTO.getFechaInicioPlan() != null) {
            local.setFechaInicioPlan(cambiarPlanDTO.getFechaInicioPlan());
        } else if (local.getFechaInicioPlan() == null) {
            // Si es la primera vez, usar fecha actual
            local.setFechaInicioPlan(LocalDate.now());
        }
        
        local.setFechaFinPlan(cambiarPlanDTO.getFechaFinPlan());
        
        // Guardar cambios
        localRepository.save(local);
        
        // Log del cambio (para auditoría)
        log.info("🔄 CAMBIO DE PLAN - Local: {} (ID: {}) | Nuevo Plan: {} | Activo: {} | Fin: {} | Motivo: {}",
            local.getNombre(),
            localId,
            cambiarPlanDTO.getPlanSuscripcion(),
            cambiarPlanDTO.getPlanActivo(),
            cambiarPlanDTO.getFechaFinPlan() != null ? cambiarPlanDTO.getFechaFinPlan() : "Indefinido",
            cambiarPlanDTO.getMotivoCambio() != null ? cambiarPlanDTO.getMotivoCambio() : "No especificado"
        );
        
        // Retornar información actualizada
        Set<Feature> features = planService.getFeaturesDisponibles(local);
        PlanInfoDTO planInfo = PlanInfoDTO.fromLocal(local, features);
        
        logOperacionExito("Plan cambiado exitosamente", local.getNombre());
        return ResponseEntity.ok(planInfo);
    }

    /**
     * Activar el plan de un local.
     * 
     * @param localId ID del local
     * @return información actualizada del plan
     */
    @PostMapping("/locales/{localId}/plan/activar")
    public ResponseEntity<PlanInfoDTO> activarPlanLocal(@PathVariable Long localId) {
        logOperacionInicio("SuperAdmin activa plan de local", String.valueOf(localId));
        
        Local local = localRepository.findById(localId)
            .orElseThrow(() -> new RuntimeException("Local no encontrado con ID: " + localId));
        
        local.setPlanActivo(true);
        localRepository.save(local);
        
        log.info("✅ PLAN ACTIVADO - Local: {} (ID: {})", local.getNombre(), localId);
        
        Set<Feature> features = planService.getFeaturesDisponibles(local);
        PlanInfoDTO planInfo = PlanInfoDTO.fromLocal(local, features);
        
        logOperacionExito("Plan activado", local.getNombre());
        return ResponseEntity.ok(planInfo);
    }

    /**
     * Desactivar el plan de un local (suspensión).
     * 
     * @param localId ID del local
     * @return información actualizada del plan
     */
    @PostMapping("/locales/{localId}/plan/desactivar")
    public ResponseEntity<PlanInfoDTO> desactivarPlanLocal(@PathVariable Long localId) {
        logOperacionInicio("SuperAdmin desactiva plan de local", String.valueOf(localId));
        
        Local local = localRepository.findById(localId)
            .orElseThrow(() -> new RuntimeException("Local no encontrado con ID: " + localId));
        
        local.setPlanActivo(false);
        localRepository.save(local);
        
        log.warn("⚠️ PLAN DESACTIVADO - Local: {} (ID: {})", local.getNombre(), localId);
        
        Set<Feature> features = planService.getFeaturesDisponibles(local);
        PlanInfoDTO planInfo = PlanInfoDTO.fromLocal(local, features);
        
        logOperacionExito("Plan desactivado", local.getNombre());
        return ResponseEntity.ok(planInfo);
    }

    /**
     * Obtener todos los planes disponibles con sus features.
     * 
     * @return mapa de planes con sus features
     */
    @GetMapping("/planes")
    public ResponseEntity<Map<String, Object>> listarPlanesDisponibles() {
        logOperacionInicio("SuperAdmin lista planes disponibles", null);
        
        Map<String, Object> response = Map.of(
            "planes", List.of(
                Map.of(
                    "id", PlanSuscripcion.BASICO,
                    "nombre", PlanSuscripcion.BASICO.getNombre(),
                    "descripcion", PlanSuscripcion.BASICO.getDescripcionCorta(),
                    "precio", PlanSuscripcion.BASICO.getPrecioMensualSugerido(),
                    "features", planService.getFeaturesDisponibles(createMockLocalWithPlan(PlanSuscripcion.BASICO))
                ),
                Map.of(
                    "id", PlanSuscripcion.ESTANDAR,
                    "nombre", PlanSuscripcion.ESTANDAR.getNombre(),
                    "descripcion", PlanSuscripcion.ESTANDAR.getDescripcionCorta(),
                    "precio", PlanSuscripcion.ESTANDAR.getPrecioMensualSugerido(),
                    "features", planService.getFeaturesDisponibles(createMockLocalWithPlan(PlanSuscripcion.ESTANDAR))
                ),
                Map.of(
                    "id", PlanSuscripcion.PREMIUM,
                    "nombre", PlanSuscripcion.PREMIUM.getNombre(),
                    "descripcion", PlanSuscripcion.PREMIUM.getDescripcionCorta(),
                    "precio", PlanSuscripcion.PREMIUM.getPrecioMensualSugerido(),
                    "features", planService.getFeaturesDisponibles(createMockLocalWithPlan(PlanSuscripcion.PREMIUM))
                )
            )
        );
        
        logOperacionExito("Planes listados", "3 planes disponibles");
        return ResponseEntity.ok(response);
    }

    /**
     * Crear un local mock temporal para consultar features de un plan.
     */
    private Local createMockLocalWithPlan(PlanSuscripcion plan) {
        return Local.builder()
            .planSuscripcion(plan)
            .planActivo(true)
            .build();
    }
}
