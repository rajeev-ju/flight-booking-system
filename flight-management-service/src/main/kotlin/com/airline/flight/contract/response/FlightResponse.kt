package com.airline.flight.contract.response

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Response DTO for flight information
 */
data class FlightResponse(
    val id: String,
    val flightNumber: String,
    val routeId: String,
    val departureTime: LocalTime,
    val arrivalTime: LocalTime,
    val durationMinutes: Int,
    val aircraftType: String,
    val totalSeats: Int,
    val availableSeats: Int,
    val price: Double,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Simplified flight response for search operations
 */
data class FlightSearchResponse(
    val id: String,
    val flightNumber: String,
    val originAirportCode: String,
    val destinationAirportCode: String,
    val airlineCode: String,
    val airlineName: String,
    val departureTime: LocalTime,
    val arrivalTime: LocalTime,
    val durationMinutes: Int,
    val aircraftType: String,
    val totalSeats: Int,
    val basePrice: Double,
    val active: Boolean
)
