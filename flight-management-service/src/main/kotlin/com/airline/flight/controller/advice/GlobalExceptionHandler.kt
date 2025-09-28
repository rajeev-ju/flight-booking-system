package com.airline.flight.controller.advice

import com.airline.flight.contract.response.ApiResponse
import com.airline.shared.exception.FlightNotFoundException
import com.airline.flight.exception.InsufficientSeatsException
import com.airline.flight.exception.ScheduleNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebInputException
import java.time.Instant

/**
 * Global Exception Handler for Flight Management Service
 * Following production-grade error handling patterns
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Map<String, String>>> {
        val errors = mutableMapOf<String, String>()
        
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Invalid value"
            errors[fieldName] = errorMessage
        }
        
        logger.warn("Validation errors: $errors")
        
        val response = ApiResponse<Map<String, String>>(
            success = false,
            data = errors,
            message = "Validation failed",
            errorCode = "VALIDATION_ERROR",
            timestamp = Instant.now()
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
    
    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Illegal argument: ${ex.message}")
        
        val response = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Invalid argument",
            errorCode = "INVALID_ARGUMENT",
            timestamp = Instant.now()
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
    
    /**
     * Handle server web input exceptions
     */
    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInputException(ex: ServerWebInputException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Invalid input: ${ex.message}")
        
        val response = ApiResponse<Nothing>(
            success = false,
            message = "Invalid input format",
            errorCode = "INVALID_INPUT",
            timestamp = Instant.now()
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
    
    /**
     * Handle flight not found exceptions
     */
    @ExceptionHandler(FlightNotFoundException::class)
    fun handleFlightNotFound(ex: FlightNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Flight not found: ${ex.message}")
        
        val response = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Flight not found",
            errorCode = "FLIGHT_NOT_FOUND",
            timestamp = Instant.now()
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }
    
    /**
     * Handle schedule not found exceptions
     */
    @ExceptionHandler(ScheduleNotFoundException::class)
    fun handleScheduleNotFound(ex: ScheduleNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Schedule not found: ${ex.message}")
        
        val response = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Schedule not found",
            errorCode = "SCHEDULE_NOT_FOUND",
            timestamp = Instant.now()
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }
    
    /**
     * Handle insufficient seats exceptions
     */
    @ExceptionHandler(InsufficientSeatsException::class)
    fun handleInsufficientSeats(ex: InsufficientSeatsException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Insufficient seats: ${ex.message}")
        
        val response = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Insufficient seats available",
            errorCode = "INSUFFICIENT_SEATS",
            timestamp = Instant.now()
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }
    
    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Runtime exception: ${ex.message}", ex)
        
        val response = ApiResponse<Nothing>(
            success = false,
            message = "An error occurred while processing your request",
            errorCode = "RUNTIME_ERROR",
            timestamp = Instant.now()
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected exception: ${ex.message}", ex)
        
        val response = ApiResponse<Nothing>(
            success = false,
            message = "An unexpected error occurred",
            errorCode = "INTERNAL_ERROR",
            timestamp = Instant.now()
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
