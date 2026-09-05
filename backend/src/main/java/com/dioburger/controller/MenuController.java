package com.dioburger.controller;

import com.dioburger.model.dto.MenuCompletoDTO;
import com.dioburger.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador público para consulta del menú del local.
 * No requiere autenticación - usado por bots y clientes.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Menú Público", description = "Endpoints públicos para consultar el catálogo de productos")
@CrossOrigin(origins = "*")
public class MenuController {

    private final CatalogoService catalogoService;

    /**
     * Obtiene el menú completo del local agrupado por categorías.
     * Incluye productos con sus extras disponibles.
     * 
     * @param telefonoLocal Teléfono del local con código de país (ej: "5491112345678")
     * @return MenuCompletoDTO con toda la información del catálogo
     */
    @Operation(
        summary = "Obtener menú completo",
        description = "Obtiene el catálogo completo del local agrupado por categorías, " +
                      "incluyendo productos con sus extras disponibles, horarios y modalidades."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Menú obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = MenuCompletoDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Local no encontrado"
        )
    })
    @GetMapping("/{telefonoLocal}")
    public ResponseEntity<MenuCompletoDTO> obtenerMenuCompleto(
        @Parameter(description = "Teléfono del local con código de país", example = "5491112345678")
        @PathVariable String telefonoLocal
    ) {
        log.info("GET /api/menu/{} - Solicitud de menú completo", telefonoLocal);
        
        MenuCompletoDTO menu = catalogoService.obtenerCatalogoCompleto(telefonoLocal);
        
        log.info("Menú obtenido exitosamente para local: {}", telefonoLocal);
        return ResponseEntity.ok(menu);
    }
}
