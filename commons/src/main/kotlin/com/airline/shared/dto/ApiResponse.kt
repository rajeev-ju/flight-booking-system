package com.airline.shared.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

/**
 * Standard API response wrapper following consistent contract
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetails? = null,
    val timestamp: Instant = Instant.now()
) {
    companion object {
        fun <T> success(data: T, requestId: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data
            )
        }

        fun <T> error(error: ErrorDetails, requestId: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = error
            )
        }

        fun <T> error(
            code: String,
            message: String,
            details: String? = null,
            requestId: String? = null
        ): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ErrorDetails(code, message, details)
            )
        }
    }
}

data class ErrorDetails(
    val code: String? = null,
    val message: String,
    val details: String? = null,
    val field: String? = null
)
