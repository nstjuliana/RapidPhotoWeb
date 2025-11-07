package com.rapidphotoupload.infrastructure.web.exceptions;

import com.rapidphotoupload.infrastructure.web.dto.ErrorResponseDto;
import com.rapidphotoupload.shared.exceptions.DomainException;
import com.rapidphotoupload.shared.exceptions.EntityNotFoundException;
import com.rapidphotoupload.shared.exceptions.StorageException;
import com.rapidphotoupload.shared.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for REST API endpoints.
 * 
 * This handler provides centralized error handling for all exceptions thrown
 * by controllers. It converts domain exceptions to appropriate HTTP status codes
 * and returns consistent error response DTOs.
 * 
 * Exception mapping:
 * - EntityNotFoundException → 404 Not Found
 * - ValidationException → 400 Bad Request
 * - StorageException → 503 Service Unavailable
 * - DomainException → 400 Bad Request
 * - IllegalArgumentException → 400 Bad Request
 * - WebExchangeBindException → 400 Bad Request (validation errors)
 * - Generic exceptions → 500 Internal Server Error
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(EntityNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleEntityNotFoundException(
            EntityNotFoundException ex, ServerWebExchange exchange) {
        logger.warn("Entity not found: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                "Entity Not Found",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }
    
    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleValidationException(
            ValidationException ex, ServerWebExchange exchange) {
        logger.warn("Validation error: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                "Validation Error",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }
    
    @ExceptionHandler(StorageException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleStorageException(
            StorageException ex, ServerWebExchange exchange) {
        logger.error("Storage error: {}", ex.getMessage(), ex);
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                "Storage Error",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse));
    }
    
    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleDomainException(
            DomainException ex, ServerWebExchange exchange) {
        logger.warn("Domain error: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                "Domain Error",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleIllegalArgumentException(
            IllegalArgumentException ex, ServerWebExchange exchange) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                "Invalid Argument",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }
    
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleWebExchangeBindException(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        logger.warn("Validation binding error: {}", ex.getMessage());
        
        // Extract first validation error message
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                "Validation Error",
                errorMessage,
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleNoResourceFoundException(
            NoResourceFoundException ex, ServerWebExchange exchange) {
        logger.debug("Resource not found: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                "Not Found",
                "The requested resource was not found. API endpoints are available at /api/*",
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }
    
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                "Internal Server Error",
                "An unexpected error occurred",
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
}

