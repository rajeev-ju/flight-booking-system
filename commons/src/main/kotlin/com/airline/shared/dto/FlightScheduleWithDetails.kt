package com.airline.shared.dto

import com.airline.shared.enums.FlightStatus
import java.time.LocalDateTime
import java.util.*

data class FlightScheduleWithDetails(
    val id: UUID,
    val flightNumber: String,
    val origin: String,
    val destination: String,
    val departureDateTime: LocalDateTime,
    val arrivalDateTime: LocalDateTime,
    val availableSeats: Int,
    val totalSeats: Int,
    val price: Double,
    val status: FlightStatus
)