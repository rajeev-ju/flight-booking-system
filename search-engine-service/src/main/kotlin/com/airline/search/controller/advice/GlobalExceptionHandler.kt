package com.airline.search.controller.advice

import com.airline.shared.dto.ApiResponse
import com.airline.shared.dto.ErrorDetails
import com.airline.shared.exception.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

/**
 * Clean Global Exception Handler - Only handles exceptions and logs errors
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BaseException::class)
    fun handleBusinessException(ex: BaseException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Business exception: ${ex.message}", ex)

        val errorDetails = ErrorDetails(
            message = ex.message ?: "Business operation failed",
        )

        val httpStatus = when (ex) {
            is FlightNotFoundException, is BookingNotFoundException -> HttpStatus.NOT_FOUND
            is FlightNotAvailableException -> HttpStatus.CONFLICT
            is InvalidSearchCriteriaException, is ValidationException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity.status(httpStatus).body(ApiResponse.error(errorDetails))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Validation exception: ${ex.bindingResult.fieldErrors.size} field errors", ex)

        val firstError = ex.bindingResult.fieldErrors.firstOrNull()
        val errorDetails = ErrorDetails(
            code = "VALIDATION_ERROR",
            message = firstError?.defaultMessage ?: "Request validation failed",
            field = firstError?.field,
            details = firstError?.rejectedValue?.toString()
        )

        return ResponseEntity.badRequest().body(ApiResponse.error(errorDetails))
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(ex: WebExchangeBindException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Web exchange bind exception", ex)

        val firstError = ex.bindingResult.fieldErrors.firstOrNull()
        val errorDetails = ErrorDetails(
            code = "VALIDATION_ERROR",
            message = firstError?.defaultMessage ?: "Request validation failed",
            field = firstError?.field,
            details = firstError?.rejectedValue?.toString()
        )

        return ResponseEntity.badRequest().body(ApiResponse.error(errorDetails))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected error: ${ex.javaClass.simpleName} - ${ex.message}", ex)

        val errorDetails = ErrorDetails(
            code = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred",
            details = "Please try again later or contact support"
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(errorDetails))
    }
}
