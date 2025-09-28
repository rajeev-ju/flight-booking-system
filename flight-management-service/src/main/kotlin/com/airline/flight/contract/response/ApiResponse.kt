package com.airline.flight.contract.response

import java.time.Instant

/**
 * Standard API response wrapper
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null,
    val timestamp: Instant = Instant.now(),
    val requestId: String? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null, requestId: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message,
                requestId = requestId
            )
        }
        
        fun <T> error(message: String, errorCode: String? = null, requestId: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                errorCode = errorCode,
                requestId = requestId
            )
        }
    }
}

/**
 * Operation result response
 */
data class OperationResponse(
    val success: Boolean,
    val message: String,
    val affectedRows: Int = 0,
    val resourceId: String? = null
)
