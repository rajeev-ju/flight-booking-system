package com.airline.flight.contract.request

import jakarta.validation.constraints.*
import java.time.LocalDateTime

/**
 * Request DTO for creating a flight schedule
 */
data class CreateScheduleRequest(
    @field:NotBlank(message = "Flight ID is required")
    val flightNumber: String,

    @field:NotNull(message = "Schedule date is required")
    @field:Future(message = "Schedule date must be in the future")
    val scheduleDate: LocalDateTime,

    @field:NotNull(message = "Departure date time is required")
    val departureDateTime: LocalDateTime,

    @field:NotNull(message = "Arrival date time is required")
    val arrivalDateTime: LocalDateTime,

    @field:Min(value = 0, message = "Available seats cannot be negative")
    @field:Max(value = 500, message = "Available seats cannot exceed 500")
    val availableSeats: Int,

    @field:DecimalMin(value = "100.0", message = "Price must be at least 100")
    @field:DecimalMax(value = "50000.0", message = "Price cannot exceed 50000")
    val price: Double
)
