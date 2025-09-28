package com.airline.flight.contract.response

import com.airline.shared.enums.FlightStatus
import java.time.LocalDateTime

/**
 * Response DTO for flight schedule information
 */
data class ScheduleResponse(
    val flightNumber: String,
    val scheduleDate: LocalDateTime,
    val departureDateTime: LocalDateTime,
    val arrivalDateTime: LocalDateTime,
    val availableSeats: Int,
    val price: Double,
    val status: FlightStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Enhanced schedule response with flight details for search operations
 */
data class ScheduleSearchResponse(
    val id: String,
    val flightNumber: String,
    val airlineCode: String,
    val airlineName: String,
    val originAirportCode: String,
    val destinationAirportCode: String,
    val scheduleDate: LocalDateTime,
    val departureDateTime: LocalDateTime,
    val arrivalDateTime: LocalDateTime,
    val durationMinutes: Int,
    val aircraftType: String,
    val availableSeats: Int,
    val price: Double,
    val status: FlightStatus
)
