package com.airline.flight.contract.response

import java.time.LocalDateTime
import java.util.UUID

/**
 * Response DTO for flight schedule details
 * Used by booking service to fetch complete flight information
 */
data class FlightScheduleDetails(
    val id: UUID,
    val flightNumber: String,
    val origin: String,
    val destination: String,
    val departureDateTime: LocalDateTime,
    val arrivalDateTime: LocalDateTime,
    val availableSeats: Int,
    val totalSeats: Int,
    val price: Double,
    val status: String
)
