package com.airline.shared.model

import com.airline.shared.enums.FlightStatus
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class Flight(
    val id: UUID,
    val flightNumber: String, // e.g., "AI101"
    val airlineCode: String, // e.g., "AI" for Air India
    val originAirportCode: String,
    val destinationAirportCode: String,
    val departureTime: LocalTime, // Daily departure time
    val arrivalTime: LocalTime, // Daily arrival time
    val duration: Int, // Duration in minutes
    val aircraft: String, // Aircraft type
    val totalSeats: Int,
    val basePrice: Double, // Base price for this route
    val active: Boolean = true,
    val effectiveFrom: LocalDateTime,
    val effectiveTo: LocalDateTime
)

data class FlightSchedule(
    val id: UUID,
    val flightNumber: String,
    val scheduleDate: LocalDateTime, // Specific date for this flight
    val departureDateTime: LocalDateTime,
    val arrivalDateTime: LocalDateTime,
    val availableSeats: Int,
    val price: Double, // Can vary from base price
    val status: FlightStatus = FlightStatus.SCHEDULED
)
