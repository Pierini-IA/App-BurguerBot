package com.dioburger.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para todos los controladores REST.
 * Centraliza el manejo de errores y proporciona respuestas consistentes.
 * 
 * @author Dio Burger Team
 * @version 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de recursos no encontrados.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        log.warn("❌ Recurso no encontrado: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja excepciones de stock insuficiente.
     */
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleStockInsuficienteException(StockInsuficienteException ex) {
        log.warn("⚠️ Stock insuficiente: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Stock Insuficiente")
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones de horarios no disponibles.
     */
    @ExceptionHandler(HorarioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleHorarioNoDisponibleException(HorarioNoDisponibleException ex) {
        log.warn("⚠️ Horario no disponible: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Horario No Disponible")
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones de pedidos duplicados.
     */
    @ExceptionHandler(PedidoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handlePedidoDuplicadoException(PedidoDuplicadoException ex) {
        log.warn("⚠️ Pedido duplicado: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Pedido Duplicado")
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones de reservas duplicadas.
     */
    @ExceptionHandler(ReservaDuplicadaException.class)
    public ResponseEntity<ErrorResponse> handleReservaDuplicadaException(ReservaDuplicadaException ex) {
        log.warn("⚠️ Reserva duplicada: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Reserva Duplicada")
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones de cancelaciones no permitidas.
     */
    @ExceptionHandler(CancelacionNoPermitidaException.class)
    public ResponseEntity<ErrorResponse> handleCancelacionNoPermitidaException(CancelacionNoPermitidaException ex) {
        log.warn("⚠️ Cancelación no permitida: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Cancelación No Permitida")
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja errores de validación de datos (anotaciones @Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("⚠️ Error de validación: {}", errors);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Datos inválidos")
                .validationErrors(errors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones de acceso denegado (seguridad).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("🚫 Acceso denegado: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("No tienes permisos para acceder a este recurso")
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Maneja excepciones genéricas de IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("⚠️ Argumento inválido: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones de feature no disponible en el plan.
     */
    @ExceptionHandler(FeatureNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleFeatureNotAvailableException(FeatureNotAvailableException ex) {
        log.warn("🔒 Feature no disponible: {} - Plan actual: {}, Requerido: {}", 
            ex.getFeature(), ex.getPlanActual(), ex.getPlanRequerido());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYMENT_REQUIRED.value())  // 402 Payment Required
                .error("Feature Not Available")
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(error);
    }

    /**
     * Maneja excepciones genéricas no capturadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("❌ Error interno del servidor: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ha ocurrido un error interno. Por favor contacta al administrador.")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * DTO para respuestas de error estandarizadas.
     */
    @lombok.Data
    @lombok.Builder
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private Map<String, String> validationErrors;
    }
}
