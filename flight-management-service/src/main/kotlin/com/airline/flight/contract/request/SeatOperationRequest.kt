package com.airline.flight.contract.request

import jakarta.validation.constraints.*

/**
 * Request DTO for seat operations (reserve/release)
 */
data class SeatOperationRequest(
    @field:Min(value = 1, message = "Number of seats must be at least 1")
    @field:Max(value = 9, message = "Number of seats cannot exceed 9")
    val seats: Int,
    
    val reason: String? = null
)
