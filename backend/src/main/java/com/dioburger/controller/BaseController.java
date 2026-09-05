package com.dioburger.controller;

import com.dioburger.model.entity.Local;
import com.dioburger.service.LocalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador base con métodos utilitarios compartidos por todos los controladores.
 * Reduce código duplicado y proporciona funcionalidad común.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Slf4j
public abstract class BaseController {

    @Autowired
    protected LocalService localService;

    /**
     * Obtiene un local por su teléfono (Multi-Tenant ID).
     * Método de conveniencia usado por todos los controladores.
     * 
     * @param telefonoLocal Teléfono del local
     * @return Local encontrado
     */
    protected Local obtenerLocal(String telefonoLocal) {
        return localService.buscarPorTelefono(telefonoLocal);
    }

    /**
     * Crea una respuesta de éxito con mensaje personalizado.
     * 
     * @param message Mensaje de éxito
     * @return ResponseEntity con status 200 y mensaje
     */
    protected ResponseEntity<Map<String, Object>> successResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una respuesta de éxito con datos.
     * 
     * @param message Mensaje de éxito
     * @param data Datos a devolver
     * @return ResponseEntity con status 200, mensaje y datos
     */
    protected ResponseEntity<Map<String, Object>> successResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una respuesta CREATED (201) para recursos nuevos.
     * 
     * @param message Mensaje de éxito
     * @param data Recurso creado
     * @return ResponseEntity con status 201
     */
    protected ResponseEntity<Map<String, Object>> createdResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Crea una respuesta de error con código de estado personalizado.
     * 
     * @param status Código HTTP
     * @param message Mensaje de error
     * @return ResponseEntity con status y mensaje
     */
    protected ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Valida que el local exista antes de ejecutar operaciones.
     * Lanza NotFoundException si no existe.
     * 
     * @param telefonoLocal Teléfono del local a validar
     */
    protected void validarLocal(String telefonoLocal) {
        localService.buscarPorTelefono(telefonoLocal);
    }

    /**
     * Verifica que un recurso pertenezca al local actual (Multi-Tenancy).
     * 
     * @param recursoLocalId ID del local del recurso
     * @param localActualId ID del local actual
     * @return true si pertenece al mismo local
     */
    protected boolean perteneceAlLocal(Long recursoLocalId, Long localActualId) {
        return recursoLocalId.equals(localActualId);
    }

    /**
     * Loguea el inicio de una operación con formato consistente.
     * 
     * @param operacion Nombre de la operación
     * @param telefonoLocal Teléfono del local
     */
    protected void logOperacionInicio(String operacion, String telefonoLocal) {
        log.info("🏪 {} - Local: {}", operacion, telefonoLocal);
    }

    /**
     * Loguea el éxito de una operación con formato consistente.
     * 
     * @param operacion Nombre de la operación
     * @param detalle Detalle adicional
     */
    protected void logOperacionExito(String operacion, String detalle) {
        log.info("✅ {} - {}", operacion, detalle);
    }

    /**
     * Loguea una advertencia de operación.
     * 
     * @param operacion Nombre de la operación
     * @param detalle Detalle de la advertencia
     */
    protected void logAdvertencia(String operacion, String detalle) {
        log.warn("⚠️ {} - {}", operacion, detalle);
    }

    /**
     * Loguea un error de operación con formato consistente.
     * 
     * @param operacion Nombre de la operación
     * @param detalle Detalle del error
     */
    protected void logOperacionError(String operacion, String detalle) {
        log.error("❌ {} - {}", operacion, detalle);
    }
}
