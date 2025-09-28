package com.airline.booking.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @ExceptionHandler(BookingNotFoundException::class)
    suspend fun handleBookingNotFoundException(ex: BookingNotFoundException): ResponseEntity<ErrorResponse> {
        logger.error("Booking not found: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    error = "BOOKING_NOT_FOUND",
                    message = ex.message ?: "Booking not found",
                    timestamp = LocalDateTime.now()
                )
            )
    }
    
    @ExceptionHandler(SeatNotAvailableException::class)
    suspend fun handleSeatNotAvailableException(ex: SeatNotAvailableException): ResponseEntity<ErrorResponse> {
        logger.error("Seats not available: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    error = "SEATS_NOT_AVAILABLE",
                    message = ex.message ?: "Seats not available",
                    timestamp = LocalDateTime.now()
                )
            )
    }
    
    @ExceptionHandler(InvalidBookingStateException::class)
    suspend fun handleInvalidBookingStateException(ex: InvalidBookingStateException): ResponseEntity<ErrorResponse> {
        logger.error("Invalid booking state: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    error = "INVALID_BOOKING_STATE",
                    message = ex.message ?: "Invalid booking state",
                    timestamp = LocalDateTime.now()
                )
            )
    }
    
    @ExceptionHandler(WebExchangeBindException::class)
    suspend fun handleValidationException(ex: WebExchangeBindException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { 
            "${it.field}: ${it.defaultMessage}"
        }.joinToString(", ")
        
        logger.error("Validation error: $errors")
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    error = "VALIDATION_ERROR",
                    message = "Validation failed: $errors",
                    timestamp = LocalDateTime.now()
                )
            )
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    suspend fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { 
            "${it.field}: ${it.defaultMessage}"
        }.joinToString(", ")
        
        logger.error("Method argument validation error: $errors")
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    error = "VALIDATION_ERROR",
                    message = "Validation failed: $errors",
                    timestamp = LocalDateTime.now()
                )
            )
    }
    
    @ExceptionHandler(Exception::class)
    suspend fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    error = "INTERNAL_SERVER_ERROR",
                    message = "An unexpected error occurred: ${ex.message}",
                    timestamp = LocalDateTime.now()
                )
            )
    }
}

data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: LocalDateTime
)
