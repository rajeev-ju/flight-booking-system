package com.airline.flight.contract.request

import jakarta.validation.constraints.*
import java.time.LocalTime

/**
 * Request DTO for updating flight details
 */
data class UpdateFlightRequest(
    @field:NotBlank(message = "Flight number is required")
    @field:Pattern(regexp = "^[A-Z]{2}\\d{3,4}$", message = "Flight number must be in format like AI101")
    val flightNumber: String,
    
    @field:NotNull(message = "Departure time is required")
    val departureTime: LocalTime,
    
    @field:NotNull(message = "Arrival time is required")
    val arrivalTime: LocalTime,
    
    @field:Min(value = 30, message = "Duration must be at least 30 minutes")
    @field:Max(value = 1440, message = "Duration cannot exceed 24 hours")
    val durationMinutes: Int,
    
    @field:NotBlank(message = "Aircraft type is required")
    val aircraftType: String,
    
    @field:Min(value = 50, message = "Total seats must be at least 50")
    @field:Max(value = 500, message = "Total seats cannot exceed 500")
    val totalSeats: Int,
    
    @field:DecimalMin(value = "100.0", message = "Price must be at least 100")
    @field:DecimalMax(value = "50000.0", message = "Price cannot exceed 50000")
    val price: Double,
    
    val active: Boolean = true
)
