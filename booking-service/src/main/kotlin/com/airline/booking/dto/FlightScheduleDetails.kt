package com.airline.booking.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
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