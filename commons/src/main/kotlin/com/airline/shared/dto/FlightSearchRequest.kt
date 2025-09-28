package com.airline.shared.dto

import com.airline.shared.enums.CabinClass
import com.airline.shared.enums.SortOption
import jakarta.validation.constraints.*
import java.time.LocalDate

/**
 * Flight search request DTO with comprehensive validation
 */
data class FlightSearchRequest(
    @field:NotBlank(message = "Origin airport code is required")
    @field:Size(min = 3, max = 3, message = "Origin must be a 3-letter airport code")
    @field:Pattern(regexp = "^[A-Z]{3}$", message = "Origin must be uppercase 3-letter code")
    val origin: String,

    @field:NotBlank(message = "Destination airport code is required")
    @field:Size(min = 3, max = 3, message = "Destination must be a 3-letter airport code")
    @field:Pattern(regexp = "^[A-Z]{3}$", message = "Destination must be uppercase 3-letter code")
    val destination: String,

    @field:NotNull(message = "Departure date is required")
    @field:Future(message = "Departure date must be in the future")
    val departureDate: LocalDate,

    @field:Min(value = 1, message = "At least 1 passenger is required")
    @field:Max(value = 9, message = "Maximum 9 passengers allowed")
    val passengers: Int,

    val returnDate: LocalDate? = null,

    val sortBy: SortOption = SortOption.PRICE,

    @field:Min(value = 1, message = "Max results must be at least 1")
    @field:Max(value = 100, message = "Max results cannot exceed 100")
    val maxResults: Int = 20,

    val includeConnecting: Boolean = true,

    @field:Min(value = 0, message = "Max stops cannot be negative")
    @field:Max(value = 2, message = "Max stops cannot exceed 2")
    val maxStops: Int = 1,

    val cabinClass: CabinClass = CabinClass.ECONOMY
)
