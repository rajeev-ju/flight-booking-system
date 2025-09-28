package com.airline.booking.contract.request

import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class CreateBookingRequest(
    @field:NotNull(message = "Flight schedule ID is required")
    val flightScheduleId: UUID,
    
    // Optional - will be fetched from flight schedule if not provided
    val flightNumber: String? = null,


    @field:NotEmpty(message = "At least one passenger is required")
    @field:Size(min = 1, max = 9, message = "Booking can have 1 to 9 passengers")
    @field:Valid
    val passengers: List<PassengerInfo>,

    @field:NotBlank(message = "Contact email is required")
    @field:Email(message = "Invalid email format")
    val contactEmail: String,

    @field:NotBlank(message = "Contact phone is required")
    @field:Pattern(
        regexp = "^\\+?[1-9]\\d{9,14}$",
        message = "Invalid phone number format"
    )
    val contactPhone: String,

    @field:NotNull(message = "Total amount is required")
    @field:DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @field:DecimalMax(value = "999999.99", message = "Total amount exceeds maximum limit")
    val totalAmount: BigDecimal
)
