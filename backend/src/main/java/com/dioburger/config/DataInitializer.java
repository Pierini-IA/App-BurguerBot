package com.dioburger.config;

import com.dioburger.model.entity.Categoria;
import com.dioburger.model.entity.ConfiguracionLocal;
import com.dioburger.model.entity.Extra;
import com.dioburger.model.entity.Ingrediente;
import com.dioburger.model.entity.Local;
import com.dioburger.model.entity.Mesa;
import com.dioburger.model.entity.Producto;
import com.dioburger.model.entity.ProductoExtra;
import com.dioburger.model.entity.Receta;
import com.dioburger.model.entity.Usuario;
import com.dioburger.model.enums.PlanSuscripcion;
import com.dioburger.model.enums.Rol;
import com.dioburger.model.enums.UnidadMedida;
import com.dioburger.repository.LocalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración para cargar datos iniciales desde data/initial-data.json
 * solo si la base de datos está vacía.
 * 
 * Carga dos locales ficticios con productos, ingredientes, mesas y usuarios.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final LocalRepository localRepository;
    private final com.dioburger.repository.UsuarioRepository usuarioRepository;
    private final com.dioburger.repository.ExtraRepository extraRepository;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Bean que se ejecuta al iniciar la aplicación.
     * Carga datos iniciales solo si la base de datos está vacía.
     * 
     * @return ApplicationRunner configurado
     */
    @Bean
    public ApplicationRunner initData() {
        return args -> {
            if (localRepository.count() == 0) {
                log.info("=== Base de datos vacía. Cargando datos iniciales... ===");
                cargarDatosIniciales();
                log.info("=== Datos iniciales cargados exitosamente ===");
            } else {
                log.info("=== Base de datos ya contiene datos. Omitiendo inicialización ===");
            }
        };
    }

    /**
     * Carga los datos iniciales desde el archivo JSON.
     * 
     * @throws Exception si hay error al leer o procesar el archivo
     */
    private void cargarDatosIniciales() throws Exception {
        InputStream inputStream = new ClassPathResource("data/initial-data.json").getInputStream();
        JsonNode root = objectMapper.readTree(inputStream);
        JsonNode localesNode = root.get("locales");

        for (JsonNode localNode : localesNode) {
            Local local = crearLocal(localNode);
            // saveAndFlush: garantiza que el Local (y su config/productos/etc. en cascada)
            // quede persistido antes de guardar usuarios y extras que lo referencian.
            local = localRepository.saveAndFlush(local);
            log.info("✓ Local creado: {} ({})", local.getNombre(), local.getTelefono());

            // Guardar usuarios del local
            JsonNode usuariosNode = localNode.get("usuarios");
            for (JsonNode usuarioNode : usuariosNode) {
                Usuario usuario = crearUsuario(usuarioNode, local);
                usuarioRepository.save(usuario);
                log.info("  ✓ Usuario creado: {} ({})", usuario.getUsername(), usuario.getRol());
            }

            // Guardar extras del local (si existen)
            if (localNode.has("extras")) {
                JsonNode extrasNode = localNode.get("extras");
                for (JsonNode extraNode : extrasNode) {
                    Extra extra = crearExtra(extraNode, local);
                    extraRepository.save(extra);
                    log.info("  ✓ Extra creado: {} (${}.00)", extra.getNombre(), extra.getPrecioAdicional());
                }
            }
        }
    }

    /**
     * Crea un local completo a partir de un nodo JSON.
     * Incluye configuración, ingredientes, productos, recetas, mesas y usuarios.
     * 
     * @param localNode nodo JSON del local
     * @return entidad Local completa
     */
    private Local crearLocal(JsonNode localNode) {
        // Crear local
        Local.LocalBuilder localBuilder = Local.builder()
            .nombre(localNode.get("nombre").asText())
            .direccion(localNode.get("direccion").asText())
            .telefono(localNode.get("telefono").asText())
            .ingredientes(new ArrayList<>())
            .productos(new ArrayList<>())
            .mesas(new ArrayList<>());
        
        // Agregar campos del plan de suscripción si existen
        if (localNode.has("planSuscripcion")) {
            localBuilder.planSuscripcion(
                PlanSuscripcion.valueOf(localNode.get("planSuscripcion").asText())
            );
        }
        if (localNode.has("planActivo")) {
            localBuilder.planActivo(localNode.get("planActivo").asBoolean());
        }
        if (localNode.has("fechaInicioPlan") && !localNode.get("fechaInicioPlan").isNull()) {
            localBuilder.fechaInicioPlan(java.time.LocalDate.parse(localNode.get("fechaInicioPlan").asText()));
        }
        if (localNode.has("fechaFinPlan") && !localNode.get("fechaFinPlan").isNull()) {
            localBuilder.fechaFinPlan(java.time.LocalDate.parse(localNode.get("fechaFinPlan").asText()));
        }
        
        Local local = localBuilder.build();

        // Crear configuración
        ConfiguracionLocal config = crearConfiguracion(localNode.get("configuracion"), local);
        local.setConfiguracion(config);

        // Crear ingredientes
        Map<String, Ingrediente> ingredientesMap = new HashMap<>();
        JsonNode ingredientesNode = localNode.get("ingredientes");
        for (JsonNode ingredienteNode : ingredientesNode) {
            Ingrediente ingrediente = crearIngrediente(ingredienteNode, local);
            local.getIngredientes().add(ingrediente);
            ingredientesMap.put(ingrediente.getNombre(), ingrediente);
        }

        // Crear productos con recetas
        JsonNode productosNode = localNode.get("productos");
        for (JsonNode productoNode : productosNode) {
            Producto producto = crearProducto(productoNode, local, ingredientesMap);
            local.getProductos().add(producto);
        }

        // Crear mesas (si existen)
        if (localNode.has("mesas")) {
            JsonNode mesasNode = localNode.get("mesas");
            for (JsonNode mesaNode : mesasNode) {
                Mesa mesa = crearMesa(mesaNode, local);
                local.getMesas().add(mesa);
            }
        }

        return local;
    }

    /**
     * Crea la configuración de un local.
     */
    private ConfiguracionLocal crearConfiguracion(JsonNode configNode, Local local) {
        return ConfiguracionLocal.builder()
            .local(local)
            .horaApertura(LocalTime.parse(configNode.get("horaApertura").asText()))
            .horaCierre(LocalTime.parse(configNode.get("horaCierre").asText()))
            .intervaloMinutosPedidos(configNode.get("intervaloMinutosPedidos").asInt())
            .maxPedidosPorIntervalo(configNode.get("maxPedidosPorIntervalo").asInt())
            .horaAperturaReservas(LocalTime.parse(configNode.get("horaAperturaReservas").asText()))
            .horaCierreReservas(LocalTime.parse(configNode.get("horaCierreReservas").asText()))
            .intervaloMinutosReservas(configNode.get("intervaloMinutosReservas").asInt())
            .maxReservasPorIntervalo(configNode.get("maxReservasPorIntervalo").asInt())
            .permiteDelivery(configNode.get("permiteDelivery").asBoolean())
            .permiteTakeAway(configNode.get("permiteTakeAway").asBoolean())
            .permiteReservas(configNode.get("permiteReservas").asBoolean())
            .impresionActiva(configNode.get("impresionActiva").asBoolean())
            .urlWebhookImpresora(
                configNode.has("urlWebhookImpresora") && !configNode.get("urlWebhookImpresora").isNull()
                    ? configNode.get("urlWebhookImpresora").asText()
                    : null
            )
            .build();
    }

    /**
     * Crea un ingrediente.
     */
    private Ingrediente crearIngrediente(JsonNode ingredienteNode, Local local) {
        return Ingrediente.builder()
            .local(local)
            .nombre(ingredienteNode.get("nombre").asText())
            .stockActual(new BigDecimal(ingredienteNode.get("stockActual").asText()))
            .unidadMedida(UnidadMedida.valueOf(ingredienteNode.get("unidadMedida").asText()))
            .build();
    }

    /**
     * Crea un producto con su receta.
     */
    private Producto crearProducto(JsonNode productoNode, Local local, Map<String, Ingrediente> ingredientesMap) {
        Producto producto = Producto.builder()
            .local(local)
            .nombre(productoNode.get("nombre").asText())
            .precio(new BigDecimal(productoNode.get("precio").asText()))
            .descripcion(productoNode.get("descripcion").asText())
            .estaAgotado(false)
            .recetas(new ArrayList<>())
            .build();

        // Crear recetas
        JsonNode recetaNode = productoNode.get("receta");
        for (JsonNode itemReceta : recetaNode) {
            String nombreIngrediente = itemReceta.get("ingrediente").asText();
            Ingrediente ingrediente = ingredientesMap.get(nombreIngrediente);
            
            Receta receta = Receta.builder()
                .producto(producto)
                .ingrediente(ingrediente)
                .cantidadRequerida(new BigDecimal(itemReceta.get("cantidad").asText()))
                .build();
            
            producto.getRecetas().add(receta);
        }

        return producto;
    }

    /**
     * Crea una mesa.
     */
    private Mesa crearMesa(JsonNode mesaNode, Local local) {
        return Mesa.builder()
            .local(local)
            .numero(mesaNode.get("numero").asInt())
            .capacidad(mesaNode.get("capacidad").asInt())
            .disponible(true)
            .build();
    }

    /**
     * Crea un usuario (no se añade a la colección del local, se guarda independiente).
     * SUPERADMIN no tiene local asociado (null).
     */
    private Usuario crearUsuario(JsonNode usuarioNode, Local local) {
        String passwordPlain = usuarioNode.get("password").asText();
        String passwordHashed = passwordEncoder.encode(passwordPlain);
        Rol rol = Rol.valueOf(usuarioNode.get("rol").asText());
        
        return Usuario.builder()
            .local(rol == Rol.ROLE_SUPERADMIN ? null : local)
            .username(usuarioNode.get("username").asText())
            .password(passwordHashed)
            .rol(rol)
            .build();
    }
    
    /**
     * Crea un extra (adicional) para un local.
     */
    private Extra crearExtra(JsonNode extraNode, Local local) {
        return Extra.builder()
            .nombre(extraNode.get("nombre").asText())
            .descripcion(extraNode.has("descripcion") ? extraNode.get("descripcion").asText() : null)
            .precioAdicional(new BigDecimal(extraNode.get("precioAdicional").asText()))
            .local(local)
            .activo(true)
            .build();
    }
}
