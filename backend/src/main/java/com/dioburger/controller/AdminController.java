package com.dioburger.controller;

import com.dioburger.model.dto.CategoriaDTO;
import com.dioburger.model.dto.ExtraDTO;
import com.dioburger.model.dto.LocalDTO;
import com.dioburger.model.dto.UsuarioCreateDTO;
import com.dioburger.model.entity.*;
import com.dioburger.model.enums.Rol;
import com.dioburger.repository.*;
import com.dioburger.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones administrativas.
 * Solo accesible para usuarios con rol ROLE_ADMIN.
 * 
 * Endpoints:
 * - GET /api/admin/productos - Listar productos
 * - POST /api/admin/productos - Crear producto
 * - PUT /api/admin/productos/{id} - Actualizar producto
 * - DELETE /api/admin/productos/{id} - Eliminar producto
 * - PUT /api/admin/ingredientes/{id}/stock - Actualizar stock
 * - GET /api/admin/mesas - Listar mesas
 * - POST /api/admin/mesas - Crear mesa
 * 
 * @author Dio Burger Team
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
public class AdminController extends BaseController {

    private final ProductoRepository productoRepository;
    private final IngredienteRepository ingredienteRepository;
    private final MesaRepository mesaRepository;
    private final LocalRepository localRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ExtraRepository extraRepository;
    private final StockService stockService;
    private final PasswordEncoder passwordEncoder;

    // ==================== PRODUCTOS ====================

    /**
     * Lista todos los productos de un local.
     * 
     * @param telefonoLocal Teléfono del local
     * @return Lista de productos
     */
    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> listarProductos(
            @RequestParam String telefonoLocal) {
        
        log.info("👨‍💼 Admin lista productos del local: {}", telefonoLocal);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Producto> productos = productoRepository.findByLocal(local);

        log.info("✅ {} productos encontrados", productos.size());

        return ResponseEntity.ok(productos);
    }

    /**
     * Crea un nuevo producto.
     * 
     * @param telefonoLocal Teléfono del local
     * @param producto Datos del producto
     * @return Producto creado
     */
    @PostMapping("/productos")
    public ResponseEntity<Producto> crearProducto(
            @RequestParam String telefonoLocal,
            @RequestBody @Valid Producto producto) {
        
        log.info("👨‍💼 Admin crea producto: {} en local {}", 
                producto.getNombre(), telefonoLocal);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        producto.setLocal(local);
        producto.setEstaAgotado(false);
        resolverCategoria(producto, producto.getCategoria());
        reemplazarReceta(producto, producto.getRecetas() != null
                ? new ArrayList<>(producto.getRecetas())
                : new ArrayList<>());

        Producto productoGuardado = productoRepository.save(producto);

        // Ajustar disponibilidad según el stock de los ingredientes de la receta.
        stockService.actualizarDisponibilidadProducto(productoGuardado);

        log.info("✅ Producto creado con ID: {}", productoGuardado.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(productoGuardado);
    }

    /**
     * Actualiza un producto existente.
     * 
     * @param id ID del producto
     * @param producto Datos actualizados
     * @return Producto actualizado
     */
    @PutMapping("/productos/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable Long id,
            @RequestBody @Valid Producto producto) {
        
        log.info("👨‍💼 Admin actualiza producto ID: {}", id);

        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        productoExistente.setNombre(producto.getNombre());
        productoExistente.setDescripcion(producto.getDescripcion());
        productoExistente.setPrecio(producto.getPrecio());

        // Categoría y receta se actualizan solo si vienen en el body.
        if (producto.getCategoria() != null) {
            resolverCategoria(productoExistente, producto.getCategoria());
        }
        if (producto.getRecetas() != null) {
            reemplazarReceta(productoExistente, new ArrayList<>(producto.getRecetas()));
        }

        Producto productoActualizado = productoRepository.save(productoExistente);

        // Actualizar disponibilidad basada en stock
        stockService.actualizarDisponibilidadProducto(productoActualizado);

        log.info("✅ Producto actualizado: {}", productoActualizado.getNombre());

        return ResponseEntity.ok(productoActualizado);
    }

    /**
     * Elimina un producto.
     * 
     * @param id ID del producto
     * @return Response vacía
     */
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        
        log.info("👨‍💼 Admin elimina producto ID: {}", id);

        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado");
        }

        productoRepository.deleteById(id);

        log.info("✅ Producto eliminado");

        return ResponseEntity.noContent().build();
    }

    /**
     * Alterna la disponibilidad de un producto (agotado / disponible).
     * Marca manual: la usa el panel para "cortar" un producto sin borrarlo.
     *
     * @param id ID del producto
     * @return Producto con el nuevo valor de {@code estaAgotado}
     */
    @PatchMapping("/productos/{id}/disponibilidad")
    public ResponseEntity<Producto> cambiarDisponibilidadProducto(@PathVariable Long id) {

        log.info("👨‍💼 Admin alterna disponibilidad del producto ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        boolean nuevoEstadoAgotado = !Boolean.TRUE.equals(producto.getEstaAgotado());
        producto.setEstaAgotado(nuevoEstadoAgotado);

        Producto actualizado = productoRepository.save(producto);

        log.info("✅ Producto '{}' ahora está {}",
                actualizado.getNombre(), nuevoEstadoAgotado ? "AGOTADO" : "DISPONIBLE");

        return ResponseEntity.ok(actualizado);
    }

    // ==================== INGREDIENTES ====================

    /**
     * Lista todos los ingredientes de un local.
     * 
     * @param telefonoLocal Teléfono del local
     * @return Lista de ingredientes
     */
    @GetMapping("/ingredientes")
    public ResponseEntity<List<Ingrediente>> listarIngredientes(
            @RequestParam String telefonoLocal) {
        
        log.info("👨‍💼 Admin lista ingredientes del local: {}", telefonoLocal);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Ingrediente> ingredientes = ingredienteRepository.findByLocal(local);

        log.info("✅ {} ingredientes encontrados", ingredientes.size());

        return ResponseEntity.ok(ingredientes);
    }

    /**
     * Actualiza el stock de un ingrediente.
     * 
     * @param id ID del ingrediente
     * @param nuevoStock Nuevo valor de stock
     * @return Ingrediente actualizado
     */
    @PutMapping("/ingredientes/{id}/stock")
    public ResponseEntity<Ingrediente> actualizarStock(
            @PathVariable Long id,
            @RequestParam BigDecimal nuevoStock) {
        
        log.info("👨‍💼 Admin actualiza stock del ingrediente ID: {} a {}", id, nuevoStock);

        Ingrediente ingrediente = ingredienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

        BigDecimal stockAnterior = ingrediente.getStockActual();
        ingrediente.setStockActual(nuevoStock);

        Ingrediente ingredienteActualizado = ingredienteRepository.save(ingrediente);

        log.info("✅ Stock actualizado de {} a {} para ingrediente: {}", 
                stockAnterior, nuevoStock, ingrediente.getNombre());

        // Actualizar disponibilidad de productos que usan este ingrediente
        stockService.actualizarDisponibilidadProductos(ingrediente.getLocal());

        return ResponseEntity.ok(ingredienteActualizado);
    }

    /**
     * Crea un nuevo ingrediente.
     * 
     * @param telefonoLocal Teléfono del local
     * @param ingrediente Datos del ingrediente
     * @return Ingrediente creado
     */
    @PostMapping("/ingredientes")
    public ResponseEntity<Ingrediente> crearIngrediente(
            @RequestParam String telefonoLocal,
            @RequestBody @Valid Ingrediente ingrediente) {
        
        log.info("👨‍💼 Admin crea ingrediente: {} en local {}", 
                ingrediente.getNombre(), telefonoLocal);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        ingrediente.setLocal(local);

        Ingrediente ingredienteGuardado = ingredienteRepository.save(ingrediente);

        log.info("✅ Ingrediente creado con ID: {}", ingredienteGuardado.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ingredienteGuardado);
    }

    /**
     * Actualiza los datos de un ingrediente (nombre, stock, unidad de medida).
     * Para ajustar únicamente el stock existe {@code PUT /ingredientes/{id}/stock}.
     *
     * @param id ID del ingrediente
     * @param ingrediente Datos actualizados
     * @return Ingrediente actualizado
     */
    @PutMapping("/ingredientes/{id}")
    public ResponseEntity<Ingrediente> actualizarIngrediente(
            @PathVariable Long id,
            @RequestBody @Valid Ingrediente ingrediente) {

        log.info("👨‍💼 Admin actualiza ingrediente ID: {}", id);

        Ingrediente existente = ingredienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

        existente.setNombre(ingrediente.getNombre());
        if (ingrediente.getStockActual() != null) {
            existente.setStockActual(ingrediente.getStockActual());
        }
        if (ingrediente.getUnidadMedida() != null) {
            existente.setUnidadMedida(ingrediente.getUnidadMedida());
        }

        Ingrediente actualizado = ingredienteRepository.save(existente);

        // El cambio de stock puede afectar la disponibilidad de productos que lo usan.
        stockService.actualizarDisponibilidadProductos(actualizado.getLocal());

        log.info("✅ Ingrediente actualizado: {}", actualizado.getNombre());

        return ResponseEntity.ok(actualizado);
    }

    /**
     * Elimina un ingrediente.
     * Falla si el ingrediente está siendo usado en la receta de algún producto.
     *
     * @param id ID del ingrediente
     * @return Response vacía
     */
    @DeleteMapping("/ingredientes/{id}")
    public ResponseEntity<Void> eliminarIngrediente(@PathVariable Long id) {

        log.info("👨‍💼 Admin elimina ingrediente ID: {}", id);

        if (!ingredienteRepository.existsById(id)) {
            throw new RuntimeException("Ingrediente no encontrado");
        }

        ingredienteRepository.deleteById(id);

        log.info("✅ Ingrediente eliminado");

        return ResponseEntity.noContent().build();
    }

    // ==================== MESAS ====================

    /**
     * Lista todas las mesas de un local.
     * 
     * @param telefonoLocal Teléfono del local
     * @return Lista de mesas
     */
    @GetMapping("/mesas")
    public ResponseEntity<List<Mesa>> listarMesas(
            @RequestParam String telefonoLocal) {
        
        log.info("👨‍💼 Admin lista mesas del local: {}", telefonoLocal);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Mesa> mesas = mesaRepository.findByLocal(local);

        log.info("✅ {} mesas encontradas", mesas.size());

        return ResponseEntity.ok(mesas);
    }

    /**
     * Crea una nueva mesa.
     * 
     * @param telefonoLocal Teléfono del local
     * @param mesa Datos de la mesa
     * @return Mesa creada
     */
    @PostMapping("/mesas")
    public ResponseEntity<Mesa> crearMesa(
            @RequestParam String telefonoLocal,
            @RequestBody @Valid Mesa mesa) {
        
        log.info("👨‍💼 Admin crea mesa #{} en local {}", 
                mesa.getNumero(), telefonoLocal);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        mesa.setLocal(local);
        mesa.setDisponible(true);

        Mesa mesaGuardada = mesaRepository.save(mesa);

        log.info("✅ Mesa creada con ID: {}", mesaGuardada.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(mesaGuardada);
    }

    /**
     * Actualiza una mesa existente.
     * 
     * @param id ID de la mesa
     * @param mesa Datos actualizados
     * @return Mesa actualizada
     */
    @PutMapping("/mesas/{id}")
    public ResponseEntity<Mesa> actualizarMesa(
            @PathVariable Long id,
            @RequestBody @Valid Mesa mesa) {
        
        log.info("👨‍💼 Admin actualiza mesa ID: {}", id);

        Mesa mesaExistente = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        mesaExistente.setNumero(mesa.getNumero());
        mesaExistente.setCapacidad(mesa.getCapacidad());
        mesaExistente.setDisponible(mesa.getDisponible());

        Mesa mesaActualizada = mesaRepository.save(mesaExistente);

        log.info("✅ Mesa actualizada: #{}", mesaActualizada.getNumero());

        return ResponseEntity.ok(mesaActualizada);
    }

    /**
     * Elimina una mesa.
     * 
     * @param id ID de la mesa
     * @return Response vacía
     */
    @DeleteMapping("/mesas/{id}")
    public ResponseEntity<Void> eliminarMesa(@PathVariable Long id) {
        
        log.info("👨‍💼 Admin elimina mesa ID: {}", id);

        if (!mesaRepository.existsById(id)) {
            throw new RuntimeException("Mesa no encontrada");
        }

        mesaRepository.deleteById(id);

        log.info("✅ Mesa eliminada");

        return ResponseEntity.noContent().build();
    }

    // ==================== ENDPOINTS SUPER ADMIN ====================

    /**
     * Crea un nuevo local (solo SUPERADMIN).
     * Este endpoint permite crear locales de la red de hamburgueserías.
     * 
     * @param localDTO Datos del nuevo local
     * @return Local creado
     */
    @PostMapping("/locales")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Local> crearLocal(@Valid @RequestBody LocalDTO localDTO) {
        
        log.info("🔧 SUPERADMIN crea nuevo local: {}", localDTO.getNombre());

        // Validar que el teléfono no exista
        if (localRepository.existsByTelefono(localDTO.getTelefono())) {
            throw new IllegalArgumentException(
                "Ya existe un local con el teléfono: " + localDTO.getTelefono()
            );
        }

        // Crear el local
        Local local = new Local();
        local.setNombre(localDTO.getNombre());
        local.setDireccion(localDTO.getDireccion());
        local.setTelefono(localDTO.getTelefono());

        // Crear configuración por defecto
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

        local.setConfiguracion(config);
        config.setLocal(local);

        Local saved = localRepository.save(local);

        log.info("✅ Local creado: {} - ID: {}, Teléfono: {}", 
            saved.getNombre(), saved.getId(), saved.getTelefono());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Lista TODOS los locales de la red (solo SUPERADMIN).
     * 
     * @return Lista de todos los locales
     */
    @GetMapping("/locales")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<Local>> listarTodosLosLocales() {
        
        log.info("🔧 SUPERADMIN lista todos los locales");

        List<Local> locales = localRepository.findAll();

        log.info("✅ Encontrados {} locales", locales.size());

        return ResponseEntity.ok(locales);
    }

    /**
     * Obtiene un local específico por ID (solo SUPERADMIN).
     * 
     * @param id ID del local
     * @return Local encontrado
     */
    @GetMapping("/locales/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Local> obtenerLocalPorId(@PathVariable Long id) {
        
        log.info("🔧 SUPERADMIN obtiene local ID: {}", id);

        Local local = localRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Local no encontrado con ID: " + id));

        return ResponseEntity.ok(local);
    }

    /**
     * Actualiza un local existente (solo SUPERADMIN).
     * 
     * @param id ID del local
     * @param localDTO Nuevos datos del local
     * @return Local actualizado
     */
    @PutMapping("/locales/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Local> actualizarLocal(
            @PathVariable Long id,
            @Valid @RequestBody LocalDTO localDTO) {
        
        log.info("🔧 SUPERADMIN actualiza local ID: {}", id);

        Local local = localRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Local no encontrado con ID: " + id));

        // Actualizar campos
        local.setNombre(localDTO.getNombre());
        local.setDireccion(localDTO.getDireccion());

        // Solo actualizar teléfono si cambió y no existe otro local con ese teléfono
        if (!local.getTelefono().equals(localDTO.getTelefono())) {
            if (localRepository.existsByTelefono(localDTO.getTelefono())) {
                throw new IllegalArgumentException(
                    "Ya existe un local con el teléfono: " + localDTO.getTelefono()
                );
            }
            local.setTelefono(localDTO.getTelefono());
        }

        Local updated = localRepository.save(local);

        log.info("✅ Local actualizado: {}", updated.getNombre());

        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina un local (solo SUPERADMIN).
     * ⚠️ CUIDADO: Esto eliminará en cascada todos los datos del local.
     * 
     * @param id ID del local
     * @return Response vacía
     */
    @DeleteMapping("/locales/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Void> eliminarLocal(@PathVariable Long id) {
        
        log.warn("⚠️ SUPERADMIN elimina local ID: {}", id);

        if (!localRepository.existsById(id)) {
            throw new RuntimeException("Local no encontrado con ID: " + id);
        }

        localRepository.deleteById(id);

        log.info("✅ Local eliminado (ID: {})", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Crea un nuevo usuario para un local (solo SUPERADMIN).
     * Permite crear admins y personal de cocina para cada local.
     * 
     * @param usuarioDTO Datos del nuevo usuario
     * @return Usuario creado
     */
    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Usuario> crearUsuario(@Valid @RequestBody UsuarioCreateDTO usuarioDTO) {
        
        log.info("🔧 SUPERADMIN crea usuario: {} con rol: {}", 
            usuarioDTO.getUsername(), usuarioDTO.getRol());

        // Validar que el username no exista
        if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
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

        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        usuario.setRol(usuarioDTO.getRol());

        // Asociar al local si no es SUPERADMIN
        if (usuarioDTO.getRol() != Rol.ROLE_SUPERADMIN) {
            Local local = localService.buscarPorTelefono(usuarioDTO.getTelefonoLocal());
            usuario.setLocal(local);
        }

        Usuario saved = usuarioRepository.save(usuario);

        log.info("✅ Usuario creado: {} (ID: {})", saved.getUsername(), saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Lista todos los usuarios del sistema (solo SUPERADMIN).
     * 
     * @return Lista de todos los usuarios
     */
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<Usuario>> listarTodosLosUsuarios() {
        
        log.info("🔧 SUPERADMIN lista todos los usuarios");

        List<Usuario> usuarios = usuarioRepository.findAll();

        log.info("✅ Encontrados {} usuarios", usuarios.size());

        return ResponseEntity.ok(usuarios);
    }

    // ==================== CATEGORÍAS ====================

    /**
     * Lista todas las categorías de un local.
     * 
     * @param telefonoLocal Teléfono del local
     * @param soloActivas Si true, solo retorna categorías activas
     * @return Lista de categorías
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaDTO>> listarCategorias(
            @RequestParam String telefonoLocal,
            @RequestParam(defaultValue = "false") Boolean soloActivas) {
        
        log.info("📂 Admin lista categorías del local: {} (soloActivas: {})", telefonoLocal, soloActivas);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Categoria> categorias = soloActivas 
            ? categoriaRepository.findByLocalAndActivoOrderByOrdenAsc(local, true)
            : categoriaRepository.findByLocalOrderByOrdenAsc(local);

        List<CategoriaDTO> categoriasDTO = categorias.stream()
            .map(this::convertirCategoriaADTO)
            .collect(Collectors.toList());

        log.info("✅ {} categorías encontradas", categoriasDTO.size());

        return ResponseEntity.ok(categoriasDTO);
    }

    /**
     * Obtiene una categoría por ID.
     * 
     * @param id ID de la categoría
     * @return Categoría encontrada
     */
    @GetMapping("/categorias/{id}")
    public ResponseEntity<CategoriaDTO> obtenerCategoria(@PathVariable Long id) {
        
        log.info("📂 Admin obtiene categoría ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        CategoriaDTO dto = convertirCategoriaADTO(categoria);

        log.info("✅ Categoría encontrada: {}", categoria.getNombre());

        return ResponseEntity.ok(dto);
    }

    /**
     * Crea una nueva categoría.
     * 
     * @param telefonoLocal Teléfono del local
     * @param categoriaDTO Datos de la categoría
     * @return Categoría creada
     */
    @PostMapping("/categorias")
    public ResponseEntity<CategoriaDTO> crearCategoria(
            @RequestParam String telefonoLocal,
            @RequestBody @Valid CategoriaDTO categoriaDTO) {
        
        log.info("📂 Admin crea categoría: {} en local {}", categoriaDTO.getNombre(), telefonoLocal);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        // Validar que no exista una categoría con el mismo nombre en el local
        if (categoriaRepository.existsByNombreAndLocal(categoriaDTO.getNombre(), local)) {
            throw new IllegalArgumentException(
                "Ya existe una categoría con el nombre: " + categoriaDTO.getNombre()
            );
        }

        Categoria categoria = Categoria.builder()
            .nombre(categoriaDTO.getNombre())
            .descripcion(categoriaDTO.getDescripcion())
            .orden(categoriaDTO.getOrden() != null ? categoriaDTO.getOrden() : 0)
            .activo(categoriaDTO.getActivo() != null ? categoriaDTO.getActivo() : true)
            .local(local)
            .build();

        Categoria saved = categoriaRepository.save(categoria);

        log.info("✅ Categoría creada: {} (ID: {})", saved.getNombre(), saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(convertirCategoriaADTO(saved));
    }

    /**
     * Actualiza una categoría existente.
     * 
     * @param id ID de la categoría
     * @param categoriaDTO Datos actualizados
     * @return Categoría actualizada
     */
    @PutMapping("/categorias/{id}")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(
            @PathVariable Long id,
            @RequestBody @Valid CategoriaDTO categoriaDTO) {
        
        log.info("📂 Admin actualiza categoría ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // Validar nombre único (si cambió)
        if (!categoria.getNombre().equals(categoriaDTO.getNombre())) {
            if (categoriaRepository.existsByNombreAndLocal(categoriaDTO.getNombre(), categoria.getLocal())) {
                throw new IllegalArgumentException(
                    "Ya existe una categoría con el nombre: " + categoriaDTO.getNombre()
                );
            }
        }

        categoria.setNombre(categoriaDTO.getNombre());
        categoria.setDescripcion(categoriaDTO.getDescripcion());
        categoria.setOrden(categoriaDTO.getOrden());
        categoria.setActivo(categoriaDTO.getActivo());

        Categoria updated = categoriaRepository.save(categoria);

        log.info("✅ Categoría actualizada: {}", updated.getNombre());

        return ResponseEntity.ok(convertirCategoriaADTO(updated));
    }

    /**
     * Elimina una categoría.
     * 
     * @param id ID de la categoría
     * @return Respuesta vacía
     */
    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        
        log.info("📂 Admin elimina categoría ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        categoriaRepository.delete(categoria);

        log.info("✅ Categoría eliminada: {}", categoria.getNombre());

        return ResponseEntity.noContent().build();
    }

    // ==================== EXTRAS ====================

    /**
     * Lista todos los extras de un local.
     * 
     * @param telefonoLocal Teléfono del local
     * @param soloActivos Si true, solo retorna extras activos
     * @param categoriaId Filtrar por categoría (opcional)
     * @return Lista de extras
     */
    @GetMapping("/extras")
    public ResponseEntity<List<ExtraDTO>> listarExtras(
            @RequestParam String telefonoLocal,
            @RequestParam(defaultValue = "false") Boolean soloActivos,
            @RequestParam(required = false) Long categoriaId) {
        
        log.info("🍟 Admin lista extras del local: {} (soloActivos: {}, categoriaId: {})", 
            telefonoLocal, soloActivos, categoriaId);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        List<Extra> extras;

        if (categoriaId != null) {
            Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + categoriaId));
            
            extras = soloActivos 
                ? extraRepository.findByCategoriaAndActivo(categoria, true)
                : extraRepository.findByCategoria(categoria);
        } else {
            extras = soloActivos 
                ? extraRepository.findByLocalAndActivo(local, true)
                : extraRepository.findByLocal(local);
        }

        List<ExtraDTO> extrasDTO = extras.stream()
            .map(this::convertirExtraADTO)
            .collect(Collectors.toList());

        log.info("✅ {} extras encontrados", extrasDTO.size());

        return ResponseEntity.ok(extrasDTO);
    }

    /**
     * Obtiene un extra por ID.
     * 
     * @param id ID del extra
     * @return Extra encontrado
     */
    @GetMapping("/extras/{id}")
    public ResponseEntity<ExtraDTO> obtenerExtra(@PathVariable Long id) {
        
        log.info("🍟 Admin obtiene extra ID: {}", id);

        Extra extra = extraRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Extra no encontrado con ID: " + id));

        ExtraDTO dto = convertirExtraADTO(extra);

        log.info("✅ Extra encontrado: {}", extra.getNombre());

        return ResponseEntity.ok(dto);
    }

    /**
     * Crea un nuevo extra.
     * 
     * @param telefonoLocal Teléfono del local
     * @param extraDTO Datos del extra
     * @return Extra creado
     */
    @PostMapping("/extras")
    public ResponseEntity<ExtraDTO> crearExtra(
            @RequestParam String telefonoLocal,
            @RequestBody @Valid ExtraDTO extraDTO) {
        
        log.info("🍟 Admin crea extra: {} en local {}", extraDTO.getNombre(), telefonoLocal);

        Local local = localService.buscarPorTelefono(telefonoLocal);

        // Validar que no exista un extra con el mismo nombre en el local
        if (extraRepository.existsByNombreAndLocal(extraDTO.getNombre(), local)) {
            throw new IllegalArgumentException(
                "Ya existe un extra con el nombre: " + extraDTO.getNombre()
            );
        }

        Extra extra = Extra.builder()
            .nombre(extraDTO.getNombre())
            .descripcion(extraDTO.getDescripcion())
            .precioAdicional(extraDTO.getPrecioAdicional())
            .activo(extraDTO.getActivo() != null ? extraDTO.getActivo() : true)
            .local(local)
            .build();

        // Asociar a categoría si se especifica
        if (extraDTO.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(extraDTO.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + extraDTO.getCategoriaId()));
            extra.setCategoria(categoria);
        }

        Extra saved = extraRepository.save(extra);

        log.info("✅ Extra creado: {} (ID: {}) - Precio: ${}", 
            saved.getNombre(), saved.getId(), saved.getPrecioAdicional());

        return ResponseEntity.status(HttpStatus.CREATED).body(convertirExtraADTO(saved));
    }

    /**
     * Actualiza un extra existente.
     * 
     * @param id ID del extra
     * @param extraDTO Datos actualizados
     * @return Extra actualizado
     */
    @PutMapping("/extras/{id}")
    public ResponseEntity<ExtraDTO> actualizarExtra(
            @PathVariable Long id,
            @RequestBody @Valid ExtraDTO extraDTO) {
        
        log.info("🍟 Admin actualiza extra ID: {}", id);

        Extra extra = extraRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Extra no encontrado con ID: " + id));

        // Validar nombre único (si cambió)
        if (!extra.getNombre().equals(extraDTO.getNombre())) {
            if (extraRepository.existsByNombreAndLocal(extraDTO.getNombre(), extra.getLocal())) {
                throw new IllegalArgumentException(
                    "Ya existe un extra con el nombre: " + extraDTO.getNombre()
                );
            }
        }

        extra.setNombre(extraDTO.getNombre());
        extra.setDescripcion(extraDTO.getDescripcion());
        extra.setPrecioAdicional(extraDTO.getPrecioAdicional());
        extra.setActivo(extraDTO.getActivo());

        // Actualizar categoría
        if (extraDTO.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(extraDTO.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + extraDTO.getCategoriaId()));
            extra.setCategoria(categoria);
        } else {
            extra.setCategoria(null);
        }

        Extra updated = extraRepository.save(extra);

        log.info("✅ Extra actualizado: {} - Precio: ${}", updated.getNombre(), updated.getPrecioAdicional());

        return ResponseEntity.ok(convertirExtraADTO(updated));
    }

    /**
     * Elimina un extra.
     * 
     * @param id ID del extra
     * @return Respuesta vacía
     */
    @DeleteMapping("/extras/{id}")
    public ResponseEntity<Void> eliminarExtra(@PathVariable Long id) {
        
        log.info("🍟 Admin elimina extra ID: {}", id);

        Extra extra = extraRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Extra no encontrado con ID: " + id));

        extraRepository.delete(extra);

        log.info("✅ Extra eliminado: {}", extra.getNombre());

        return ResponseEntity.noContent().build();
    }

    /**
     * Lista los extras disponibles para un producto específico.
     * 
     * @param productoId ID del producto
     * @return Lista de extras del producto
     */
    @GetMapping("/productos/{productoId}/extras")
    public ResponseEntity<List<ExtraDTO>> listarExtrasDeProducto(@PathVariable Long productoId) {
        
        log.info("🍟 Admin lista extras del producto ID: {}", productoId);

        List<Extra> extras = extraRepository.findExtrasDisponiblesParaProducto(productoId);

        List<ExtraDTO> extrasDTO = extras.stream()
            .map(this::convertirExtraADTO)
            .collect(Collectors.toList());

        log.info("✅ {} extras encontrados para el producto", extrasDTO.size());

        return ResponseEntity.ok(extrasDTO);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Asocia la categoría al producto resolviéndola por ID.
     * Si {@code categoriaEntrada} es null o sin ID, deja el producto sin categoría.
     *
     * @param producto        producto destino
     * @param categoriaEntrada categoría del body (se usa solo su ID)
     */
    private void resolverCategoria(Producto producto, Categoria categoriaEntrada) {
        if (categoriaEntrada == null || categoriaEntrada.getId() == null) {
            producto.setCategoria(null);
            return;
        }
        Categoria categoria = categoriaRepository.findById(categoriaEntrada.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría no encontrada con ID: " + categoriaEntrada.getId()));
        producto.setCategoria(categoria);
    }

    /**
     * Reemplaza por completo la receta de un producto.
     * Cada item del body aporta solo el ID del ingrediente y la cantidad requerida;
     * el ingrediente se resuelve contra la base y la relación inversa se completa acá.
     * Mutar la colección existente (en vez de reemplazarla) mantiene el orphanRemoval.
     *
     * @param producto producto destino
     * @param entrada  items de receta del body
     */
    private void reemplazarReceta(Producto producto, List<Receta> entrada) {
        // En creación (no-args + @Builder.Default de Lombok) la lista llega null.
        if (producto.getRecetas() == null) {
            producto.setRecetas(new ArrayList<>());
        } else {
            producto.getRecetas().clear();
        }
        if (entrada == null) {
            return;
        }
        for (Receta item : entrada) {
            if (item == null || item.getIngrediente() == null || item.getIngrediente().getId() == null) {
                continue;
            }
            Ingrediente ingrediente = ingredienteRepository.findById(item.getIngrediente().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ingrediente no encontrado con ID: " + item.getIngrediente().getId()));

            Receta receta = Receta.builder()
                    .producto(producto)
                    .ingrediente(ingrediente)
                    .cantidadRequerida(item.getCantidadRequerida() != null
                            ? item.getCantidadRequerida()
                            : BigDecimal.ZERO)
                    .build();
            producto.getRecetas().add(receta);
        }
    }

    /**
     * Convierte una entidad Categoria a CategoriaDTO.
     * 
     * @param categoria Entidad Categoria
     * @return CategoriaDTO
     */
    private CategoriaDTO convertirCategoriaADTO(Categoria categoria) {
        return CategoriaDTO.builder()
            .id(categoria.getId())
            .nombre(categoria.getNombre())
            .descripcion(categoria.getDescripcion())
            .orden(categoria.getOrden())
            .activo(categoria.getActivo())
            .localId(categoria.getLocal().getId())
            .localNombre(categoria.getLocal().getNombre())
            .cantidadProductos(categoria.getProductos() != null ? categoria.getProductos().size() : 0)
            .cantidadExtras(categoria.getExtras() != null ? categoria.getExtras().size() : 0)
            .build();
    }

    /**
     * Convierte una entidad Extra a ExtraDTO.
     * 
     * @param extra Entidad Extra
     * @return ExtraDTO
     */
    private ExtraDTO convertirExtraADTO(Extra extra) {
        return ExtraDTO.builder()
            .id(extra.getId())
            .nombre(extra.getNombre())
            .descripcion(extra.getDescripcion())
            .precioAdicional(extra.getPrecioAdicional())
            .activo(extra.getActivo())
            .localId(extra.getLocal().getId())
            .localNombre(extra.getLocal().getNombre())
            .categoriaId(extra.getCategoria() != null ? extra.getCategoria().getId() : null)
            .categoriaNombre(extra.getCategoria() != null ? extra.getCategoria().getNombre() : null)
            .build();
    }
}

